package src.net.netty;

import src.net.io.IOUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;

/**
 * @author caoyang
 */
public class NettyClient {

    private final ByteBuffer readHeader  = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
    private final ByteBuffer writeHeader = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
    private SocketChannel channel;

    public void sendMessage(byte[] body) throws Exception {
        // 创建客户端通道
        channel = SocketChannel.open();
        channel.socket().setSoTimeout(60000);
        channel.connect(new InetSocketAddress("127.0.0.1", IOUtils.PORT));

        // 客户端发请求
        writeWithHeader(channel, body);

        // 接收服务端响应的信息
        readHeader.clear();
        read(channel, readHeader);
        int bodyLen = readHeader.getInt(0);
        ByteBuffer bodyBuf = ByteBuffer.allocate(bodyLen).order(ByteOrder.BIG_ENDIAN);
        read(channel, bodyBuf);
        System.out.println("<客户端>收到响应内容: " + new String(bodyBuf.array(), "UTF-8") + ",长度:" + bodyLen);
    }

    private void writeWithHeader(SocketChannel channel, byte[] body) throws IOException {
        writeHeader.clear();
        writeHeader.putInt(body.length);
        writeHeader.flip();
        // channel.write(writeHeader);
        channel.write(ByteBuffer.wrap(body));
    }

    private void read(SocketChannel channel, ByteBuffer buffer) throws IOException {
        while (buffer.hasRemaining()) {
            int r = channel.read(buffer);
            if (r == -1) {
                throw new IOException("end of stream when reading header");
            }
        }
    }

    public static void main(String[] args) {
        try {
            NettyClient nettyClient = new NettyClient();
            String body = "Netty Client comes！";
            nettyClient.sendMessage(body.getBytes());
            String file = "/Users/caoyang/IdeaProjects/Java/IdeaProjects.iml";
            FileInputStream fis = new FileInputStream(file);
            byte[] bytes = new byte[1024];
            int r = 0;
            while ((r = fis.read(bytes)) != -1) {
                nettyClient.sendMessage(bytes);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
