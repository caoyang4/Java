package src.net.socket.tcp;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 服务端
 * @author caoyang
 */
public class Server {
    public static void main(String[] args)
    {
        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        BufferedReader in = null;
        int port = 5000;
        String info = null;
        try {
            // 创建服务端
            serverSocket = new ServerSocket(port);
            System.out.println("服务端已开启，等待客户端...");
            // 监听客户端
            clientSocket = serverSocket.accept();
            // 接收客户端发送的内容
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            while(true) {
                info = in.readLine();
                System.out.println("服务端收到：" + info);
                Thread.sleep(2000);
            }
        }
        catch(IOException | InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
