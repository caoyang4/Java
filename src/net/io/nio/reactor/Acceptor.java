package src.net.io.nio.reactor;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author caoyang
 */
public class Acceptor implements Runnable {
    private final ExecutorService executor = Executors.newFixedThreadPool(20);

    private final ServerSocketChannel serverSocket;

    public Acceptor(ServerSocketChannel serverSocket) {
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        try {
            // 获取客户端连接
            SocketChannel channel = serverSocket.accept();
            if (null != channel) {
                // 将客户端连接交由线程池处理
                executor.execute(new Handler(channel));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
