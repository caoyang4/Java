package src.net.io.nio.reactor;

import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author caoyang
 */
public class Handler implements Runnable {
    private volatile static Selector selector;
    private final SocketChannel channel;
    private SelectionKey key;
    private volatile ByteBuffer input = ByteBuffer.allocate(1024);
    private volatile ByteBuffer output = ByteBuffer.allocate(1024);

    public Handler(SocketChannel channel) throws IOException {
        this.channel = channel;
        // 设置客户端连接为非阻塞模式
        channel.configureBlocking(false);
        // 为客户端创建一个新的多路复用器
        selector = Selector.open();
        // 注册客户端Channel的读事件
        key = channel.register(selector, SelectionKey.OP_READ);
    }

    @Override
    public void run() {
        try {
            while (selector.isOpen() && channel.isOpen()) {
                // 等待客户端事件发生
                Set<SelectionKey> keys = select();
                Iterator<SelectionKey> iterator = keys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    // 如果当前是读事件，则读取数据
                    if (key.isReadable()) {
                        read(key);
                    }
                    // 如果当前是写事件，则写入数据
                    else if (key.isWritable()) {
                        write(key);
                    }
                    iterator.remove();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 这里处理的主要目的是处理Jdk的一个bug，该bug会导致Selector被意外触发，但是实际上没有任何事件到达，
     * 此时的处理方式是新建一个Selector，然后重新将当前Channel注册到该Selector上
     * @return
     * @throws IOException
     */
    private Set<SelectionKey> select() throws IOException {
        selector.select();
        Set<SelectionKey> keys = selector.selectedKeys();
        if (keys.isEmpty()) {
            int interestOps = key.interestOps();
            selector = Selector.open();
            key = channel.register(selector, interestOps);
            return select();
        }
        return keys;
    }

    /**
     *  读取客户端发送的数据
     * @param key
     * @throws IOException
     */
    private void read(SelectionKey key) throws IOException {
        channel.read(input);
        if (input.position() == 0) {
            return;
        }
        input.flip();
        // 对读取的数据进行业务处理
        process();
        input.clear();
        // 读取完成后监听写入事件
        key.interestOps(SelectionKey.OP_WRITE);
    }

    private void write(SelectionKey key) throws IOException {
        output.flip();
        if (channel.isOpen()) {
            // 当有写入事件时，将业务处理的结果写入到客户端Channel中
            channel.write(output);
            key.channel();
            channel.close();
            output.clear();
        }
    }

    /**
     * 进行业务处理，并且获取处理结果。本质上，基于Reactor模型，如果这里成为处理瓶颈，
     * 则直接将其处理过程放入线程池即可，并且使用一个Future获取处理结果，最后写入客户端Channel
     */
    private void process() {
        byte[] bytes = new byte[input.remaining()];
        input.get(bytes);
        String message = new String(bytes, CharsetUtil.UTF_8);
        System.out.println("receive message from client: \n" + message);
        output.put("hello client".getBytes());
    }
}
