package src.jvm;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

/**
 * 方法区OOM
 * -XX:MetaspaceSize=256m
 * -XX:MaxMetaspaceSize=256m
 */
public class MethodAreaOOMTest2 extends ClassLoader{
    public static void main(String[] args) {
        int times = 0;
        MethodAreaOOMTest2 test = new MethodAreaOOMTest2();
        try {
            for(;;){
                // 用于生成二进制字节码
                ClassWriter  classWriter = new ClassWriter(0);
                classWriter.visit(
                        // 版本
                        Opcodes.V1_8,
                        // 权限修饰符
                        Opcodes.ACC_PUBLIC,
                        // 类名
                        "Class"+times,
                        null,
                        // 父类
                        "java/lang/Object",
                        // 接口
                        null
                );
                byte[] code = classWriter.toByteArray();
                test.defineClass("Class"+times++, code, 0, code.length);
            }
        } catch (OutOfMemoryError e){
            throw e;
        } finally {
            System.out.println("创建了"+times+"个对象之后，方法区发生OOM");
        }
    }
}

