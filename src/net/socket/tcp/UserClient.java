package src.net.socket.tcp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * @author caoyang
 */
public class UserClient {
    static Map<String, String> userMap = new HashMap<>();
    static {
        userMap.put("james", "詹姆斯");
        userMap.put("young", "彦祖");
        userMap.put("kobe", "科比");
        userMap.put("yaoming", "姚明");
        userMap.put("jack", "老马");
        userMap.put("pony", "小马");
        userMap.put("kevin", "小帅");
    }
    public static void main(String[] args) {
        for (String key: userMap.keySet()) {
            Socket socket = null;
            ObjectOutputStream oos = null;
            ObjectInputStream ois = null;
            try {
                socket = new Socket("localhost", 8000);
                oos = new ObjectOutputStream(socket.getOutputStream());
                User user = new User(key, userMap.get(key));
                oos.writeObject(user);
                oos.flush();
                ois = new ObjectInputStream(socket.getInputStream());
                Object o = ois.readObject();
                System.out.println("收到服务端数据：");
                if (o != null) {
                    user = (User) o;
                    System.out.println("\t" + user);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    ois.close();
                    oos.close();
                    socket.close();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
}
