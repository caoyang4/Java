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
 * @author caoyang
 */
public class SelectorNIO {
    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        /*
        * 通道必须配置为非阻塞模式，否则使用选择器就没有任何意义了，
        * 因为如果通道在某个事件上被阻塞，那么服务器就不能响应其它事件，
        * 必须等待这个事件处理完毕才能去处理其它事件，显然这和选择器的作用背道而驰
        * */
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(IOUtils.PORT), IOUtils.BACK_LOG);
        Selector selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("start ServerSocketChannel with Selector");
        while (true){
            // 监听事件
            selector.select();
            Set<SelectionKey> selectionKeySet = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeySet.iterator();
            while (iterator.hasNext()){
                SelectionKey selectionKey = iterator.next();
                if(selectionKey.isAcceptable()){
                    System.out.println("Acceptable事件");
                    ServerSocketChannel channel = (ServerSocketChannel) selectionKey.channel();
                    SocketChannel socketChannel = channel.accept();
                    System.out.println(socketChannel.getRemoteAddress());
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, SelectionKey.OP_WRITE);
                } else if(selectionKey.isWritable()){
                    System.out.println("Writable事件");
                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                    try {
                            IOUtils.doWork();
                            socketChannel.write(ByteBuffer.wrap(IOUtils.buildHttpResponse().getBytes()));
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                iterator.remove();
            }
        }
    }
}
