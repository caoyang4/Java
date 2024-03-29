package java.util.stream;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Spliterator;
import java.util.concurrent.CountedCompleter;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.DoubleBinaryOperator;
import java.util.function.IntBinaryOperator;
import java.util.function.LongBinaryOperator;
import java.util.function.ObjDoubleConsumer;
import java.util.function.ObjIntConsumer;
import java.util.function.ObjLongConsumer;
import java.util.function.Supplier;

final class ReduceOps {

    private ReduceOps() { }

    public static <T, U> TerminalOp<T, U> makeRef(U seed, BiFunction<U, ? super T, U> reducer, BinaryOperator<U> combiner) {
        Objects.requireNonNull(reducer);
        Objects.requireNonNull(combiner);
        class ReducingSink extends Box<U> implements AccumulatingSink<T, U, ReducingSink> {
            @Override
            public void begin(long size) {
                state = seed;
            }

            @Override
            public void accept(T t) {
                state = reducer.apply(state, t);
            }

            @Override
            public void combine(ReducingSink other) {
                state = combiner.apply(state, other.state);
            }
        }
        return new ReduceOp<T, U, ReducingSink>(StreamShape.REFERENCE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    public static <T> TerminalOp<T, Optional<T>>
    makeRef(BinaryOperator<T> operator) {
        Objects.requireNonNull(operator);
        class ReducingSink
                implements AccumulatingSink<T, Optional<T>, ReducingSink> {
            private boolean empty;
            private T state;

            public void begin(long size) {
                empty = true;
                state = null;
            }

            @Override
            public void accept(T t) {
                if (empty) {
                    empty = false;
                    state = t;
                } else {
                    state = operator.apply(state, t);
                }
            }

            @Override
            public Optional<T> get() {
                return empty ? Optional.empty() : Optional.of(state);
            }

            @Override
            public void combine(ReducingSink other) {
                if (!other.empty)
                    accept(other.state);
            }
        }
        return new ReduceOp<T, Optional<T>, ReducingSink>(StreamShape.REFERENCE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    public static <T, I> TerminalOp<T, I>
    makeRef(Collector<? super T, I, ?> collector) {
        Supplier<I> supplier = Objects.requireNonNull(collector).supplier();
        BiConsumer<I, ? super T> accumulator = collector.accumulator();
        BinaryOperator<I> combiner = collector.combiner();
        class ReducingSink extends Box<I>
                implements AccumulatingSink<T, I, ReducingSink> {
            @Override
            public void begin(long size) {
                state = supplier.get();
            }

            @Override
            public void accept(T t) {
                accumulator.accept(state, t);
            }

            @Override
            public void combine(ReducingSink other) {
                state = combiner.apply(state, other.state);
            }
        }
        return new ReduceOp<T, I, ReducingSink>(StreamShape.REFERENCE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }

            @Override
            public int getOpFlags() {
                return collector.characteristics().contains(Collector.Characteristics.UNORDERED)
                       ? StreamOpFlag.NOT_ORDERED
                       : 0;
            }
        };
    }

    public static <T, R> TerminalOp<T, R>
    makeRef(Supplier<R> seedFactory,
            BiConsumer<R, ? super T> accumulator,
            BiConsumer<R,R> reducer) {
        Objects.requireNonNull(seedFactory);
        Objects.requireNonNull(accumulator);
        Objects.requireNonNull(reducer);
        class ReducingSink extends Box<R>
                implements AccumulatingSink<T, R, ReducingSink> {
            @Override
            public void begin(long size) {
                state = seedFactory.get();
            }

            @Override
            public void accept(T t) {
                accumulator.accept(state, t);
            }

            @Override
            public void combine(ReducingSink other) {
                reducer.accept(state, other.state);
            }
        }
        return new ReduceOp<T, R, ReducingSink>(StreamShape.REFERENCE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    public static TerminalOp<Integer, Integer>
    makeInt(int identity, IntBinaryOperator operator) {
        Objects.requireNonNull(operator);
        class ReducingSink
                implements AccumulatingSink<Integer, Integer, ReducingSink>, Sink.OfInt {
            private int state;

            @Override
            public void begin(long size) {
                state = identity;
            }

            @Override
            public void accept(int t) {
                state = operator.applyAsInt(state, t);
            }

            @Override
            public Integer get() {
                return state;
            }

            @Override
            public void combine(ReducingSink other) {
                accept(other.state);
            }
        }
        return new ReduceOp<Integer, Integer, ReducingSink>(StreamShape.INT_VALUE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    public static TerminalOp<Integer, OptionalInt>
    makeInt(IntBinaryOperator operator) {
        Objects.requireNonNull(operator);
        class ReducingSink
                implements AccumulatingSink<Integer, OptionalInt, ReducingSink>, Sink.OfInt {
            private boolean empty;
            private int state;

            public void begin(long size) {
                empty = true;
                state = 0;
            }

            @Override
            public void accept(int t) {
                if (empty) {
                    empty = false;
                    state = t;
                }
                else {
                    state = operator.applyAsInt(state, t);
                }
            }

            @Override
            public OptionalInt get() {
                return empty ? OptionalInt.empty() : OptionalInt.of(state);
            }

            @Override
            public void combine(ReducingSink other) {
                if (!other.empty)
                    accept(other.state);
            }
        }
        return new ReduceOp<Integer, OptionalInt, ReducingSink>(StreamShape.INT_VALUE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    public static <R> TerminalOp<Integer, R>
    makeInt(Supplier<R> supplier,
            ObjIntConsumer<R> accumulator,
            BinaryOperator<R> combiner) {
        Objects.requireNonNull(supplier);
        Objects.requireNonNull(accumulator);
        Objects.requireNonNull(combiner);
        class ReducingSink extends Box<R>
                implements AccumulatingSink<Integer, R, ReducingSink>, Sink.OfInt {
            @Override
            public void begin(long size) {
                state = supplier.get();
            }

            @Override
            public void accept(int t) {
                accumulator.accept(state, t);
            }

            @Override
            public void combine(ReducingSink other) {
                state = combiner.apply(state, other.state);
            }
        }
        return new ReduceOp<Integer, R, ReducingSink>(StreamShape.INT_VALUE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    public static TerminalOp<Long, Long>
    makeLong(long identity, LongBinaryOperator operator) {
        Objects.requireNonNull(operator);
        class ReducingSink
                implements AccumulatingSink<Long, Long, ReducingSink>, Sink.OfLong {
            private long state;

            @Override
            public void begin(long size) {
                state = identity;
            }

            @Override
            public void accept(long t) {
                state = operator.applyAsLong(state, t);
            }

            @Override
            public Long get() {
                return state;
            }

            @Override
            public void combine(ReducingSink other) {
                accept(other.state);
            }
        }
        return new ReduceOp<Long, Long, ReducingSink>(StreamShape.LONG_VALUE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    public static TerminalOp<Long, OptionalLong>
    makeLong(LongBinaryOperator operator) {
        Objects.requireNonNull(operator);
        class ReducingSink
                implements AccumulatingSink<Long, OptionalLong, ReducingSink>, Sink.OfLong {
            private boolean empty;
            private long state;

            public void begin(long size) {
                empty = true;
                state = 0;
            }

            @Override
            public void accept(long t) {
                if (empty) {
                    empty = false;
                    state = t;
                }
                else {
                    state = operator.applyAsLong(state, t);
                }
            }

            @Override
            public OptionalLong get() {
                return empty ? OptionalLong.empty() : OptionalLong.of(state);
            }

            @Override
            public void combine(ReducingSink other) {
                if (!other.empty)
                    accept(other.state);
            }
        }
        return new ReduceOp<Long, OptionalLong, ReducingSink>(StreamShape.LONG_VALUE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    public static <R> TerminalOp<Long, R>
    makeLong(Supplier<R> supplier,
             ObjLongConsumer<R> accumulator,
             BinaryOperator<R> combiner) {
        Objects.requireNonNull(supplier);
        Objects.requireNonNull(accumulator);
        Objects.requireNonNull(combiner);
        class ReducingSink extends Box<R>
                implements AccumulatingSink<Long, R, ReducingSink>, Sink.OfLong {
            @Override
            public void begin(long size) {
                state = supplier.get();
            }

            @Override
            public void accept(long t) {
                accumulator.accept(state, t);
            }

            @Override
            public void combine(ReducingSink other) {
                state = combiner.apply(state, other.state);
            }
        }
        return new ReduceOp<Long, R, ReducingSink>(StreamShape.LONG_VALUE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    public static TerminalOp<Double, Double>
    makeDouble(double identity, DoubleBinaryOperator operator) {
        Objects.requireNonNull(operator);
        class ReducingSink
                implements AccumulatingSink<Double, Double, ReducingSink>, Sink.OfDouble {
            private double state;

            @Override
            public void begin(long size) {
                state = identity;
            }

            @Override
            public void accept(double t) {
                state = operator.applyAsDouble(state, t);
            }

            @Override
            public Double get() {
                return state;
            }

            @Override
            public void combine(ReducingSink other) {
                accept(other.state);
            }
        }
        return new ReduceOp<Double, Double, ReducingSink>(StreamShape.DOUBLE_VALUE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    public static TerminalOp<Double, OptionalDouble>
    makeDouble(DoubleBinaryOperator operator) {
        Objects.requireNonNull(operator);
        class ReducingSink
                implements AccumulatingSink<Double, OptionalDouble, ReducingSink>, Sink.OfDouble {
            private boolean empty;
            private double state;

            public void begin(long size) {
                empty = true;
                state = 0;
            }

            @Override
            public void accept(double t) {
                if (empty) {
                    empty = false;
                    state = t;
                }
                else {
                    state = operator.applyAsDouble(state, t);
                }
            }

            @Override
            public OptionalDouble get() {
                return empty ? OptionalDouble.empty() : OptionalDouble.of(state);
            }

            @Override
            public void combine(ReducingSink other) {
                if (!other.empty)
                    accept(other.state);
            }
        }
        return new ReduceOp<Double, OptionalDouble, ReducingSink>(StreamShape.DOUBLE_VALUE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    public static <R> TerminalOp<Double, R>
    makeDouble(Supplier<R> supplier,
               ObjDoubleConsumer<R> accumulator,
               BinaryOperator<R> combiner) {
        Objects.requireNonNull(supplier);
        Objects.requireNonNull(accumulator);
        Objects.requireNonNull(combiner);
        class ReducingSink extends Box<R>
                implements AccumulatingSink<Double, R, ReducingSink>, Sink.OfDouble {
            @Override
            public void begin(long size) {
                state = supplier.get();
            }

            @Override
            public void accept(double t) {
                accumulator.accept(state, t);
            }

            @Override
            public void combine(ReducingSink other) {
                state = combiner.apply(state, other.state);
            }
        }
        return new ReduceOp<Double, R, ReducingSink>(StreamShape.DOUBLE_VALUE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    /**
     * A type of {@code TerminalSink} that implements an associative reducing
     * operation on elements of type {@code T} and producing a result of type
     * {@code R}.
     *
     * @param <T> the type of input element to the combining operation
     * @param <R> the result type
     * @param <K> the type of the {@code AccumulatingSink}.
     */
    private interface AccumulatingSink<T, R, K extends AccumulatingSink<T, R, K>> extends TerminalSink<T, R> {
        public void combine(K other);
    }

    private static abstract class Box<U> {
        U state;

        Box() {} // Avoid creation of special accessor

        public U get() {
            return state;
        }
    }

    private static abstract class ReduceOp<T, R, S extends AccumulatingSink<T, R, S>> implements TerminalOp<T, R> {
        private final StreamShape inputShape;

        ReduceOp(StreamShape shape) {
            inputShape = shape;
        }

        public abstract S makeSink();

        @Override
        public StreamShape inputShape() {
            return inputShape;
        }

        @Override
        public <P_IN> R evaluateSequential(PipelineHelper<T> helper, Spliterator<P_IN> spliterator) {
            return helper.wrapAndCopyInto(makeSink(), spliterator).get();
        }
        // 并行
        // 并行流的实现本质上就是在ForkJoin上进行了一层封装，将Stream 不断尝试分解成更小的split，然后使用fork/join 框架分而治之
        @Override
        public <P_IN> R evaluateParallel(PipelineHelper<T> helper, Spliterator<P_IN> spliterator) {
            // ReduceTask是ForkJoinTask的子类，其实Stream的并行处理都是基于Fork/Join框架
            return new ReduceTask<>(this, helper, spliterator).invoke().get();
        }
    }

    @SuppressWarnings("serial")
    private static final class ReduceTask<P_IN, P_OUT, R, S extends AccumulatingSink<P_OUT, R, S>> extends AbstractTask<P_IN, P_OUT, S, ReduceTask<P_IN, P_OUT, R, S>> {
        private final ReduceOp<P_OUT, R, S> op;

        ReduceTask(ReduceOp<P_OUT, R, S> op, PipelineHelper<P_OUT> helper, Spliterator<P_IN> spliterator) {
            super(helper, spliterator);
            this.op = op;
        }

        ReduceTask(ReduceTask<P_IN, P_OUT, R, S> parent,
                   Spliterator<P_IN> spliterator) {
            super(parent, spliterator);
            this.op = parent.op;
        }

        @Override
        protected ReduceTask<P_IN, P_OUT, R, S> makeChild(Spliterator<P_IN> spliterator) {
            return new ReduceTask<>(this, spliterator);
        }

        @Override
        protected S doLeaf() {
            return helper.wrapAndCopyInto(op.makeSink(), spliterator);
        }

        @Override
        public void onCompletion(CountedCompleter<?> caller) {
            if (!isLeaf()) {
                S leftResult = leftChild.getLocalResult();
                leftResult.combine(rightChild.getLocalResult());
                setLocalResult(leftResult);
            }
            // GC spliterator, left and right child
            super.onCompletion(caller);
        }
    }
}
