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

        Class xstu = Class.forName("src.basis.reflection.Young");
        Object o = xstu.newInstance();


//        Field[] fields = xstu.getFields();
//        System.out.println(fields[0].getName());
//
//        for(Field f: xstu.getDeclaredFields()){
//            System.out.print(f.getName()+"\t");
//            System.out.print(Modifier.toString(f.getModifiers()) +"\t");
//            System.out.println(f.getType());
//
//        }
//
//        Field field = xstu.getDeclaredField("no");
//        field.set(o, 123);
//        System.out.println(field.get(o));
//
//        field = xstu.getDeclaredField("name");
//        field.setAccessible(true);
//        field.set(o, "xiaowu");
//        System.out.println(field.get(o));

//        XStu x = new XStu();
//        System.out.println(x.v1(1,2,3));

        Method method = xstu.getDeclaredMethod("v1", int.class, int.class, int.class);
        Object r = method.invoke(o, 1, 2, 3);
        System.out.println(r);

        Constructor<Young> con = xstu.getConstructor(String.class);
        Young sx = con.newInstance("sss");
        System.out.println(sx.sex);


    }
}


