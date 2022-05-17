package java.util.stream;

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Supplier;

/**
 * 不可变类
 * 用于生成并返回 stream 对象，Java8支持的流处理的元素类型有4种，double、int，long和reference类型
 */
public final class StreamSupport {

    // Suppresses default constructor, ensuring non-instantiability.
    private StreamSupport() {}

    public static <T> Stream<T> stream(Spliterator<T> spliterator, boolean parallel) {
        Objects.requireNonNull(spliterator);
        return new ReferencePipeline.Head<>(spliterator,
                                            StreamOpFlag.fromCharacteristics(spliterator),
                                            parallel);
    }

    public static <T> Stream<T> stream(Supplier<? extends Spliterator<T>> supplier, int characteristics, boolean parallel) {
        Objects.requireNonNull(supplier);
        return new ReferencePipeline.Head<>(supplier,
                                            StreamOpFlag.fromCharacteristics(characteristics),
                                            parallel);
    }

    public static IntStream intStream(Spliterator.OfInt spliterator, boolean parallel) {
        return new IntPipeline.Head<>(spliterator,
                                      StreamOpFlag.fromCharacteristics(spliterator),
                                      parallel);
    }

    public static IntStream intStream(Supplier<? extends Spliterator.OfInt> supplier, int characteristics,
                                      boolean parallel) {
        return new IntPipeline.Head<>(supplier,
                                      StreamOpFlag.fromCharacteristics(characteristics),
                                      parallel);
    }

    public static LongStream longStream(Spliterator.OfLong spliterator, boolean parallel) {
        return new LongPipeline.Head<>(spliterator,
                                       StreamOpFlag.fromCharacteristics(spliterator),
                                       parallel);
    }

    public static LongStream longStream(Supplier<? extends Spliterator.OfLong> supplier, int characteristics, boolean parallel) {
        return new LongPipeline.Head<>(supplier,
                                       StreamOpFlag.fromCharacteristics(characteristics),
                                       parallel);
    }

    public static DoubleStream doubleStream(Spliterator.OfDouble spliterator, boolean parallel) {
        return new DoublePipeline.Head<>(spliterator,
                                         StreamOpFlag.fromCharacteristics(spliterator),
                                         parallel);
    }

    public static DoubleStream doubleStream(Supplier<? extends Spliterator.OfDouble> supplier, int characteristics, boolean parallel) {
        return new DoublePipeline.Head<>(supplier,
                                         StreamOpFlag.fromCharacteristics(characteristics),
                                         parallel);
    }
}
