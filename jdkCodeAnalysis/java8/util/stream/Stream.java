package java.util.stream;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.function.UnaryOperator;

/**
 * Java8支持的流处理的元素类型有4种，double、int，long和reference类型
 * Stream接口定义一些流基本操作，使用Stage的概念来描述一个完整的操作，将具有先后顺序的各个Stage连到一起，就构成了整个流水线
 * ⭐️⭐️⭐️流操作本质：
 *  Stream 中创建流和中间操作都会形成一个 AbstractPipeline，组成一个双向链表，每添加一个中间操作，就会在链表结尾新增一个 AbstractPipeline 节点。
 *
 * Stream流处理是一个来自数据源的元素队列并支持聚合操作。其包括的特征有：
 *   元素队列： 元素是特定类型的对象，形成一个队列。 Java中的Stream并不会存储元素，而是按需计算。
 *   数据源： 流的来源。 可以是集合，数组，I/O channel， 产生器generator，迭代器 等。
 *   聚合操作： 类似SQL语句一样的操作， 比如filter, map, reduce, find, match, sorted等
 *
 * 并行流的实现本质上就是在ForkJoin上进行了一层封装，将Stream 不断尝试分解成更小的split，然后使用fork/join 框架分而治之
 *
 * 与Collection集合操作不同， Stream操作还有两个基础的特征：
 *   Pipelining: 中间操作都会返回流对象本身。 这样多个操作可以串联成一个管道， 如同流式风格（fluent style）。 这样做可以对操作进行优化， 比如延迟执行(laziness)和短路( short-circuiting)。
 *   内部迭代： 以前对集合遍历都是通过Iterator或者For-Each的方式, 显式的在集合外部进行迭代， 这叫做外部迭代。 Stream提供了内部迭代的方式， 通过访问者模式(Visitor)实现。
 * 除了操作不同， 从实现角度比较， Stream和Collection也存在很多区别：
 *   不存储数据。 流不是一个存储元素的数据结构。 它只是传递源(source)的数据。
 *   功能性的(Functional in nature)。 在流上操作只是产生一个结果，不会修改源。 例如filter- 只是生成一个筛选后的stream，不会删除源里的元素。
 *   延迟搜索。 许多流操作， 如filter， map等，都是延迟执行。 中间操作总是lazy的。
 *   Stream可能是无界的。 而集合总是有界的(元素数量是有限大小)。 短路操作如limit(n) ， findFirst()可以在有限的时间内完成在无界的stream
 *   可消费的(Consumable)。 流的元素在流的声明周期内只能访问一次。 再次访问只能再重新从源中生成一个Stream
 *
 * Stream中的操作可以分为两大类：中间操作（Intermediate operations）与结束操作
 *   中间操作（Intermediate Operations）
 *     无状态（Stateless）操作：每个数据的处理是独立的，不会影响或依赖之前的数据。
 *        如filter()、flatMap()、flatMapToDouble()、flatMapToInt()、flatMapToLong()、map()、mapToDouble()、mapToInt()、mapToLong()、peek()、unordered() 等；
 *     有状态（Stateful）操作：处理时会记录状态，比如处理了几个。后面元素的处理会依赖前面记录的状态，或者拿到所有元素才能继续下去。
 *        如distinct()、sorted()、sorted(comparator)、limit()、skip() 等；
 *   结束操作（Terminal Operations）
 *     非短路操作：处理完所有数据才能得到结果。
 *        如collect()、count()、forEach()、forEachOrdered()、max()、min()、reduce()、toArray()等；
 *     短路操作（short-circuiting）操作：拿到符合预期的结果就会停下来，不一定会处理完所有数据。
 *        如anyMatch()、allMatch()、noneMatch()、findFirst()、findAny() 等。
 *
 *  不管无状态还是有状态的中间操作都为返回一个StatelessOp或者StatefulOp传递给下一个操作，有点像设计模式中的责任链模式。
 *
 * 流的生成方式：
 *   集合类的stream() 和 parallelStream()方法;
 *   数组Arrays.stream(Object[]);
 *   Stream类的静态工厂方法： Stream.of(Object[]), IntStream.range(int, int)， Stream.iterate(Object, UnaryOperator);
 *   通过StreamSupport辅助类从spliterator产生流.
 *
 *
 * Stream中的最终操作都是惰性的，是如何实现的呢。
 *   首先找到最后一个操作，也就是最终操作， 执行它的opWrapSink，事实上得到一个链表，最终返回第一个Sink,
 *   执行第一个Sink的accept将触发链式操作， 将管道中的操作在一个迭代中执行一次。
 *   事实上Java是将所有的操作形成一个类似链接的结构（通过Sink的downstream,upstream）,
 *   在遇到最终操作时触发链式反应， 通过各种数据类型特定的spliterator的一次迭代最终得到结果。
 *
 * 并行操作是通过ForkJoinTask框架实现
 */
public interface Stream<T> extends BaseStream<T, Stream<T>> {

    Stream<T> filter(Predicate<? super T> predicate);

    <R> Stream<R> map(Function<? super T, ? extends R> mapper);

    IntStream mapToInt(ToIntFunction<? super T> mapper);

    LongStream mapToLong(ToLongFunction<? super T> mapper);

    DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper);

    <R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper);

    IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper);

    LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper);

    DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper);

    Stream<T> distinct();

    Stream<T> sorted();

    Stream<T> sorted(Comparator<? super T> comparator);

    Stream<T> peek(Consumer<? super T> action);

    Stream<T> limit(long maxSize);

    Stream<T> skip(long n);

    void forEach(Consumer<? super T> action);

    void forEachOrdered(Consumer<? super T> action);

    Object[] toArray();

    <A> A[] toArray(IntFunction<A[]> generator);

    T reduce(T identity, BinaryOperator<T> accumulator);

    Optional<T> reduce(BinaryOperator<T> accumulator);

    <U> U reduce(U identity,
                 BiFunction<U, ? super T, U> accumulator,
                 BinaryOperator<U> combiner);

    <R> R collect(Supplier<R> supplier,
                  BiConsumer<R, ? super T> accumulator,
                  BiConsumer<R, R> combiner);

    <R, A> R collect(Collector<? super T, A, R> collector);

    Optional<T> min(Comparator<? super T> comparator);

    Optional<T> max(Comparator<? super T> comparator);

    long count();

    boolean anyMatch(Predicate<? super T> predicate);

    boolean allMatch(Predicate<? super T> predicate);

    boolean noneMatch(Predicate<? super T> predicate);

    Optional<T> findFirst();

    Optional<T> findAny();

    // Static factories

    public static<T> Builder<T> builder() {
        return new Streams.StreamBuilderImpl<>();
    }

    public static<T> Stream<T> empty() {
        return StreamSupport.stream(Spliterators.<T>emptySpliterator(), false);
    }

    public static<T> Stream<T> of(T t) {
        return StreamSupport.stream(new Streams.StreamBuilderImpl<>(t), false);
    }

    @SafeVarargs
    @SuppressWarnings("varargs") // Creating a stream from an array is safe
    public static<T> Stream<T> of(T... values) {
        return Arrays.stream(values);
    }

    public static<T> Stream<T> iterate(final T seed, final UnaryOperator<T> f) {
        Objects.requireNonNull(f);
        final Iterator<T> iterator = new Iterator<T>() {
            @SuppressWarnings("unchecked")
            T t = (T) Streams.NONE;

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public T next() {
                return t = (t == Streams.NONE) ? seed : f.apply(t);
            }
        };
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
                iterator,
                Spliterator.ORDERED | Spliterator.IMMUTABLE), false);
    }

    public static<T> Stream<T> generate(Supplier<T> s) {
        Objects.requireNonNull(s);
        return StreamSupport.stream(
                new StreamSpliterators.InfiniteSupplyingSpliterator.OfRef<>(Long.MAX_VALUE, s), false);
    }

    public static <T> Stream<T> concat(Stream<? extends T> a, Stream<? extends T> b) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);

        @SuppressWarnings("unchecked")
        Spliterator<T> split = new Streams.ConcatSpliterator.OfRef<>(
                (Spliterator<T>) a.spliterator(), (Spliterator<T>) b.spliterator());
        Stream<T> stream = StreamSupport.stream(split, a.isParallel() || b.isParallel());
        return stream.onClose(Streams.composedClose(a, b));
    }

    public interface Builder<T> extends Consumer<T> {

        @Override
        void accept(T t);

        default Builder<T> add(T t) {
            accept(t);
            return this;
        }

        Stream<T> build();

    }
}
