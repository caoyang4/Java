package src.net.io.bio;

import src.net.io.IOUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author caoyang
 */
public class MultiThreadBIO {
    public static void main(String[] args) throws IOException {
        ThreadPoolExecutor threadPoolExecutor = IOUtils.buildThreadPoolExecutor();
        ServerSocket serverSocket = new ServerSocket(IOUtils.PORT);
        System.out.println("start ServerSocket ThreadPoolExecutor");
        while (true){
            threadPoolExecutor.execute(() -> {
                try {
                    Socket socket = serverSocket.accept();
                    System.out.println(socket.getRemoteSocketAddress());
                    OutputStream oos = socket.getOutputStream();
                    BufferedWriter bufferedWriter = IOUtils.buildBufferedWriter(oos);
                    IOUtils.doWork();
                    bufferedWriter.write(IOUtils.buildHttpResponse());
                    bufferedWriter.flush();
                } catch (Exception e){
                    e.printStackTrace();
                }
            });
        }
    }
}
