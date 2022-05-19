package java.lang;

import java.io.*;
import java.util.concurrent.TimeUnit;
// 创建进程
public abstract class Process {
    public abstract OutputStream getOutputStream();

    public abstract InputStream getInputStream();

    public abstract InputStream getErrorStream();

    public abstract int waitFor() throws InterruptedException;

    public boolean waitFor(long timeout, TimeUnit unit)
        throws InterruptedException
    {
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

    public abstract int exitValue();

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
