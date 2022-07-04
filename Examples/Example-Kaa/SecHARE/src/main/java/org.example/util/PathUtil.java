package org.example.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;

public class PathUtil {

    public static String getPathFromClass(Class<?> cls) {
        if (cls == null) {
            return null;
        }
        try {
            String path = null;
            URL url = getClassLocationUrl(cls);
            if (url != null) {
                path = url.getPath();
                if ("jar".equalsIgnoreCase(url.getProtocol())) {
                    path = new URL(path).getPath();
                    int location = path.indexOf("!/");
                    if (location != -1) {
                        path = path.substring(0, location);
                    }
                }
                File file = new File(path);
                path = file.getCanonicalPath();
            }
            return path;
        } catch (IOException e) {
            return null;
        }
    }

    public static String getFullPathRelateClass(String relatedPath, Class<?> cls) {
        String path;
        if (relatedPath == null) {
            return null;
        }
        String clsPath = getPathFromClass(cls);
        if (clsPath == null) {
            return null;
        }
        try {
            File clsFile = new File(clsPath);
            String tempPath = clsFile.getParent() + File.separator + relatedPath;
            File file = new File(tempPath);
            path = file.getCanonicalPath();
            return path;
        } catch (IOException e) {
            return null;
        }
    }

    private static URL getClassLocationUrl(Class<?> cls) {
        URL result = null;
        String clsAsResource = cls.getName().replace('.', '/').concat(".class");
        ProtectionDomain pd = cls.getProtectionDomain();
        if (pd != null) {
            CodeSource cs = pd.getCodeSource();
            if (cs != null) {
                result = cs.getLocation();
            }
            if (result != null) {
                if ("file".equals(result.getProtocol())) {
                    try {
                        if (result.toExternalForm().endsWith(".jar") || result.toExternalForm().endsWith(".zip")) {
                            result = new URL("jar:".concat(result.toExternalForm()).concat("!/").concat(clsAsResource));
                        } else if (new File(result.getFile()).isDirectory()) {
                            result = new URL(result, clsAsResource);
                        }
                    } catch (MalformedURLException ignore) {}
                }
            }
        }

        if (result == null) {
            ClassLoader clsLoader = cls.getClassLoader();
            result = clsLoader != null ? clsLoader.getResource(clsAsResource) :
                    ClassLoader.getSystemResource(clsAsResource);
        }
        return result;
    }
}
