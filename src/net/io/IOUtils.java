package src.net.io;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author caoyang
 */
public class IOUtils {
    private static final String HTTP_SEP = "\r\n";
    public static final int PORT = 8000;
    public static final int BACK_LOG = 1024;

    public static String buildHttpResponse(){
        StringBuilder stringBuilder = new StringBuilder();
        String symbol = "hello Java";
        String body = "<h1>" + symbol + "</h1>";
        stringBuilder.append("HTTP/1.1 200 OK").append(HTTP_SEP);
        stringBuilder.append("connection: Close").append(HTTP_SEP);
        stringBuilder.append("content-type: text/html").append(HTTP_SEP);
        stringBuilder.append("content-length: ").append(body.length()).append(HTTP_SEP);
        stringBuilder.append(HTTP_SEP);
        stringBuilder.append(body);
        return stringBuilder.toString();
    }

    public static BufferedWriter buildBufferedWriter(OutputStream oos){
        return new BufferedWriter(new OutputStreamWriter(oos));
    }

    /**
     * 线程池
     */
    public static ThreadPoolExecutor buildThreadPoolExecutor(){
        return new ThreadPoolExecutor(
                100,
                100,
                0,
                TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    public static void doWork() throws InterruptedException {
        Thread.sleep(1000);
        System.out.println("welcome to java net...");
    }
}
