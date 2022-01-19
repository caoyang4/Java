package src.basis.reflection;

import java.lang.reflect.*;

/**
 * @author caoyang
 */
public class TestReflection {
    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchFieldException, NoSuchMethodException, InvocationTargetException {
        Class cls1 = Class.forName("java.lang.String");

        Class cls11 = "a".getClass();

        Class cls111 = String.class;

        String s = (String) cls1.newInstance();

        Class xxs = Class.forName("src.basis.reflection.Young");
        Object o = xxs.newInstance();

        System.out.println("反射获取属性");
        Field[] fields = xxs.getFields();
        System.out.println(fields[0].getName());

        for(Field f: xxs.getDeclaredFields()){
            System.out.print(f.getName()+"\t");
            System.out.print(Modifier.toString(f.getModifiers()) +"\t");
            System.out.println(f.getType());

        }

        Field field = xxs.getDeclaredField("no");
        field.set(o, 123);
        System.out.println(field.get(o));

        field = xxs.getDeclaredField("name");
        field.setAccessible(true);
        field.set(o, "xiaoMing");
        System.out.println(field.get(o));

        Young x = new Young();
        System.out.println(x.v1(1,2,3));

        System.out.println("反射获取方法");
        Method method = xxs.getDeclaredMethod("v1", int.class, int.class, int.class);
        Object r = method.invoke(o, 1, 2, 3);
        System.out.println(r);

        Constructor<Young> con = xxs.getConstructor(String.class);
        Young sx = con.newInstance("sss");
        System.out.println(sx.sex);


    }
}


