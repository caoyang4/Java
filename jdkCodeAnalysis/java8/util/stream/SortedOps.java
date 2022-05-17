package java.util.stream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.IntFunction;


final class SortedOps {

    private SortedOps() { }

    static <T> Stream<T> makeRef(AbstractPipeline<?, T, ?> upstream) {
        return new OfRef<>(upstream);
    }

    static <T> Stream<T> makeRef(AbstractPipeline<?, T, ?> upstream,
                                Comparator<? super T> comparator) {
        return new OfRef<>(upstream, comparator);
    }

    static <T> IntStream makeInt(AbstractPipeline<?, Integer, ?> upstream) {
        return new OfInt(upstream);
    }

    static <T> LongStream makeLong(AbstractPipeline<?, Long, ?> upstream) {
        return new OfLong(upstream);
    }

    static <T> DoubleStream makeDouble(AbstractPipeline<?, Double, ?> upstream) {
        return new OfDouble(upstream);
    }

    private static final class OfRef<T> extends ReferencePipeline.StatefulOp<T, T> {
        private final boolean isNaturalSort;
        private final Comparator<? super T> comparator;

        OfRef(AbstractPipeline<?, T, ?> upstream) {
            super(upstream, StreamShape.REFERENCE,
                  StreamOpFlag.IS_ORDERED | StreamOpFlag.IS_SORTED);
            this.isNaturalSort = true;
            // Will throw CCE when we try to sort if T is not Comparable
            @SuppressWarnings("unchecked")
            Comparator<? super T> comp = (Comparator<? super T>) Comparator.naturalOrder();
            this.comparator = comp;
        }

        OfRef(AbstractPipeline<?, T, ?> upstream, Comparator<? super T> comparator) {
            super(upstream, StreamShape.REFERENCE,
                  StreamOpFlag.IS_ORDERED | StreamOpFlag.NOT_SORTED);
            this.isNaturalSort = false;
            this.comparator = Objects.requireNonNull(comparator);
        }

        @Override
        public Sink<T> opWrapSink(int flags, Sink<T> sink) {
            Objects.requireNonNull(sink);

            // If the input is already naturally sorted and this operation
            // also naturally sorted then this is a no-op
            if (StreamOpFlag.SORTED.isKnown(flags) && isNaturalSort)
                return sink;
            else if (StreamOpFlag.SIZED.isKnown(flags))
                return new SizedRefSortingSink<>(sink, comparator);
            else
                return new RefSortingSink<>(sink, comparator);
        }

        @Override
        public <P_IN> Node<T> opEvaluateParallel(PipelineHelper<T> helper,
                                                 Spliterator<P_IN> spliterator,
                                                 IntFunction<T[]> generator) {
            // If the input is already naturally sorted and this operation
            // naturally sorts then collect the output
            if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags()) && isNaturalSort) {
                return helper.evaluate(spliterator, false, generator);
            }
            else {
                // @@@ Weak two-pass parallel implementation; parallel collect, parallel sort
                T[] flattenedData = helper.evaluate(spliterator, true, generator).asArray(generator);
                Arrays.parallelSort(flattenedData, comparator);
                return Nodes.node(flattenedData);
            }
        }
    }

    private static final class OfInt extends IntPipeline.StatefulOp<Integer> {
        OfInt(AbstractPipeline<?, Integer, ?> upstream) {
            super(upstream, StreamShape.INT_VALUE,
                  StreamOpFlag.IS_ORDERED | StreamOpFlag.IS_SORTED);
        }

        @Override
        public Sink<Integer> opWrapSink(int flags, Sink<Integer> sink) {
            Objects.requireNonNull(sink);

            if (StreamOpFlag.SORTED.isKnown(flags))
                return sink;
            else if (StreamOpFlag.SIZED.isKnown(flags))
                return new SizedIntSortingSink(sink);
            else
                return new IntSortingSink(sink);
        }

        @Override
        public <P_IN> Node<Integer> opEvaluateParallel(PipelineHelper<Integer> helper,
                                                       Spliterator<P_IN> spliterator,
                                                       IntFunction<Integer[]> generator) {
            if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags())) {
                return helper.evaluate(spliterator, false, generator);
            }
            else {
                Node.OfInt n = (Node.OfInt) helper.evaluate(spliterator, true, generator);

                int[] content = n.asPrimitiveArray();
                Arrays.parallelSort(content);

                return Nodes.node(content);
            }
        }
    }

    private static final class OfLong extends LongPipeline.StatefulOp<Long> {
        OfLong(AbstractPipeline<?, Long, ?> upstream) {
            super(upstream, StreamShape.LONG_VALUE,
                  StreamOpFlag.IS_ORDERED | StreamOpFlag.IS_SORTED);
        }

        @Override
        public Sink<Long> opWrapSink(int flags, Sink<Long> sink) {
            Objects.requireNonNull(sink);

            if (StreamOpFlag.SORTED.isKnown(flags))
                return sink;
            else if (StreamOpFlag.SIZED.isKnown(flags))
                return new SizedLongSortingSink(sink);
            else
                return new LongSortingSink(sink);
        }

        @Override
        public <P_IN> Node<Long> opEvaluateParallel(PipelineHelper<Long> helper,
                                                    Spliterator<P_IN> spliterator,
                                                    IntFunction<Long[]> generator) {
            if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags())) {
                return helper.evaluate(spliterator, false, generator);
            }
            else {
                Node.OfLong n = (Node.OfLong) helper.evaluate(spliterator, true, generator);

                long[] content = n.asPrimitiveArray();
                Arrays.parallelSort(content);

                return Nodes.node(content);
            }
        }
    }

    private static final class OfDouble extends DoublePipeline.StatefulOp<Double> {
        OfDouble(AbstractPipeline<?, Double, ?> upstream) {
            super(upstream, StreamShape.DOUBLE_VALUE,
                  StreamOpFlag.IS_ORDERED | StreamOpFlag.IS_SORTED);
        }

        @Override
        public Sink<Double> opWrapSink(int flags, Sink<Double> sink) {
            Objects.requireNonNull(sink);

            if (StreamOpFlag.SORTED.isKnown(flags))
                return sink;
            else if (StreamOpFlag.SIZED.isKnown(flags))
                return new SizedDoubleSortingSink(sink);
            else
                return new DoubleSortingSink(sink);
        }

        @Override
        public <P_IN> Node<Double> opEvaluateParallel(PipelineHelper<Double> helper,
                                                      Spliterator<P_IN> spliterator,
                                                      IntFunction<Double[]> generator) {
            if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags())) {
                return helper.evaluate(spliterator, false, generator);
            }
            else {
                Node.OfDouble n = (Node.OfDouble) helper.evaluate(spliterator, true, generator);

                double[] content = n.asPrimitiveArray();
                Arrays.parallelSort(content);

                return Nodes.node(content);
            }
        }
    }

    private static abstract class AbstractRefSortingSink<T> extends Sink.ChainedReference<T, T> {
        protected final Comparator<? super T> comparator;
        // @@@ could be a lazy final value, if/when support is added
        // true if cancellationRequested() has been called
        protected boolean cancellationRequestedCalled;

        AbstractRefSortingSink(Sink<? super T> downstream, Comparator<? super T> comparator) {
            super(downstream);
            this.comparator = comparator;
        }

        @Override
        public final boolean cancellationRequested() {
            // If this method is called then an operation within the stream
            // pipeline is short-circuiting (see AbstractPipeline.copyInto).
            // Note that we cannot differentiate between an upstream or
            // downstream operation
            cancellationRequestedCalled = true;
            return false;
        }
    }

    private static final class SizedRefSortingSink<T> extends AbstractRefSortingSink<T> {
        private T[] array;
        private int offset;

        SizedRefSortingSink(Sink<? super T> sink, Comparator<? super T> comparator) {
            super(sink, comparator);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void begin(long size) {
            if (size >= Nodes.MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            array = (T[]) new Object[(int) size];
        }

        @Override
        public void end() {
            Arrays.sort(array, 0, offset, comparator);
            downstream.begin(offset);
            if (!cancellationRequestedCalled) {
                for (int i = 0; i < offset; i++)
                    downstream.accept(array[i]);
            }
            else {
                for (int i = 0; i < offset && !downstream.cancellationRequested(); i++)
                    downstream.accept(array[i]);
            }
            downstream.end();
            array = null;
        }

        @Override
        public void accept(T t) {
            array[offset++] = t;
        }
    }

    /**
     * Stream.sorted()方法将对Stream中的元素进行排序，显然这是一个有状态的中间操作，因为读取所有元素之前是没法得到最终顺序的
     *
     * 首先beging()方法告诉Sink参与排序的元素个数，方便确定中间结果容器的的大小；
     * 之后通过accept()方法将元素添加到中间结果当中，最终执行时调用者会不断调用该方法，直到遍历所有元素；
     * 最后end()方法告诉Sink所有元素遍历完毕，启动排序步骤，排序完成后将结果传递给下游的Sink；
     * 如果下游的Sink是短路操作，将结果传递给下游时不断询问下游cancellationRequested()是否可以结束处理。
     */
    private static final class RefSortingSink<T> extends AbstractRefSortingSink<T> {
        // 存放用于排序的元素
        private ArrayList<T> list;

        RefSortingSink(Sink<? super T> sink, Comparator<? super T> comparator) {
            super(sink, comparator);
        }

        @Override
        public void begin(long size) {
            if (size >= Nodes.MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            // 创建一个存放排序元素的列表
            list = (size >= 0) ? new ArrayList<T>((int) size) : new ArrayList<T>();
        }

        @Override
        public void end() {
            // 只有元素全部接收之后才能开始排序
            list.sort(comparator);
            downstream.begin(list.size());
            // 下游Sink不包含短路操作
            if (!cancellationRequestedCalled) {
                // 将处理结果传递给流水线下游的Sink
                list.forEach(downstream::accept);
            }
            else { // 下游Sink包含短路操作
                for (T t : list) {
                    // 每次都调用cancellationRequested()询问是否可以结束处理
                    if (downstream.cancellationRequested()) break;
                    // 将处理结果传递给流水线下游的Sink
                    downstream.accept(t);
                }
            }
            downstream.end();
            list = null;
        }

        @Override
        public void accept(T t) {
            // 使用当前Sink包装动作处理t，只是简单的将元素添加到中间列表当
            list.add(t);
        }
    }

    private static abstract class AbstractIntSortingSink extends Sink.ChainedInt<Integer> {
        // true if cancellationRequested() has been called
        protected boolean cancellationRequestedCalled;

        AbstractIntSortingSink(Sink<? super Integer> downstream) {
            super(downstream);
        }

        @Override
        public final boolean cancellationRequested() {
            cancellationRequestedCalled = true;
            return false;
        }
    }

    private static final class SizedIntSortingSink extends AbstractIntSortingSink {
        private int[] array;
        private int offset;

        SizedIntSortingSink(Sink<? super Integer> downstream) {
            super(downstream);
        }

        @Override
        public void begin(long size) {
            if (size >= Nodes.MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            array = new int[(int) size];
        }

        @Override
        public void end() {
            Arrays.sort(array, 0, offset);
            downstream.begin(offset);
            if (!cancellationRequestedCalled) {
                for (int i = 0; i < offset; i++)
                    downstream.accept(array[i]);
            }
            else {
                for (int i = 0; i < offset && !downstream.cancellationRequested(); i++)
                    downstream.accept(array[i]);
            }
            downstream.end();
            array = null;
        }

        @Override
        public void accept(int t) {
            array[offset++] = t;
        }
    }

    private static final class IntSortingSink extends AbstractIntSortingSink {
        private SpinedBuffer.OfInt b;

        IntSortingSink(Sink<? super Integer> sink) {
            super(sink);
        }

        @Override
        public void begin(long size) {
            if (size >= Nodes.MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            b = (size > 0) ? new SpinedBuffer.OfInt((int) size) : new SpinedBuffer.OfInt();
        }

        @Override
        public void end() {
            int[] ints = b.asPrimitiveArray();
            Arrays.sort(ints);
            downstream.begin(ints.length);
            if (!cancellationRequestedCalled) {
                for (int anInt : ints)
                    downstream.accept(anInt);
            }
            else {
                for (int anInt : ints) {
                    if (downstream.cancellationRequested()) break;
                    downstream.accept(anInt);
                }
            }
            downstream.end();
        }

        @Override
        public void accept(int t) {
            b.accept(t);
        }
    }

    private static abstract class AbstractLongSortingSink extends Sink.ChainedLong<Long> {
        // true if cancellationRequested() has been called
        protected boolean cancellationRequestedCalled;

        AbstractLongSortingSink(Sink<? super Long> downstream) {
            super(downstream);
        }

        @Override
        public final boolean cancellationRequested() {
            cancellationRequestedCalled = true;
            return false;
        }
    }

    private static final class SizedLongSortingSink extends AbstractLongSortingSink {
        private long[] array;
        private int offset;

        SizedLongSortingSink(Sink<? super Long> downstream) {
            super(downstream);
        }

        @Override
        public void begin(long size) {
            if (size >= Nodes.MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            array = new long[(int) size];
        }

        @Override
        public void end() {
            Arrays.sort(array, 0, offset);
            downstream.begin(offset);
            if (!cancellationRequestedCalled) {
                for (int i = 0; i < offset; i++)
                    downstream.accept(array[i]);
            }
            else {
                for (int i = 0; i < offset && !downstream.cancellationRequested(); i++)
                    downstream.accept(array[i]);
            }
            downstream.end();
            array = null;
        }

        @Override
        public void accept(long t) {
            array[offset++] = t;
        }
    }

    private static final class LongSortingSink extends AbstractLongSortingSink {
        private SpinedBuffer.OfLong b;

        LongSortingSink(Sink<? super Long> sink) {
            super(sink);
        }

        @Override
        public void begin(long size) {
            if (size >= Nodes.MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            b = (size > 0) ? new SpinedBuffer.OfLong((int) size) : new SpinedBuffer.OfLong();
        }

        @Override
        public void end() {
            long[] longs = b.asPrimitiveArray();
            Arrays.sort(longs);
            downstream.begin(longs.length);
            if (!cancellationRequestedCalled) {
                for (long aLong : longs)
                    downstream.accept(aLong);
            }
            else {
                for (long aLong : longs) {
                    if (downstream.cancellationRequested()) break;
                    downstream.accept(aLong);
                }
            }
            downstream.end();
        }

        @Override
        public void accept(long t) {
            b.accept(t);
        }
    }

    private static abstract class AbstractDoubleSortingSink extends Sink.ChainedDouble<Double> {
        // true if cancellationRequested() has been called
        protected boolean cancellationRequestedCalled;

        AbstractDoubleSortingSink(Sink<? super Double> downstream) {
            super(downstream);
        }

        @Override
        public final boolean cancellationRequested() {
            cancellationRequestedCalled = true;
            return false;
        }
    }

    private static final class SizedDoubleSortingSink extends AbstractDoubleSortingSink {
        private double[] array;
        private int offset;

        SizedDoubleSortingSink(Sink<? super Double> downstream) {
            super(downstream);
        }

        @Override
        public void begin(long size) {
            if (size >= Nodes.MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            array = new double[(int) size];
        }

        @Override
        public void end() {
            Arrays.sort(array, 0, offset);
            downstream.begin(offset);
            if (!cancellationRequestedCalled) {
                for (int i = 0; i < offset; i++)
                    downstream.accept(array[i]);
            }
            else {
                for (int i = 0; i < offset && !downstream.cancellationRequested(); i++)
                    downstream.accept(array[i]);
            }
            downstream.end();
            array = null;
        }

        @Override
        public void accept(double t) {
            array[offset++] = t;
        }
    }

    private static final class DoubleSortingSink extends AbstractDoubleSortingSink {
        private SpinedBuffer.OfDouble b;

        DoubleSortingSink(Sink<? super Double> sink) {
            super(sink);
        }

        @Override
        public void begin(long size) {
            if (size >= Nodes.MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            b = (size > 0) ? new SpinedBuffer.OfDouble((int) size) : new SpinedBuffer.OfDouble();
        }

        @Override
        public void end() {
            double[] doubles = b.asPrimitiveArray();
            Arrays.sort(doubles);
            downstream.begin(doubles.length);
            if (!cancellationRequestedCalled) {
                for (double aDouble : doubles)
                    downstream.accept(aDouble);
            }
            else {
                for (double aDouble : doubles) {
                    if (downstream.cancellationRequested()) break;
                    downstream.accept(aDouble);
                }
            }
            downstream.end();
        }

        @Override
        public void accept(double t) {
            b.accept(t);
        }
    }
}
