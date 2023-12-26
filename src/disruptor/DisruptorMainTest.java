package src.disruptor;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * disruptor框架
 */
public class DisruptorMainTest {
    public static void main(String[] args) {
        OrderEvent orderEvent = new OrderEvent().newInstance();
        Disruptor disruptor = new Disruptor<>(
                orderEvent,
                1024 * 1024,
                command -> System.out.println("command"),
                // 枚举修改为多生产者
                ProducerType.MULTI,
                new YieldingWaitStrategy()
        );
        disruptor.handleEventsWithWorkerPool(new OrderEventHandler(), new OrderEventHandler());
        disruptor.start();
        RingBuffer<OrderEvent> ringBuffer = disruptor.getRingBuffer();
        OrderEventProducer eventProducer = new OrderEventProducer(ringBuffer);
        // 创建一个线程池，模拟多个生产者
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(100);
        for (int i = 0; i < 100; i++) {
            fixedThreadPool.execute(() -> eventProducer.onData(UUID.randomUUID().toString()));
        }

    }
    @Data
    static class OrderEvent implements EventFactory<OrderEvent> {

        private String msg;

        @Override
        public OrderEvent newInstance() {
            return new OrderEvent();
        }
    }
    static class OrderEventProducer {
        private final RingBuffer<OrderEvent> ringBuffer;
        public OrderEventProducer(RingBuffer<OrderEvent> ringBuffer) {
            this.ringBuffer = ringBuffer;
        }
        public void onData(String msg) {
            long sequence = ringBuffer.next();
            try {
                OrderEvent orderEvent = ringBuffer.getPublished(sequence);
                orderEvent.setMsg(msg);
            } finally {
                ringBuffer.publish(sequence);
            }
        }
    }
    @Slf4j
    static class OrderEventHandler implements EventHandler<OrderEvent>, WorkHandler<OrderEvent> {
        @Override
        public void onEvent(OrderEvent event, long sequence, boolean endOfBatch) {
            log.info("event: {}, sequence: {}, endOfBatch: {}", event, sequence, endOfBatch);
        }
        @Override
        public void onEvent(OrderEvent event) {
            log.info("event: {}", event);
        }
    }

}
