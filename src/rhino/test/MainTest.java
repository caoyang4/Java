package src.rhino.test;

import src.rhino.Rhino;
import src.rhino.threadpool.DefaultThreadPoolProperties;
import src.rhino.threadpool.ThreadPool;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * rhino测试
 * @author caoyang
 * @create 2022-05-30 16:12
 */
public class MainTest {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ThreadPool threadPool = Rhino.newThreadPool(UUID.randomUUID().toString());
        Future<String> future = threadPool.submit(new Callable() {
            @Override
            public Object call() throws Exception {
                return "rhino comes...";
            }
        });
        String result = future.get();
        System.out.println(result);
    }
}
