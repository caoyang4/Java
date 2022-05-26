package src.jvm;

import java.io.*;

/**
 * 自定义类加载器
 * @author caoyang
 * @create 2022-05-25 23:26
 */
public class SelfClassLoader extends ClassLoader{
    private String path;

    public SelfClassLoader(String path) {
        this.path = path;
    }

    public SelfClassLoader(ClassLoader parent, String path) {
        super(parent);
        this.path = path;
    }

    @Override
    protected Class<?> findClass(String className) throws ClassNotFoundException {
        // class文件目录
        String classPath = path + className + ".class";
        BufferedInputStream bufferedInputStream = null;
        ByteArrayOutputStream baos = null;
        try {
            // 输入流
            bufferedInputStream = new BufferedInputStream(new FileInputStream(classPath));
            // 输出流
            baos = new ByteArrayOutputStream();
            byte[] data = new byte[1024];
            int len;
            // 读入字节流并写入
            while ((len = bufferedInputStream.read(data)) != -1){
                baos.write(data, 0, len);
            }
            // 获取字节码数据
            byte[] byteCodes = baos.toByteArray();
            // 将字节码数据转为 Class 对象
            return defineClass(null, byteCodes, 0, byteCodes.length);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (bufferedInputStream != null) bufferedInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void main(String[] args) throws ClassNotFoundException {
        String path = "/Users/caoyang/IdeaProjects/Java/com/sun/proxy/";
        SelfClassLoader loader = new SelfClassLoader(path);
        Class clz = loader.loadClass("$Proxy0");
        // 类对象：class com.sun.proxy.$Proxy0 的类加载器：src.jvm.SelfClassLoader
        System.out.println("类对象：" + clz + " 的类加载器：" + clz.getClassLoader().getClass().getName());
        // 当前类加载器的父加载器：sun.misc.Launcher$AppClassLoader
        System.out.println("当前类加载器的父加载器：" + clz.getClassLoader().getParent().getClass().getName());
    }
}
