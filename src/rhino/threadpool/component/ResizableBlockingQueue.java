package src.rhino.threadpool.component;

import java.util.concurrent.BlockingQueue;

public interface ResizableBlockingQueue<E> extends BlockingQueue<E> {

    int getCapacity();

    void setCapacity(int capacity);
}
