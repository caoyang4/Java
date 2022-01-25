package src.socket.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * @author caoyang
 */
public class Server {
    public static void main(String[] args) throws IOException {
        DatagramSocket datagramSocket = new DatagramSocket(8000);
        byte[] data = new byte[1024];
        DatagramPacket datagramPacket = new DatagramPacket(data, data.length);
        System.out.println("服务端启动...");

        // 接收数据
        datagramSocket.receive(datagramPacket);
        String msg = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
        System.out.println("服务端获取消息：" + msg);

        // 发送数据
        String info = "welcome...";
        byte[] send_data = info.getBytes();
        InetAddress address = datagramPacket.getAddress();
        int port = datagramPacket.getPort();
        DatagramPacket datagramPacket1 = new DatagramPacket(send_data, send_data.length, address, port);
        datagramSocket.send(datagramPacket1);
        System.out.println("服务端发送消息：" + info);

        // 关闭 socket
        datagramSocket.close();


    }
}
