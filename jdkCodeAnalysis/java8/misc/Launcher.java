package sun.misc;

import java.io.File;
import java.io.IOException;
import java.io.FilePermission;
import java.net.*;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.Set;
import java.util.Vector;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.security.AccessControlContext;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Permission;
import java.security.ProtectionDomain;
import java.security.CodeSource;
import sun.security.util.SecurityConstants;
import sun.net.www.ParseUtil;

public class Launcher {
    private static URLStreamHandlerFactory factory = new Factory();
    private static Launcher launcher = new Launcher();
    private static String bootClassPath = System.getProperty("sun.boot.class.path");

    public static Launcher getLauncher() {
        return launcher;
    }

    private ClassLoader loader;

    public Launcher() {
        // Create the extension class loader
        ClassLoader extcl;
        try {
            // 扩展类加载器
            extcl = ExtClassLoader.getExtClassLoader();
        } catch (IOException e) {
            throw new InternalError("Could not create extension class loader", e);
        }

        // Now create the class loader to use to launch the application
        try {
            // 系统类加载器，将扩展类加载器作为父加载器传入
            loader = AppClassLoader.getAppClassLoader(extcl);
        } catch (IOException e) {
            throw new InternalError("Could not create application class loader", e);
        }

        // 默认设置AppClassLoader为线程上下文类加载器
        Thread.currentThread().setContextClassLoader(loader);

        // Finally, install a security manager if requested
        String s = System.getProperty("java.security.manager");
        if (s != null) {
            // init FileSystem machinery before SecurityManager installation
            sun.nio.fs.DefaultFileSystemProvider.create();

            SecurityManager sm = null;
            if ("".equals(s) || "default".equals(s)) {
                sm = new java.lang.SecurityManager();
            } else {
                try {
                    sm = (SecurityManager)loader.loadClass(s).newInstance();
                } catch (IllegalAccessException e) {
                } catch (InstantiationException e) {
                } catch (ClassNotFoundException e) {
                } catch (ClassCastException e) {
                }
            }
            if (sm != null) {
                System.setSecurityManager(sm);
            } else {
                throw new InternalError("Could not create SecurityManager: " + s);
            }
        }
    }

    /*
     * Returns the class loader used to launch the main application.
     */
    public ClassLoader getClassLoader() {
        return loader;
    }

    /*
     * 扩展类加载器，是系统类的父加载器，但两者没有继承关系
     */
    static class ExtClassLoader extends URLClassLoader {

        static {
            ClassLoader.registerAsParallelCapable();
        }
        // DCL+volatile 创建单例
        private static volatile ExtClassLoader instance = null;

        public static ExtClassLoader getExtClassLoader() throws IOException {
            if (instance == null) {
                synchronized(ExtClassLoader.class) {
                    if (instance == null) {
                        instance = createExtClassLoader();
                    }
                }
            }
            return instance;
        }

        private static ExtClassLoader createExtClassLoader() throws IOException {
            try {
                return AccessController.doPrivileged(
                    new PrivilegedExceptionAction<ExtClassLoader>() {
                        public ExtClassLoader run() throws IOException {
                            final File[] dirs = getExtDirs();
                            int len = dirs.length;
                            for (int i = 0; i < len; i++) {
                                MetaIndex.registerDirectory(dirs[i]);
                            }
                            return new ExtClassLoader(dirs);
                        }
                    });
            } catch (java.security.PrivilegedActionException e) {
                throw (IOException) e.getException();
            }
        }

        void addExtURL(URL url) {
            super.addURL(url);
        }

        /*
         * Creates a new ExtClassLoader for the specified directories.
         */
        public ExtClassLoader(File[] dirs) throws IOException {
            // ExtClassLoader的父类加载器置为 null，
            super(getExtURLs(dirs), null, factory);
            SharedSecrets.getJavaNetAccess().
                getURLClassPath(this).initLookupCache(this);
        }
        // 指定-D java.ext.dirs参数来添加和改变ExtClassLoader的加载路径
        private static File[] getExtDirs() {
            String s = System.getProperty("java.ext.dirs");
            File[] dirs;
            if (s != null) {
                StringTokenizer st =
                    new StringTokenizer(s, File.pathSeparator);
                int count = st.countTokens();
                dirs = new File[count];
                for (int i = 0; i < count; i++) {
                    dirs[i] = new File(st.nextToken());
                }
            } else {
                dirs = new File[0];
            }
            return dirs;
        }

        private static URL[] getExtURLs(File[] dirs) throws IOException {
            Vector<URL> urls = new Vector<URL>();
            for (int i = 0; i < dirs.length; i++) {
                String[] files = dirs[i].list();
                if (files != null) {
                    for (int j = 0; j < files.length; j++) {
                        if (!files[j].equals("meta-index")) {
                            File f = new File(dirs[i], files[j]);
                            urls.add(getFileURL(f));
                        }
                    }
                }
            }
            URL[] ua = new URL[urls.size()];
            urls.copyInto(ua);
            return ua;
        }


        public String findLibrary(String name) {
            name = System.mapLibraryName(name);
            URL[] urls = super.getURLs();
            File prevDir = null;
            for (int i = 0; i < urls.length; i++) {
                // Get the ext directory from the URL; convert to
                // URI first, so the URL will be decoded.
                URI uri;
                try {
                    uri = urls[i].toURI();
                } catch (URISyntaxException ue) {
                    // skip this URL if cannot convert it to URI
                    continue;
                }
                // Use the Paths.get(uri) call in order to handle
                // UNC based file name conversion correctly.
                File dir = Paths.get(uri).toFile().getParentFile();
                if (dir != null && !dir.equals(prevDir)) {
                    // Look in architecture-specific subdirectory first
                    // Read from the saved system properties to avoid deadlock
                    String arch = VM.getSavedProperty("os.arch");
                    if (arch != null) {
                        File file = new File(new File(dir, arch), name);
                        if (file.exists()) {
                            return file.getAbsolutePath();
                        }
                    }
                    // Then check the extension directory
                    File file = new File(dir, name);
                    if (file.exists()) {
                        return file.getAbsolutePath();
                    }
                }
                prevDir = dir;
            }
            return null;
        }

        private static AccessControlContext getContext(File[] dirs)
            throws IOException
        {
            PathPermissions perms =
                new PathPermissions(dirs);

            ProtectionDomain domain = new ProtectionDomain(
                new CodeSource(perms.getCodeBase(),
                    (java.security.cert.Certificate[]) null),
                perms);

            AccessControlContext acc =
                new AccessControlContext(new ProtectionDomain[] { domain });

            return acc;
        }
    }

    /**
     * 系统类加载器
     */
    static class AppClassLoader extends URLClassLoader {

        static {
            ClassLoader.registerAsParallelCapable();
        }

        public static ClassLoader getAppClassLoader(final ClassLoader extcl) throws IOException {
            final String s = System.getProperty("java.class.path");
            final File[] path = (s == null) ? new File[0] : getClassPath(s);

            return AccessController.doPrivileged(
                new PrivilegedAction<AppClassLoader>() {
                    public AppClassLoader run() {
                    URL[] urls = (s == null) ? new URL[0] : pathToURLs(path);
                    return new AppClassLoader(urls, extcl);
                }
            });
        }

        final URLClassPath ucp;

        AppClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent, factory);
            ucp = SharedSecrets.getJavaNetAccess().getURLClassPath(this);
            ucp.initLookupCache(this);
        }
        // 重写了父类ClassLoader的loadClass()方法
        public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            int i = name.lastIndexOf('.');
            if (i != -1) {
                SecurityManager sm = System.getSecurityManager();
                if (sm != null) {
                    sm.checkPackageAccess(name.substring(0, i));
                }
            }

            if (ucp.knownToNotExist(name)) {
                // The class of the given name is not found in the parent
                // class loader as well as its local URLClassPath.
                // Check if this class has already been defined dynamically;
                // if so, return the loaded class; otherwise, skip the parent
                // delegation and findClass.
                Class<?> c = findLoadedClass(name);
                if (c != null) {
                    if (resolve) {
                        resolveClass(c);
                    }
                    return c;
                }
                throw new ClassNotFoundException(name);
            }

            return (super.loadClass(name, resolve));
        }

        protected PermissionCollection getPermissions(CodeSource codesource)
        {
            PermissionCollection perms = super.getPermissions(codesource);
            perms.add(new RuntimePermission("exitVM"));
            return perms;
        }

        private void appendToClassPathForInstrumentation(String path) {
            assert(Thread.holdsLock(this));

            // addURL is a no-op if path already contains the URL
            super.addURL( getFileURL(new File(path)) );
        }


        private static AccessControlContext getContext(File[] cp)
            throws java.net.MalformedURLException
        {
            PathPermissions perms =
                new PathPermissions(cp);

            ProtectionDomain domain =
                new ProtectionDomain(new CodeSource(perms.getCodeBase(),
                    (java.security.cert.Certificate[]) null),
                perms);

            AccessControlContext acc =
                new AccessControlContext(new ProtectionDomain[] { domain });

            return acc;
        }
    }

    private static class BootClassPathHolder {
        static final URLClassPath bcp;
        static {
            URL[] urls;
            if (bootClassPath != null) {
                urls = AccessController.doPrivileged(
                    new PrivilegedAction<URL[]>() {
                        public URL[] run() {
                            File[] classPath = getClassPath(bootClassPath);
                            int len = classPath.length;
                            Set<File> seenDirs = new HashSet<File>();
                            for (int i = 0; i < len; i++) {
                                File curEntry = classPath[i];
                                // Negative test used to properly handle
                                // nonexistent jars on boot class path
                                if (!curEntry.isDirectory()) {
                                    curEntry = curEntry.getParentFile();
                                }
                                if (curEntry != null && seenDirs.add(curEntry)) {
                                    MetaIndex.registerDirectory(curEntry);
                                }
                            }
                            return pathToURLs(classPath);
                        }
                    }
                );
            } else {
                urls = new URL[0];
            }
            bcp = new URLClassPath(urls, factory, null);
            bcp.initLookupCache(null);
        }
    }

    public static URLClassPath getBootstrapClassPath() {
        return BootClassPathHolder.bcp;
    }

    private static URL[] pathToURLs(File[] path) {
        URL[] urls = new URL[path.length];
        for (int i = 0; i < path.length; i++) {
            urls[i] = getFileURL(path[i]);
        }
        // DEBUG
        //for (int i = 0; i < urls.length; i++) {
        //  System.out.println("urls[" + i + "] = " + '"' + urls[i] + '"');
        //}
        return urls;
    }

    private static File[] getClassPath(String cp) {
        File[] path;
        if (cp != null) {
            int count = 0, maxCount = 1;
            int pos = 0, lastPos = 0;
            // Count the number of separators first
            while ((pos = cp.indexOf(File.pathSeparator, lastPos)) != -1) {
                maxCount++;
                lastPos = pos + 1;
            }
            path = new File[maxCount];
            lastPos = pos = 0;
            // Now scan for each path component
            while ((pos = cp.indexOf(File.pathSeparator, lastPos)) != -1) {
                if (pos - lastPos > 0) {
                    path[count++] = new File(cp.substring(lastPos, pos));
                } else {
                    // empty path component translates to "."
                    path[count++] = new File(".");
                }
                lastPos = pos + 1;
            }
            // Make sure we include the last path component
            if (lastPos < cp.length()) {
                path[count++] = new File(cp.substring(lastPos));
            } else {
                path[count++] = new File(".");
            }
            // Trim array to correct size
            if (count != maxCount) {
                File[] tmp = new File[count];
                System.arraycopy(path, 0, tmp, 0, count);
                path = tmp;
            }
        } else {
            path = new File[0];
        }
        // DEBUG
        //for (int i = 0; i < path.length; i++) {
        //  System.out.println("path[" + i + "] = " + '"' + path[i] + '"');
        //}
        return path;
    }

    private static URLStreamHandler fileHandler;

    static URL getFileURL(File file) {
        try {
            file = file.getCanonicalFile();
        } catch (IOException e) {}

        try {
            return ParseUtil.fileToEncodedURL(file);
        } catch (MalformedURLException e) {
            // Should never happen since we specify the protocol...
            throw new InternalError(e);
        }
    }

    /*
     * The stream handler factory for loading system protocol handlers.
     */
    private static class Factory implements URLStreamHandlerFactory {
        private static String PREFIX = "sun.net.www.protocol";

        public URLStreamHandler createURLStreamHandler(String protocol) {
            String name = PREFIX + "." + protocol + ".Handler";
            try {
                Class<?> c = Class.forName(name);
                return (URLStreamHandler)c.newInstance();
            } catch (ReflectiveOperationException e) {
                throw new InternalError("could not load " + protocol +
                                        "system protocol handler", e);
            }
        }
    }
}

class PathPermissions extends PermissionCollection {
    // use serialVersionUID from JDK 1.2.2 for interoperability
    private static final long serialVersionUID = 8133287259134945693L;

    private File path[];
    private Permissions perms;

    URL codeBase;

    PathPermissions(File path[])
    {
        this.path = path;
        this.perms = null;
        this.codeBase = null;
    }

    URL getCodeBase()
    {
        return codeBase;
    }

    public void add(java.security.Permission permission) {
        throw new SecurityException("attempt to add a permission");
    }

    private synchronized void init()
    {
        if (perms != null)
            return;

        perms = new Permissions();

        // this is needed to be able to create the classloader itself!
        perms.add(SecurityConstants.CREATE_CLASSLOADER_PERMISSION);

        // add permission to read any "java.*" property
        perms.add(new java.util.PropertyPermission("java.*",
            SecurityConstants.PROPERTY_READ_ACTION));

        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                for (int i=0; i < path.length; i++) {
                    File f = path[i];
                    String path;
                    try {
                        path = f.getCanonicalPath();
                    } catch (IOException ioe) {
                        path = f.getAbsolutePath();
                    }
                    if (i == 0) {
                        codeBase = Launcher.getFileURL(new File(path));
                    }
                    if (f.isDirectory()) {
                        if (path.endsWith(File.separator)) {
                            perms.add(new FilePermission(path+"-",
                                SecurityConstants.FILE_READ_ACTION));
                        } else {
                            perms.add(new FilePermission(
                                path + File.separator+"-",
                                SecurityConstants.FILE_READ_ACTION));
                        }
                    } else {
                        int endIndex = path.lastIndexOf(File.separatorChar);
                        if (endIndex != -1) {
                            path = path.substring(0, endIndex+1) + "-";
                            perms.add(new FilePermission(path,
                                SecurityConstants.FILE_READ_ACTION));
                        } else {
                            // XXX?
                        }
                    }
                }
                return null;
            }
        });
    }

    public boolean implies(java.security.Permission permission) {
        if (perms == null)
            init();
        return perms.implies(permission);
    }

    public java.util.Enumeration<Permission> elements() {
        if (perms == null)
            init();
        synchronized (perms) {
            return perms.elements();
        }
    }

    public String toString() {
        if (perms == null)
            init();
        return perms.toString();
    }
}
