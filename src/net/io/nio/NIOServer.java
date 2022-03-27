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
 * 在 Java NIO 中的通道（Channel）就相当于操作系统的内核空间（kernel space）的缓冲区。
 * 缓冲区（Buffer）对应的相当于操作系统的用户空间（user space）中的用户缓冲区（user buffer）。
 * 通道（Channel）是全双工的（双向传输），它既可能是读缓冲区（read buffer），也可能是网络缓冲区（socket buffer）。
 * 缓冲区（Buffer）分为堆内存（HeapBuffer）和堆外内存（DirectBuffer），这是通过 malloc() 分配出来的用户态内存。
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
