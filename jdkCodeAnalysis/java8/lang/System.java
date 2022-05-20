package java.lang;

import java.io.*;
import java.lang.reflect.Executable;
import java.lang.annotation.Annotation;
import java.security.AccessControlContext;
import java.util.Properties;
import java.util.PropertyPermission;
import java.util.StringTokenizer;
import java.util.Map;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.AllPermission;
import java.nio.channels.Channel;
import java.nio.channels.spi.SelectorProvider;
import sun.nio.ch.Interruptible;
import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;
import sun.security.util.SecurityConstants;
import sun.reflect.annotation.AnnotationType;

import jdk.internal.util.StaticProperty;

public final class System {

    private static native void registerNatives();
    static {
        registerNatives();
    }

    private System() {
    }
    // 标准输入流，默认为键盘
    public final static InputStream in = null;
    // 标识输出流，默认为控制台
    public final static PrintStream out = null;
    // 标准错误流，默认为控制台
    public final static PrintStream err = null;

    private static volatile SecurityManager security = null;

    public static void setIn(InputStream in) {
        checkIO();
        setIn0(in);
    }

    public static void setOut(PrintStream out) {
        checkIO();
        setOut0(out);
    }

    public static void setErr(PrintStream err) {
        checkIO();
        setErr0(err);
    }
    // 控制台
    private static volatile Console cons = null;
    public static Console console() {
         if (cons == null) {
             synchronized (System.class) {
                 cons = sun.misc.SharedSecrets.getJavaIOAccess().console();
             }
         }
         return cons;
     }

    public static Channel inheritedChannel() throws IOException {
        return SelectorProvider.provider().inheritedChannel();
    }

    private static void checkIO() {
        SecurityManager sm = getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("setIO"));
        }
    }

    private static native void setIn0(InputStream in);
    private static native void setOut0(PrintStream out);
    private static native void setErr0(PrintStream err);

    public static void setSecurityManager(final SecurityManager s) {
        try {
            s.checkPackageAccess("java.lang");
        } catch (Exception e) {
            // no-op
        }
        setSecurityManager0(s);
    }

    private static synchronized void setSecurityManager0(final SecurityManager s) {
        SecurityManager sm = getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("setSecurityManager"));
        }

        if ((s != null) && (s.getClass().getClassLoader() != null)) {
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    s.getClass().getProtectionDomain().implies
                        (SecurityConstants.ALL_PERMISSION);
                    return null;
                }
            });
        }

        security = s;
    }

    public static SecurityManager getSecurityManager() {
        return security;
    }

    public static native long currentTimeMillis();

    public static native long nanoTime();
    // 将源数组的子数组拷贝到目标数组中
    public static native void arraycopy(Object src,  int  srcPos, Object dest, int destPos, int length);
    // 计算指定对象的哈希值，null 对象为 0
    public static native int identityHashCode(Object x);

    private static Properties props;
    private static native Properties initProperties(Properties props);

    public static Properties getProperties() {
        SecurityManager sm = getSecurityManager();
        if (sm != null) {
            sm.checkPropertiesAccess();
        }

        return props;
    }

    public static String lineSeparator() {
        return lineSeparator;
    }

    private static String lineSeparator;

    public static void setProperties(Properties props) {
        SecurityManager sm = getSecurityManager();
        if (sm != null) {
            sm.checkPropertiesAccess();
        }
        if (props == null) {
            props = new Properties();
            initProperties(props);
        }
        System.props = props;
    }
    // 读取指定名称的系统属性
    public static String getProperty(String key) {
        checkKey(key);
        SecurityManager sm = getSecurityManager();
        if (sm != null) {
            sm.checkPropertyAccess(key);
        }

        return props.getProperty(key);
    }
    // 读取指定名称 key 的系统属性值，如果不存在，则使用默认值
    public static String getProperty(String key, String def) {
        checkKey(key);
        SecurityManager sm = getSecurityManager();
        if (sm != null) {
            sm.checkPropertyAccess(key);
        }

        return props.getProperty(key, def);
    }

    public static String setProperty(String key, String value) {
        checkKey(key);
        SecurityManager sm = getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new PropertyPermission(key,
                SecurityConstants.PROPERTY_WRITE_ACTION));
        }

        return (String) props.setProperty(key, value);
    }

    public static String clearProperty(String key) {
        checkKey(key);
        SecurityManager sm = getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new PropertyPermission(key, "write"));
        }

        return (String) props.remove(key);
    }

    private static void checkKey(String key) {
        if (key == null) {
            throw new NullPointerException("key can't be null");
        }
        if (key.equals("")) {
            throw new IllegalArgumentException("key can't be empty");
        }
    }
    // 读取指定名称的环境变量
    public static String getenv(String name) {
        SecurityManager sm = getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("getenv."+name));
        }

        return ProcessEnvironment.getenv(name);
    }


    public static java.util.Map<String,String> getenv() {
        SecurityManager sm = getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("getenv.*"));
        }

        return ProcessEnvironment.getenv();
    }

    public static void exit(int status) {
        Runtime.getRuntime().exit(status);
    }
    // 主动运行垃圾收集器，不建议调用
    public static void gc() {
        Runtime.getRuntime().gc();
    }

    public static void runFinalization() {
        Runtime.getRuntime().runFinalization();
    }

    @Deprecated
    public static void runFinalizersOnExit(boolean value) {
        Runtime.runFinalizersOnExit(value);
    }

    @CallerSensitive
    public static void load(String filename) {
        Runtime.getRuntime().load0(Reflection.getCallerClass(), filename);
    }

    @CallerSensitive
    public static void loadLibrary(String libname) {
        Runtime.getRuntime().loadLibrary0(Reflection.getCallerClass(), libname);
    }

    public static native String mapLibraryName(String libname);

    private static PrintStream newPrintStream(FileOutputStream fos, String enc) {
       if (enc != null) {
            try {
                return new PrintStream(new BufferedOutputStream(fos, 128), true, enc);
            } catch (UnsupportedEncodingException uee) {}
        }
        return new PrintStream(new BufferedOutputStream(fos, 128), true);
    }

    // 初始化系统类，在线程初始化后调用。
    private static void initializeSystemClass() {
        // VM 可能会在“props”初始化期间调用 JNU_NewStringPlatform() 来设置编码敏感属性（user.home、user.name、boot.class.path 等），
        // 在这些属性中它可能需要通过 System.getProperty() 访问，
        // 在初始化的早期阶段已经初始化（放入“props”）的相关系统编码属性。
        // 因此，请确保“props”在初始化的一开始就可用，并且所有系统属性都可以直接放入其中。
        props = new Properties();
        initProperties(props);  // initialized by the VM

        // 某些系统配置可能由 VM 选项控制，例如用于支持自动装箱的对象标识语义的最大直接内存量和整数缓存大小。
        // 通常，库将从 VM 设置的属性中获取这些值。如果属性仅供内部实现使用，则应从系统属性中删除这些属性。
        //   例如，参见 java.lang.Integer.IntegerCache 和 sun.misc.VM.saveAndRemoveProperties 方法。
        // 保存只能由内部实现访问的系统属性对象的私有副本。删除某些不打算供公众访问的系统属性。
        sun.misc.VM.saveAndRemoveProperties(props);

        lineSeparator = props.getProperty("line.separator");
        StaticProperty.jdkSerialFilter();   // Load StaticProperty to cache the property values
        sun.misc.Version.init();
        // 文件流
        FileInputStream fdIn = new FileInputStream(FileDescriptor.in);
        FileOutputStream fdOut = new FileOutputStream(FileDescriptor.out);
        FileOutputStream fdErr = new FileOutputStream(FileDescriptor.err);
        setIn0(new BufferedInputStream(fdIn));
        setOut0(newPrintStream(fdOut, props.getProperty("sun.stdout.encoding")));
        setErr0(newPrintStream(fdErr, props.getProperty("sun.stderr.encoding")));
        // 现在加载 zip 库，以防止 java.util.zip.ZipFile 稍后尝试使用自身加载此库。
        loadLibrary("zip");
        // 为 HUP、TERM 和 INT（如果可用）设置 Java 信号处理程序。
        Terminator.setup();
        // 初始化需要为类库设置的任何其他操作系统设置。
        // 目前，除了在使用 java.io 类之前设置了进程范围的错误模式的 Windows 之外，在任何地方都是无操作的。
        sun.misc.VM.initializeOSEnvironment();
        // 主线程不像其他线程那样添加到它的线程组中；我们必须在这里自己处理
        Thread current = Thread.currentThread();
        current.getThreadGroup().add(current);
        // 注册共享权限
        setJavaLangAccess();
        // 在初始化期间调用的子系统可以调用 sun.misc.VM.isBooted() 以避免执行应该等到应用程序类加载器设置完成的事情。
        // 重要提示：确保这仍然是最后的初始化操作！
        sun.misc.VM.booted();
    }

    private static void setJavaLangAccess() {
        // Allow privileged classes outside of java.lang
        sun.misc.SharedSecrets.setJavaLangAccess(new sun.misc.JavaLangAccess(){
            public sun.reflect.ConstantPool getConstantPool(Class<?> klass) {
                return klass.getConstantPool();
            }
            public boolean casAnnotationType(Class<?> klass, AnnotationType oldType, AnnotationType newType) {
                return klass.casAnnotationType(oldType, newType);
            }
            public AnnotationType getAnnotationType(Class<?> klass) {
                return klass.getAnnotationType();
            }
            public Map<Class<? extends Annotation>, Annotation> getDeclaredAnnotationMap(Class<?> klass) {
                return klass.getDeclaredAnnotationMap();
            }
            public byte[] getRawClassAnnotations(Class<?> klass) {
                return klass.getRawAnnotations();
            }
            public byte[] getRawClassTypeAnnotations(Class<?> klass) {
                return klass.getRawTypeAnnotations();
            }
            public byte[] getRawExecutableTypeAnnotations(Executable executable) {
                return Class.getExecutableTypeAnnotationBytes(executable);
            }
            public <E extends Enum<E>>
                    E[] getEnumConstantsShared(Class<E> klass) {
                return klass.getEnumConstantsShared();
            }
            public void blockedOn(Thread t, Interruptible b) {
                t.blockedOn(b);
            }
            public void registerShutdownHook(int slot, boolean registerShutdownInProgress, Runnable hook) {
                Shutdown.add(slot, registerShutdownInProgress, hook);
            }
            public int getStackTraceDepth(Throwable t) {
                return t.getStackTraceDepth();
            }
            public StackTraceElement getStackTraceElement(Throwable t, int i) {
                return t.getStackTraceElement(i);
            }
            public String newStringUnsafe(char[] chars) {
                return new String(chars, true);
            }
            public Thread newThreadWithAcc(Runnable target, AccessControlContext acc) {
                return new Thread(target, acc);
            }
            public void invokeFinalize(Object o) throws Throwable {
                o.finalize();
            }
        });
    }
}
