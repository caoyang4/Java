package src.net.io.nio;

import src.net.io.IOUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * NIO的3大组件: channel buffer selector
 * @author caoyang
 */
public class MultiThreadNIO {
    public static void main(String[] args) throws IOException {
        ThreadPoolExecutor pool = IOUtils.buildThreadPoolExecutor();
        ServerSocketChannel channel = ServerSocketChannel.open();
        channel.bind(new InetSocketAddress(IOUtils.PORT), IOUtils.BACK_LOG);
        System.out.println("start ServerSocketChannel ThreadPoolExecutor");
        while (true){
            SocketChannel socketChannel = channel.accept();
            pool.execute(() -> {
                try {
                    System.out.println(socketChannel.getRemoteAddress());
                    IOUtils.doWork();
                    socketChannel.write(ByteBuffer.wrap(IOUtils.buildHttpResponse().getBytes()));
                } catch (Exception e){
                    e.printStackTrace();
                }
            });
        }
    }
}
