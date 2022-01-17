package src.socket.tcp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author caoyang
 */
public class UserServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8000);
        while (true) {
            Socket clintSocket = serverSocket.accept();
            invoke(clintSocket);
        }
    }

    public static void invoke(final Socket socket){
        new Thread(() -> {
            ObjectInputStream ois = null;
            ObjectOutputStream oos = null;
            try{
                ois = new ObjectInputStream(socket.getInputStream());
                oos = new ObjectOutputStream(socket.getOutputStream());
                User user = (User) ois.readObject();
                System.out.println("收到客户端数据：");
                System.out.println("\t" + user);
                user.setName(user.getName() + "GoodBoy");
                user.setChineseName(user.getChineseName() + "漂亮男孩");
                oos.writeObject(user);
                oos.flush();
            } catch (IOException | ClassNotFoundException e){
                e.printStackTrace();
            }finally {
                try {
                    oos.close();
                    ois.close();
                    socket.close();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
