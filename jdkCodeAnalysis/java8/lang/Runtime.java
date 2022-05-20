package java.lang;

import java.io.*;
import java.util.StringTokenizer;
import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;

/**
 * 每个 Java 应用程序都有一个Runtime类的单个实例，它允许应用程序与应用程序运行的环境进行交互，可以从getRuntime方法获取当前运行时信息
 *  Runtime类的大多数方法都是实例方法，必须针对当前运行时对象调用。
 */
public class Runtime {
    private static Runtime currentRuntime = new Runtime();
    // 返回与当前 Java 应用程序关联的运行时对象。
    public static Runtime getRuntime() {
        return currentRuntime;
    }

    private Runtime() {}
    // 通过启动其关闭序列来终止当前运行的 Java 虚拟机，status用于指定退出代码。
    // System.exit就是调用此方法
    public void exit(int status) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkExit(status);
        }
        Shutdown.exit(status);
    }

    // 注册一个新的虚拟机关闭钩子。
    // hook线程在Java 虚拟机关闭时被响应；
    // 虚拟机会等待这些钩子执行完才完成关闭，因此hook线程不建议执行长时间计算。
    // 响应事件：当最后一个非守护线程退出或调用exit
    public void addShutdownHook(Thread hook) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("shutdownHooks"));
        }
        ApplicationShutdownHooks.add(hook);
    }

    // 取消注册先前注册的虚拟机关闭挂钩
    public boolean removeShutdownHook(Thread hook) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("shutdownHooks"));
        }
        return ApplicationShutdownHooks.remove(hook);
    }
    // 强制终止当前运行的 Java 虚拟机。
    // 此方法不会导致关闭挂钩启动，并且不会运行未调用的终结器。
    public void halt(int status) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkExit(status);
        }
        Shutdown.beforeHalt();
        Shutdown.halt(status);
    }

    @Deprecated
    public static void runFinalizersOnExit(boolean value) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            try {
                security.checkExit(0);
            } catch (SecurityException e) {
                throw new SecurityException("runFinalizersOnExit");
            }
        }
        Shutdown.setRunFinalizersOnExit(value);
    }
    // 启动进程，执行命令
    public Process exec(String command) throws IOException {
        return exec(command, null, null);
    }

    public Process exec(String command, String[] envp) throws IOException {
        return exec(command, envp, null);
    }

    public Process exec(String command, String[] envp, File dir)
        throws IOException {
        if (command.length() == 0)
            throw new IllegalArgumentException("Empty command");

        StringTokenizer st = new StringTokenizer(command);
        String[] cmdarray = new String[st.countTokens()];
        for (int i = 0; st.hasMoreTokens(); i++)
            cmdarray[i] = st.nextToken();
        return exec(cmdarray, envp, dir);
    }

    public Process exec(String cmdarray[]) throws IOException {
        return exec(cmdarray, null, null);
    }

    public Process exec(String[] cmdarray, String[] envp) throws IOException {
        return exec(cmdarray, envp, null);
    }

    // 启动进程
    public Process exec(String[] cmdarray, String[] envp, File dir)
        throws IOException {
        return new ProcessBuilder(cmdarray)
            .environment(envp)
            .directory(dir)
            .start();
    }

    public native int availableProcessors();

    // 返回 Java 虚拟机中的可用内存量。 调用gc方法可能会导致freeMemory，返回的值增加freeMemory。
    // 当前可用于未来分配的对象的内存总量的近似值，以字节为单位
    public native long freeMemory();

    public native long totalMemory();

    public native long maxMemory();

    // 运行垃圾收集器。
    // 调用此方法表明 Java 虚拟机将努力回收未使用的对象，以使它们当前占用的内存可用于快速重用。
    // 当控制从方法调用返回时，虚拟机已尽最大努力回收所有丢弃的对象。
    // 尝试gc，也不一定会被执行，System.gc()就是调用此方法。
    public native void gc();

    /* Wormhole for calling java.lang.ref.Finalizer.runFinalization */
    private static native void runFinalization0();

    public void runFinalization() {
        runFinalization0();
    }

    public native void traceInstructions(boolean on);

    public native void traceMethodCalls(boolean on);

    @CallerSensitive
    public void load(String filename) {
        load0(Reflection.getCallerClass(), filename);
    }

    synchronized void load0(Class<?> fromClass, String filename) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkLink(filename);
        }
        if (!(new File(filename).isAbsolute())) {
            throw new UnsatisfiedLinkError("Expecting an absolute path of the library: " + filename);
        }
        ClassLoader.loadLibrary(fromClass, filename, true);
    }

    @CallerSensitive
    public void loadLibrary(String libname) {
        loadLibrary0(Reflection.getCallerClass(), libname);
    }

    synchronized void loadLibrary0(Class<?> fromClass, String libname) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkLink(libname);
        }
        if (libname.indexOf((int)File.separatorChar) != -1) {
            throw new UnsatisfiedLinkError("Directory separator should not appear in library name: " + libname);
        }
        ClassLoader.loadLibrary(fromClass, libname, false);
    }

    @Deprecated
    public InputStream getLocalizedInputStream(InputStream in) {
        return in;
    }

    @Deprecated
    public OutputStream getLocalizedOutputStream(OutputStream out) {
        return out;
    }

}
