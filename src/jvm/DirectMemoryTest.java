package src.jvm;

import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * 直接内存，即堆外内存
 * -XX:MaxDirectMemorySize=256m
 * 设置直接内存 256M，不指定，默认和堆的最大内存一致，即-Xmx参数
 */
@Slf4j(topic = "DirectMemoryTest")
public class DirectMemoryTest {
    static List<ByteBuffer> list = new ArrayList<>();
    static final int BUFFER_SIZE = 1024 * 1024 * 20;
    public static void main(String[] args) {
        int times = 0;
        try {
            for (;;){
                // 内部通过调用unsafe.allocateMemory(size)分配直接内存
                ByteBuffer byteBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
                list.add(byteBuffer);
                times++;
            }
        } catch (OutOfMemoryError e){
            throw e;
        } finally {
            log.info("Direct buffer memory happens OOM, times: {}", times);
        }
    }
}
