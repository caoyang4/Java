package src.net.io.nio;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 通道
 *
 * 通道 Channel 是对原 I/O 包中的流的模拟，可以通过它读取和写入数据。
 * 通道与流的不同之处在于，流只能在一个方向上移动(一个流必须是 InputStream 或者 OutputStream 的子类)，而通道是双向的，可以用于读、写或者同时用于读写。
 * 通道包括以下类型:
 *  FileChannel: 从文件中读写数据；
 *  DatagramChannel: 通过 UDP 读写网络中数据；
 *  SocketChannel: 通过 TCP 读写网络中数据；
 *  ServerSocketChannel: 可以监听新进来的 TCP 连接，对每一个新进来的连接都会创建一个 SocketChannel。
 * @author caoyang
 */
public class ChannelTest {
    public static void copyFile(String src, String dst) throws IOException {
        FileInputStream srcFile = new FileInputStream(src);
        FileChannel input = srcFile.getChannel();

        FileOutputStream dstFile = new FileOutputStream(dst);
        FileChannel output = dstFile.getChannel();

        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        while (true){
            /* 从输入通道中读取数据到缓冲区中 */
            int r = input.read(byteBuffer);
            /* read() 返回 -1 表示 EOF */
            if (r == -1){
                break;
            }
            /* 切换读写 */
            byteBuffer.flip();
            output.write(byteBuffer);
            /* 清空缓冲区 */
            byteBuffer.clear();
        }
    }

    public static void main(String[] args) throws IOException {
        String src = "/Users/caoyang/tmp/Math.java";
        String dst = "/Users/caoyang/tmp/MathBk.java";
        copyFile(src, dst);
    }
}
