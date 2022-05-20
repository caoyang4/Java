package java.lang;

import java.io.*;
import java.util.concurrent.TimeUnit;

/**
 * Process类提供了执行进程输入、执行输出到进程、等待进程完成、检查进程退出状态以及销毁（杀死）进程的方法
 */
public abstract class Process {
    // 连接到子进程的正常输入的输出流
    // 流的输出通过管道传输到此Process对象表示的Process的标准输入中。（向命令行中输入命令）
    // 如果子进程的标准输入已使用ProcessBuilder.redirectInput重定向，则此方法将返回空输出流
    public abstract OutputStream getOutputStream();
    // 连接到子进程正常输出的输入流
    // 该流从由此Process对象表示的进程的标准输出中获取管道数据。（读取命令行的输出的标准信息）
    // 如果子进程的标准输出已使用ProcessBuilder.redirectOutput重定向，则此方法将返回空输入流。
    // 否则，如果子流程的标准错误已使用ProcessBuilder.redirectErrorStream重定向，则此方法返回的输入流将接收合并的标准输出和子流程的标准错误。
    public abstract InputStream getInputStream();
    // 连接到子进程错误输出的输入流
    // 流从由此Process对象表示的进程的错误输出中获取管道数据。（读取命令行输出的错误信息）
    public abstract InputStream getErrorStream();
    // 使当前线程在必要时等待，直到由此Process对象表示的子Process终止，或者指定的等待时间过去。
    public abstract int waitFor() throws InterruptedException;

    public boolean waitFor(long timeout, TimeUnit unit) throws InterruptedException {
        long startTime = System.nanoTime();
        long rem = unit.toNanos(timeout);

        do {
            try {
                exitValue();
                return true;
            } catch(IllegalThreadStateException ex) {
                if (rem > 0)
                    Thread.sleep(Math.min(TimeUnit.NANOSECONDS.toMillis(rem) + 1, 100));
            }
            rem = unit.toNanos(timeout) - (System.nanoTime() - startTime);
        } while (rem > 0);
        return false;
    }
    // 子进程的退出值
    public abstract int exitValue();
    // 杀死子进程。
    // 此Process对象表示的子Process是否被强制终止取决于实现。
    public abstract void destroy();

    public Process destroyForcibly() {
        destroy();
        return this;
    }

    public boolean isAlive() {
        try {
            exitValue();
            return false;
        } catch(IllegalThreadStateException e) {
            return true;
        }
    }
}
