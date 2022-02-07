package src.net.io.nio;

import src.net.io.IOUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @author caoyang
 */
public class SingleThreadNIO {
    public static void main(String[] args) throws IOException, InterruptedException {
        ServerSocketChannel channel = ServerSocketChannel.open();
        channel.bind(new InetSocketAddress(IOUtils.PORT), IOUtils.BACK_LOG);
        System.out.println("start ServerSocketChannel");
        while (true){
            SocketChannel socketChannel = channel.accept();
            System.out.println(socketChannel.getRemoteAddress());
            IOUtils.doWork();
            socketChannel.write(ByteBuffer.wrap(IOUtils.buildHttpResponse().getBytes()));
        }
    }
}
