package src.socket.udp;

import java.io.IOException;
import java.net.*;

/**
 * @author caoyang
 */
public class Client {
    public static void main(String[] args) throws IOException {
        String msg = "user:young password:admin123";
        byte[] data = msg.getBytes();
        InetAddress address = InetAddress.getByName("localhost");
        // 数据包
        DatagramPacket datagramPacket = new DatagramPacket(data, data.length, address, 8000);
        DatagramSocket socket = new DatagramSocket();
        System.out.println("客户端发送消息：" + msg);
        socket.send(datagramPacket);

        byte[] info = new byte[1024];
        DatagramPacket datagramPacket1 = new DatagramPacket(info, info.length);
        socket.receive(datagramPacket1);
        String rec  = new String(datagramPacket1.getData(), 0, datagramPacket1.getLength());
        System.out.println("客户端获取消息："+rec);

        socket.close();

    }
}
