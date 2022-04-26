package src.basis.classLoad;

import sun.misc.Launcher;

import java.net.URL;

/**
 * @author caoyang
 */
public class TestClassLoader2 {
    public static void main(String[] args) {
        // 系统类加载器
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        // sun.misc.Launcher$AppClassLoader
        System.out.println(systemClassLoader);

        // 扩展类加载器
        ClassLoader extClassLoader = systemClassLoader.getParent();
        // sun.misc.Launcher$ExtClassLoader
        System.out.println(extClassLoader);

        ClassLoader bootstrapClassLoader = extClassLoader.getParent();
        // null
        System.out.println(bootstrapClassLoader);

        // sun.misc.Launcher$AppClassLoader
        ClassLoader classLoader = TestClassLoader2.class.getClassLoader();
        System.out.println(classLoader);

        // null java核心类库是由启动类加载器（BootstrapClassLoader）进行加载
        ClassLoader objClassLoader = Object.class.getClassLoader();
        System.out.println(objClassLoader);

        URL[] urls = Launcher.getBootstrapClassPath().getURLs();
        for (URL url : urls) {
            System.out.println(url);
        }
        System.out.println();
        String property = System.getProperty("java.ext.dirs");
        for (String s : property.split(":")) {
            System.out.println(s);
        }

    }
}
