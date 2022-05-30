package src.rhino.threadpool.component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

public enum QueueType {
    SELF_DEFINITION {
        @Override
        public BlockingQueue buildQueue(int capacity) {
            throw new IllegalArgumentException("不支持创建自定义队列类型");
        }
    },
    SYNCHRONOUS_QUEUE {
        @Override
        public BlockingQueue buildQueue(int capacity) {
            return new SynchronousQueue();
        }

    },
    LINKED_BLOCKING_QUEUE {
        @Override
        public BlockingQueue buildQueue(int capacity) {
            return new ResizableLinkedBlockingQueue(capacity);
        }

    },
    PRIORITY_BLOCKING_QUEUE {
        @Override
        public BlockingQueue buildQueue(int capacity) {
            return new ResizablePriorityBlockingQueue(capacity);
        }

    };


    public static boolean isResizableQueue(QueueType queueType) {
        return LINKED_BLOCKING_QUEUE.equals(queueType) || PRIORITY_BLOCKING_QUEUE.equals(queueType);
    }

    public abstract BlockingQueue buildQueue(int capacity);
}
