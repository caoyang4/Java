package src.net.io.nio;

import java.nio.ByteBuffer;

/**
 * @author caoyang
 */
public class BufferTest {
    public static void main(String[] args) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        System.out.println(byteBuffer);
        byteBuffer.putInt(1);
        System.out.println(byteBuffer);
        byteBuffer.putInt(2);
        System.out.println(byteBuffer);
        byteBuffer.putInt(3);
        System.out.println(byteBuffer);

        System.out.println("==============");

        // limit 移动至 position, position 移动至 0 位置
        byteBuffer.flip();
        System.out.println(byteBuffer);
        System.out.println("remaining " + byteBuffer.remaining());
        int x = byteBuffer.getInt();
        System.out.print(x + "\t");
        System.out.println(byteBuffer);
        // mark 移动到当前 position 位置
        byteBuffer.mark();
        int y = byteBuffer.getInt();
        System.out.print(y + "\t");
        System.out.println(byteBuffer);
        int z = byteBuffer.getInt();
        System.out.print(z + "\t");
        System.out.println(byteBuffer);
        System.out.println("remaining " + byteBuffer.remaining());

        // position 移动到当前 mark 位置
        byteBuffer.reset();
        System.out.println("after reset, remaining " + byteBuffer.remaining());
        int m = byteBuffer.getInt();
        System.out.print(m + "\t");
        System.out.println(byteBuffer);
        int n = byteBuffer.getInt();
        System.out.print(n + "\t");
        System.out.println(byteBuffer);

        System.out.println("============");
        byteBuffer.clear();
        System.out.println(byteBuffer);
    }
}
