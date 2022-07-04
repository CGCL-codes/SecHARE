package org.example.agent;

import org.example.agent.internal.*;
import org.apache.commons.io.IOUtils;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

import javax.annotation.concurrent.ThreadSafe;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

@ThreadSafe
public final class DynamicInstrumentationLoader {

    private static volatile Throwable threadFailed;
    private static volatile String toolsJarPath;
    private static volatile String attachLibPath;

    private DynamicInstrumentationLoader() {
    }


    public static void waitForInitialized() {
        try {
            // TODO sleep 500ms
            TimeUnit.MILLISECONDS.sleep(500);
            if (threadFailed != null) {
                final String javaVersion = getJavaVersion();
                final String javaHome = getJavaHome();
                throw new RuntimeException("Additional information: javaVersion=" + javaVersion + "; javaHome="
                        + javaHome + "; toolsJarPath=" + toolsJarPath + "; attachLibPath=" + attachLibPath,
                        threadFailed);
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    static {
        try {
            File tempAgentJar = createTempJar(true);

            final String pid = DynamicInstrumentationProperties.getProcessId();
            final File finalTempAgentJar = tempAgentJar;
            final Thread loadAgentThread = new Thread() {

                @Override
                public void run() {
                    try {
                        loadAgent(finalTempAgentJar, pid);
                    } catch (final Throwable e) {
                        threadFailed = e;
                        throw new RuntimeException(e);
                    }
                }
            };

            DynamicInstrumentationReflections.addPathToSystemClassLoader(tempAgentJar);

            final JdkFilesFinder jdkFilesFinder = new JdkFilesFinder();

            if (DynamicInstrumentationReflections.isBeforeJava9()) {
                final File toolsJar = jdkFilesFinder.findToolsJar();
                DynamicInstrumentationReflections.addPathToSystemClassLoader(toolsJar);
                DynamicInstrumentationLoader.toolsJarPath = toolsJar.getAbsolutePath();

                final File attachLib = jdkFilesFinder.findAttachLib();
                DynamicInstrumentationReflections.addPathToJavaLibraryPath(attachLib.getParentFile());
                DynamicInstrumentationLoader.attachLibPath = attachLib.getAbsolutePath();
            }

            loadAgentThread.start();
        } catch (final Exception e) {
            throw new RuntimeException("Final exception during agent loading:", e);
        }
    }

    private static void loadAgent(final File tempAgentJar, final String pid) throws Exception {
        if (DynamicInstrumentationReflections.isBeforeJava9()) {
            DynamicInstrumentationLoadAgentMain.loadAgent(pid, tempAgentJar.getAbsolutePath());
        } else {
            //-Djdk.attach.allowAttachSelf https://www.bountysource.com/issues/45231289-self-attach-fails-on-jdk9
            //workaround this limitation by attaching from a new process
            final File loadAgentJar = createTempJar(false, DummyAttachProvider.class);
            final String javaExecutable = getJavaHome() + File.separator + "bin" + File.separator + "java";
            final List<String> command = new ArrayList<>();
            command.add(javaExecutable);
            command.add("-classpath");
            //tools.jar not needed since java9
            command.add(loadAgentJar.getAbsolutePath());
            command.add(DynamicInstrumentationLoadAgentMain.class.getName());
            command.add(pid);
            command.add(tempAgentJar.getAbsolutePath());
            new ProcessExecutor().command(command)
                    .destroyOnExit()
                    .exitValueNormal()
                    .redirectOutput(Slf4jStream.of(DynamicInstrumentationLoader.class).asInfo())
                    .redirectError(Slf4jStream.of(DynamicInstrumentationLoader.class).asWarn())
                    .execute();
        }
    }

    private static String getJavaHome() {
        return System.getProperty("java.home");
    }

    private static String getJavaVersion() {
        return System.getProperty("java.version");
    }

    private static File createTempJar(final boolean agent, final Class<?>... additionalClasses) throws Exception {
        try {
            final String className = DynamicInstrumentationAgent.class.getName();
            try (InputStream classIn = DynamicInstrumentationReflections.getClassInputStream(DynamicInstrumentationAgent.class)) {
                return createTempJar(className, classIn, agent, additionalClasses);
            }
        } catch (final Throwable e) {
            throw newClassNotFoundException(DynamicInstrumentationAgent.class.getName() + ".class", e);
        }
    }

    /**
     * Creates a new jar that only contains the DynamicInstrumentationAgent class.
     */
    private static File createTempJar(final String className, final InputStream classIn, final boolean agent,
                                      final Class<?>... additionalClasses) throws Exception {
        final File tempAgentJar = new File(DynamicInstrumentationProperties.TEMP_DIRECTORY, className + ".jar");
        final Manifest manifest = new Manifest(
                DynamicInstrumentationLoader.class.getResourceAsStream("/META-INF/MANIFEST.MF"));
        if (agent) {
            manifest.getMainAttributes().putValue("Premain-Class", className);
            manifest.getMainAttributes().putValue("Agent-Class", className);
            manifest.getMainAttributes().putValue("Can-Redefine-Classes", String.valueOf(true));
            manifest.getMainAttributes().putValue("Can-Retransform-Classes", String.valueOf(true));
        }

        final JarOutputStream tempJarOut = new JarOutputStream(new FileOutputStream(tempAgentJar), manifest);
        final JarEntry entry = new JarEntry(className.replace(".", "/") + ".class");
        tempJarOut.putNextEntry(entry);
        IOUtils.copy(classIn, tempJarOut);
        tempJarOut.closeEntry();
        if (additionalClasses != null) {
            for (final Class<?> additionalClazz : additionalClasses) {
                final String additionalClassName = additionalClazz.getName();
                final JarEntry additionalEntry = new JarEntry(additionalClassName.replace(".", "/") + ".class");
                tempJarOut.putNextEntry(additionalEntry);
                final InputStream additionalClassIn = DynamicInstrumentationReflections
                        .getClassInputStream(additionalClazz);
                IOUtils.copy(additionalClassIn, tempJarOut);
                tempJarOut.closeEntry();
            }
        }
        tempJarOut.close();
        return tempAgentJar;
    }

    private static ClassNotFoundException newClassNotFoundException(final String file, final Throwable e) {
        final String message = "Unable to find file [" + file + "] in classpath."
                + "\nPlease make sure you have added invesdwin-instrument.jar to your classpath properly,"
                + "\nor make sure you have embedded it correctly into your fat-jar."
                + "\nThey can be created e.g. with \"maven-shade-plugin\"."
                + "\nPlease be aware that some fat-jar solutions might not work well due to classloader issues: "
                + e.toString();
        return new ClassNotFoundException(message, e);
    }

}
