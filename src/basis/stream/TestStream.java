package src.basis.stream;

import src.basis.reflection.Young;

import java.io.*;
import java.util.*;

public class TestStream {
    public static void main(String[] args) throws Exception {
        List<Student> list = new ArrayList<>();
        Student s1 = new Student("001", "james");
        Student s2 = new Student("002", "tom", "0806");
        list.add(s1);
        list.add(s2);
        FileOutputStream fileOutputStream = new FileOutputStream("/Users/caoyang/caoyang_dev/stu");
        ObjectOutputStream oos = new ObjectOutputStream(fileOutputStream);
        oos.writeObject(list);

        ObjectInputStream ois = new ObjectInputStream(new FileInputStream("/Users/caoyang/caoyang_dev/stu"));
        List<Student> ls = (List<Student>) ois.readObject();
        for (Student stu : ls) {
            System.out.println(stu);
        }

        ls.forEach(stu -> {
            System.out.println(stu);
        });

        File file = new File("/Users/caoyang/caoyang_dev/properties");
        FileReader fr = new FileReader(file);
        Properties properties = new Properties();
        properties.load(fr);
        System.out.println(properties.getProperty("name") + " " + properties.getProperty("password"));


//        FileReader fileReader = new FileReader(
//                Thread.currentThread().getContextClassLoader()
//                        .getResource("properties/cfg.properties")
//                        .getPath()
//        );
//
//        properties.load(fileReader);
//        fileReader.close();

        InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("src/properties/fish.properties");

        properties.load(in);
        in.close();

        System.out.println(properties.get("user"));

        Class st = Class.forName(properties.getProperty("package"));
        System.out.println(st);

        Young stu = (Young) st.newInstance();
        stu.setName("stu");
        System.out.println(stu.getName());


        ResourceBundle rb = ResourceBundle.getBundle("src/properties/fish");
        System.out.println(rb.getString("user"));


    }
}

class Student implements Serializable {
    private static final long SerialVersionUID = 1L;
    String stuId;
    String name;
    transient String className;

    public Student() {
    }

    public Student(String stuId, String name) {
        this.stuId = stuId;
        this.name = name;
    }

    public Student(String stuId, String name, String className) {
        this.stuId = stuId;
        this.name = name;
        this.className = className;
    }

    @Override
    public String toString() {
        return "学生ID： " + stuId + " 学生姓名：" + name + " 学生班级：" + className;
    }
}
