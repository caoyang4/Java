package src.net.io.nio;

import src.net.io.IOUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * NIO 是非阻塞的
 * NIO 面向块，I/O 面向流
 *
 * @author caoyang
 */
public class NIOServer {
    public static void main(String[] args) throws IOException {
        // 创建通道
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress("127.0.0.1", IOUtils.PORT));

        // 创建选择器
        Selector selector = Selector.open();
        // 将通道注册到选择器上
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        while (true) {
            // 监听事件
            selector.select();
            Set<SelectionKey> selectionKeySet = selector.selectedKeys();
            Iterator<SelectionKey> selectionKeyIterator = selectionKeySet.iterator();
            while (selectionKeyIterator.hasNext()) {
                SelectionKey selectionKey = selectionKeyIterator.next();
                if (selectionKey.isAcceptable()){
                    ServerSocketChannel channel = (ServerSocketChannel) selectionKey.channel();
                    SocketChannel socketChannel = channel.accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, SelectionKey.OP_READ);
                } else if (selectionKey.isReadable()) {
                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                    String data = readDataFromSocketChannel(socketChannel);
                    System.out.println(data);
                    socketChannel.close();
                }
                selectionKeyIterator.remove();
            }
        }
    }
    private static String readDataFromSocketChannel(SocketChannel socketChannel) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(IOUtils.BACK_LOG);
        StringBuilder data = new StringBuilder();
        while (true){
            byteBuffer.clear();
            int r = socketChannel.read(byteBuffer);
            if (r == -1){
                break;
            }
            byteBuffer.flip();
            int limit = byteBuffer.limit();
            char[] dst = new char[limit];
            for (int i = 0; i < limit; i++) {
                dst[i] = (char) byteBuffer.get(i);
            }
            data.append(dst);
            byteBuffer.clear();
        }
        return data.toString();
    }
}
