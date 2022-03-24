package src.net.io.bio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author caoyang
 */
public class SocketServerThread {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(5000);
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                ServerThread serverThread = new ServerThread(socket);
                new Thread(serverThread).start();
            }
        } finally {
            serverSocket.close();
        }
    }

}
class ServerThread implements Runnable{
    private Socket socket;
    public ServerThread(Socket socket) {
        this.socket = socket;
    }


    @Override
    public void run() {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = socket.getInputStream();
            out = socket.getOutputStream();
            Integer port = socket.getPort();
            final int maxLen = 1024;
            byte[] contextBytes = new byte[maxLen];
            int realLen = in.read(contextBytes, 0, maxLen);
            //读取信息
            String message = new String(contextBytes , 0 , realLen);
            System.out.println("服务端收到来自于端口【" + port + "】的信息: " + message);
            out.write("服务端给你刷个火箭！！！".getBytes());
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            try {
                if (in != null){
                    in.close();
                }
                if (out != null){
                    in.close();
                }
                socket.close();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}