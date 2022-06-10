package src.basis.io;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

/**
 * @author caoyang
 * @create 2022-06-10 18:31
 */
public class TestIO2 {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class User implements Serializable {
        Integer id;
        String name;
        // 密码保密
        transient String password;
    }

    @Test
    public void test1() throws Exception {
        List<User> list = new ArrayList<>();
        list.add(new User(23, "james", "james6&23"));
        list.add(new User(24, "kobe", "kobe8&24"));
        list.add(new User(23, "jordan", "jordan23&45"));
        String name = "/Users/caoyang/IdeaProjects/Java/src/basis/io/resource/users";
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(name))
        ){
            oos.writeObject(list);
        }
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(name))
        ){
            List<User> readList = (List<User>) ois.readObject();
            readList.forEach(System.out::println);
        }
    }

    @Test
    public void test2() throws Exception{
        String name = "/Users/caoyang/IdeaProjects/Java/src/basis/io/resource/io.properties";
        FileReader fr = new FileReader(name);
        Properties properties = new Properties();
        properties.load(fr);
        System.out.println(properties.keySet());
        System.out.println(properties);
    }

    @Test
    public void test3() throws Exception{
        String name = "/Users/caoyang/IdeaProjects/Java/src/basis/io/resource/io.properties";
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(name));
        byte[] bytes = new byte[64];
        int read;
        StringBuilder builder = new StringBuilder();
        while ((read = bis.read(bytes)) != -1){
            builder.append(new String(bytes, 0, read));
        }
        bis.close();
        System.out.println(builder);
    }

    @Test
    public void test4() throws Exception{
        String name = "/Users/caoyang/IdeaProjects/Java/src/basis/io/resource/buffer.txt";
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(name))){
            for (int i = 0; i < 10; i++) {
                String uuid = UUID.randomUUID().toString();
                bos.write(uuid.getBytes());
                bos.write("\n".getBytes());
            }
        }
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(name))){
            byte[] buffer = new byte[64];
            int read;
            StringBuilder builder = new StringBuilder();
            while ((read=bis.read(buffer)) != -1){
                builder.append(new String(buffer, 0, read));
            }
            System.out.println(builder);
        }
    }


}
