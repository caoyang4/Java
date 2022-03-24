package src.net.io.bio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

/**
 * @author caoyang
 */
public class SocketClientThread{
    public static void main(String[] args) throws InterruptedException {
        Integer clientNumber = 20;
        CountDownLatch latch = new CountDownLatch(20);
        for (int i = 1; i <= clientNumber; i++) {
            latch.countDown();
            ClientThread clientThread = new ClientThread(latch, i);
            new Thread(clientThread).start();
        }
        // 此处只是保证守护线程在启动所有线程后，进入等待状态
        synchronized (SocketClientThread.class) {
            SocketClientThread.class.wait();
        }
    }
}

 class ClientThread implements Runnable{


    private CountDownLatch latch;
    private Integer clientIdx;

    public ClientThread(CountDownLatch latch, Integer clientIdx) {
        this.latch = latch;
        this.clientIdx = clientIdx;
    }



    @Override
    public void run() {
        Socket socket = null;
        OutputStream clientRequest = null;
        InputStream clientResponse = null;
        try {
           socket  = new Socket("127.0.0.1", 5000);
           clientRequest = socket.getOutputStream();
           clientResponse = socket.getInputStream();

            this.latch.await();
            String msg = "client【"+this.clientIdx+"】comes";
            clientRequest.write(msg.getBytes());
            clientRequest.flush();
            final int maxLen = 1024;
            byte[] bytes = new byte[maxLen];
            int realLen;
            StringBuilder response = new StringBuilder();
            while ((realLen = clientResponse.read(bytes, 0, maxLen)) != -1){
                response.append(new String(bytes, 0, realLen));
            }
            System.out.println(response);



        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (clientRequest != null){
                    clientRequest.close();
                }
                if (clientResponse != null){
                    clientResponse.close();
                }
                if (socket != null){
                    socket.close();
                }
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
