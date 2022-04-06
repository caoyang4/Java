package src.juc.atomic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author caoyang
 */
public class TestAtomicIntegerArray {

    static <T> void demo(Supplier<T> arraySupplier,
                         Function<T, Integer> function,
                         BiConsumer<T, Integer> consumer,
                         Consumer<T> printConsumer){
        List<Thread> threadList = new ArrayList<>();
        T array = arraySupplier.get();
        int length = function.apply(array);
        for (int i = 0; i < length; i++) {
            threadList.add(
              new Thread(() -> {
                  for (int j = 0; j < 10000; j++) {
                    consumer.accept(array, j % length);
                  }
              })
            );
        }
        threadList.forEach(thread -> thread.start());
        threadList.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        printConsumer.accept(array);
    }

    public static void main(String[] args) {
        demo(
                () -> new int[10],
                array -> array.length,
                (array, index) -> array[index]++,
                array -> System.out.println(Arrays.toString(array))
        );


        demo(
                () -> new AtomicIntegerArray(10),
                atomicIntegerArray -> atomicIntegerArray.length(),
                (atomicIntegerArray, index) -> atomicIntegerArray.getAndIncrement(index),
                atomicIntegerArray -> System.out.println(atomicIntegerArray)
        );
    }
}
