package src.db.redis;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 自定义 redis RESP 通信协议
 * （simple String）简单字符串类型，用+开头
 * （Bulk String）多行字符串，用$开头
 * （Errors）错误类型，用-开头
 * （Integer）整型，用:开头
 * （Array） 数组，用*开头
 * @author caoyang
 * @create 2022-06-18 21:54
 */
public class RedisResp {
    static final String HOST = "127.0.0.1";
    static final int PORT = 6379;
    static Socket socket;
    static PrintWriter writer;
    static BufferedReader reader;
    public static void main(String[] args) throws IOException {
        // 1、建立连接
        try {
            socket = new Socket(HOST, PORT);
            // 2、输入输出流
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(),StandardCharsets.UTF_8));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(),StandardCharsets.UTF_8));

            // 3、发送请求
            sendRequest("get", "name");
            // 4、解析响应
            Object obj = handleResponse();
//            System.out.println("obj = " + obj);
            // 5、关闭连接
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null) socket.close();
        }

    }

    private static Object handleResponse() throws IOException {
        int prefix = reader.read();
        switch (prefix) {
            case '+':
                return reader.readLine();
            case '-':
                throw new RuntimeException(reader.readLine());
            case ':':
                return Long.parseLong(reader.readLine());
            case '$': // 多行字符串
                int len = Integer.parseInt(reader.readLine());
                if (len == -1) return null;
                if (len == 0) return "";
                // 简化只读行
                return reader.readLine();
            case '*':
                return readBulkString();
            default:
                throw new RuntimeException("wrong data format!");
        }
    }

    private static Object readBulkString() throws IOException {
        int len = Integer.parseInt(reader.readLine());
        if (len <= 0) return null;
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            list.add(handleResponse());
        }
        return list;
    }

    private static void sendRequest(String ... args) {
        writer.println("*" + args.length);
        for (String arg : args) {
            writer.println("$" + arg.getBytes(StandardCharsets.UTF_8).length);
            writer.println(arg);
        }
        writer.flush();
    }
}
