package org.example.agent.internal;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public final class DynamicInstrumentationLoadAgentMain {

    private DynamicInstrumentationLoadAgentMain() {}

    /**
     * https://github.com/jmockit/jmockit1/blob/8ea057ffe08a3ef9788ee383db695a3a404d3c86/main/src/mockit/internal/startup/AgentLoader.java
     * 
     * http://tuhrig.de/implementing-interfaces-and-abstract-classes-on-the-fly/
     */
    public static void loadAgent(final String pid, final String agentJarAbsolutePath) {
        //use reflection since tools.jar has been added to the classpath dynamically
        try {
            final Class<?> attachProviderClass = Class.forName("com.sun.tools.attach.spi.AttachProvider");
            final List<?> providers = (List<?>) attachProviderClass.getMethod("providers").invoke(null);
            final Class<?> virtualMachineClass;
            final Object virtualMachine;
            if (providers.isEmpty()) {
                final String virtualMachineClassName = findVirtualMachineClassNameAccordingToOS();
                virtualMachineClass = Class.forName(virtualMachineClassName);
                final Constructor<?> vmConstructor = virtualMachineClass.getDeclaredConstructor(attachProviderClass,
                        String.class);
                vmConstructor.setAccessible(true);
                final Object dummyAttachProvider = Class.forName("cn.chenxing.agent.internal.DummyAttachProvider")
                        .getDeclaredConstructor()
                        .newInstance();
                virtualMachine = vmConstructor.newInstance(dummyAttachProvider, pid);
            } else {
                virtualMachineClass = Class.forName("com.sun.tools.attach.VirtualMachine");
                virtualMachine = virtualMachineClass.getMethod("attach", String.class).invoke(null, pid);
            }
            virtualMachineClass.getMethod("loadAgent", String.class).invoke(virtualMachine, agentJarAbsolutePath);
            virtualMachineClass.getMethod("detach").invoke(virtualMachine);
        } catch (final IllegalAccessException | IllegalArgumentException | InvocationTargetException |
                NoSuchMethodException | SecurityException | InstantiationException e) {
            throw new RuntimeException(e);
        } catch (final ClassNotFoundException e) {
            throw new RuntimeException(newHelpMessageForNonHotSpotVM(), e);
        } catch (final NoClassDefFoundError | UnsatisfiedLinkError e) {
            throw new IllegalStateException("Native library for Attach API not available in this JRE", e);
        }
    }

    private static String findVirtualMachineClassNameAccordingToOS() {
        if (File.separatorChar == '\\') {
            return "sun.tools.attach.WindowsVirtualMachine";
        }

        final String osName = System.getProperty("os.name");

        if (osName.startsWith("Linux") || osName.startsWith("LINUX")) {
            return "sun.tools.attach.LinuxVirtualMachine";
        }

        if (osName.contains("FreeBSD") || osName.startsWith("Mac OS X")) {
            return "sun.tools.attach.BsdVirtualMachine";
        }

        if (osName.startsWith("Solaris") || osName.contains("SunOS")) {
            return "sun.tools.attach.SolarisVirtualMachine";
        }

        if (osName.contains("AIX")) {
            return "sun.tools.attach.AixVirtualMachine";
        }

        throw new IllegalStateException("Cannot use Attach API on unknown OS: " + osName);
    }

    private static String newHelpMessageForNonHotSpotVM() {
        final String vmName = System.getProperty("java.vm.name");
        String helpMessage = "To run on " + vmName;

        if (vmName.contains("J9")) {
            helpMessage += ", add <IBM SDK>/lib/tools.jar to the runtime classpath (before invesdwin-instrument), or";
        }

        return helpMessage + " use -javaagent:invesdwin-instrument.jar";
    }

}
