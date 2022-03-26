package src.net.io.nio;

import src.net.io.IOUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @author caoyang
 */
public class NIOClient {
    public static void main(String[] args) throws IOException, InterruptedException {
        Socket socket = new Socket("127.0.0.1", IOUtils.PORT);
        OutputStream oos = socket.getOutputStream();
        oos.write("\nbegin\n".getBytes());
        String file = "/Users/caoyang/IdeaProjects/Java/IdeaProjects.iml";
        FileInputStream fis = new FileInputStream(file);
        byte[] bytes = new byte[1024];
        int r = 0;
        while ((r = fis.read(bytes)) != -1) {
            oos.write(bytes);
        }
        oos.write("\nover".getBytes());
        oos.close();
    }
}
