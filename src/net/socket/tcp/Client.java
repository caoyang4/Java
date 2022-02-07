package src.net.socket.tcp;

import java.io.*;
import java.net.Socket;
import java.util.UUID;

/**
 * 客户端
 * @author caoyang
 */
public class Client {
        public static void main(String[] args) {
            Socket socket = null;
            PrintWriter pw = null;
            // 服务端ip
            String serverIp = "127.0.0.1";
            // 端口
            int port=5000;
            try
            {
                socket = new Socket(serverIp, port);
                pw = new PrintWriter(socket.getOutputStream(),true);
                while (true) {
                    UUID uuid = UUID.randomUUID();
                    System.out.println("客户端发送：" + uuid);
                    // 向服务端发送数据
                    pw.println(uuid);
                    Thread.sleep(2000);
                }
            } catch(IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
}
