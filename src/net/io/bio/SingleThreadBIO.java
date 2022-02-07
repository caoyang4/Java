package src.net.io.bio;

import src.net.io.IOUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author caoyang
 */
public class SingleThreadBIO {
    public static void main(String[] args) throws IOException, InterruptedException {
        ServerSocket serverSocket = new ServerSocket(IOUtils.PORT);
        System.out.println("start serverSocket");
        while(true) {
            Socket socket = serverSocket.accept();
            System.out.println(socket.getRemoteSocketAddress());
            OutputStream oos = socket.getOutputStream();
            BufferedWriter bufferedWriter = IOUtils.buildBufferedWriter(oos);
            IOUtils.doWork();
            bufferedWriter.write(IOUtils.buildHttpResponse());
            bufferedWriter.flush();
        }
    }

}
