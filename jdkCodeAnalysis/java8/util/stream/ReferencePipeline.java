package java.util.stream;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.LongConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

/**
 * Spliterator可以看作一个可分割迭代器，为了并行遍历元素而设计的一个迭代器，
 * jdk1.8中的集合框架中的数据结构都默认实现了spliterator。
 * 它提供了trySplit()，为多线程提供处理数据片。它是为了并行处理流而新增的一个迭代类
 *
 */
abstract class ReferencePipeline<P_IN, P_OUT> extends AbstractPipeline<P_IN, P_OUT, Stream<P_OUT>> implements Stream<P_OUT>  {

    ReferencePipeline(Supplier<? extends Spliterator<?>> source, int sourceFlags, boolean parallel) {
        super(source, sourceFlags, parallel);
    }

    ReferencePipeline(Spliterator<?> source, int sourceFlags, boolean parallel) {
        super(source, sourceFlags, parallel);
    }

    ReferencePipeline(AbstractPipeline<?, P_IN, ?> upstream, int opFlags) {
        super(upstream, opFlags);
    }

    // Shape-specific methods

    @Override
    final StreamShape getOutputShape() {
        return StreamShape.REFERENCE;
    }

    @Override
    final <P_IN> Node<P_OUT> evaluateToNode(PipelineHelper<P_OUT> helper,
                                        Spliterator<P_IN> spliterator,
                                        boolean flattenTree,
                                        IntFunction<P_OUT[]> generator) {
        return Nodes.collect(helper, spliterator, flattenTree, generator);
    }

    @Override
    final <P_IN> Spliterator<P_OUT> wrap(PipelineHelper<P_OUT> ph,
                                     Supplier<Spliterator<P_IN>> supplier,
                                     boolean isParallel) {
        return new StreamSpliterators.WrappingSpliterator<>(ph, supplier, isParallel);
    }

    @Override
    final Spliterator<P_OUT> lazySpliterator(Supplier<? extends Spliterator<P_OUT>> supplier) {
        return new StreamSpliterators.DelegatingSpliterator<>(supplier);
    }

    @Override
    final void forEachWithCancel(Spliterator<P_OUT> spliterator, Sink<P_OUT> sink) {
        do { } while (!sink.cancellationRequested() && spliterator.tryAdvance(sink));
    }

    @Override
    final Node.Builder<P_OUT> makeNodeBuilder(long exactSizeIfKnown, IntFunction<P_OUT[]> generator) {
        return Nodes.builder(exactSizeIfKnown, generator);
    }


    // BaseStream

    @Override
    public final Iterator<P_OUT> iterator() {
        return Spliterators.iterator(spliterator());
    }


    // Stream

    // Stateless intermediate operations from Stream

    @Override
    public Stream<P_OUT> unordered() {
        if (!isOrdered())
            return this;
        return new StatelessOp<P_OUT, P_OUT>(this, StreamShape.REFERENCE, StreamOpFlag.NOT_ORDERED) {
            @Override
            Sink<P_OUT> opWrapSink(int flags, Sink<P_OUT> sink) {
                return sink;
            }
        };
    }

    @Override
    public final Stream<P_OUT> filter(Predicate<? super P_OUT> predicate) {
        Objects.requireNonNull(predicate);
        // 返回一个StatelessOp对象(此类依然继承于ReferencePipeline)，它的一个回调函数opWrapSink会返回一个Sink对象链表
        // 第一个参数，传入的是this，即将上一步创建的对象传入，作为的previousStage
        return new StatelessOp<P_OUT, P_OUT>(this, StreamShape.REFERENCE, StreamOpFlag.NOT_SIZED) {
            // opWrapSink会返回一个Sink对象链表
            // Sink代表管道操作的每一个阶段：比如在filter阶段，在调用accept之前，先调用begin通知数据来了，数据发送后调用end
            @Override
            Sink<P_OUT> opWrapSink(int flags, Sink<P_OUT> sink) {
                // 采用静态代理及装饰器模式
                return new Sink.ChainedReference<P_OUT, P_OUT>(sink) {
                    @Override
                    public void begin(long size) {
                        downstream.begin(-1);
                    }

                    @Override
                    public void accept(P_OUT u) {
                        if (predicate.test(u))
                            downstream.accept(u);
                    }
                };
            }
        };
    }

    /**
     * stream.filter(....).map(...)怎么形成一个链的？
     * filter返回一个StatelessOp,我们记为StatelessOp1, 而map返回另外一个StatelessOp，我们记为StatelessOp2。
     * 在调用StatelessOp1.map时， StatelessOp2是这样生成的：return new StatelessOp<P_OUT, R>(StatelessOp1,......);
     * 管道中每个阶段的Stream保留前一个流的引用
     */
    @Override
    @SuppressWarnings("unchecked")
    public final <R> Stream<R> map(Function<? super P_OUT, ? extends R> mapper) {
        Objects.requireNonNull(mapper);
        return new StatelessOp<P_OUT, R>(this, StreamShape.REFERENCE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            Sink<P_OUT> opWrapSink(int flags, Sink<R> sink) {
                return new Sink.ChainedReference<P_OUT, R>(sink) {
                    @Override
                    public void accept(P_OUT u) {
                        // 使用当前Sink包装的回调函数mapper处理u
                        // 将处理结果传递给流水线下游的Sink
                        downstream.accept(mapper.apply(u));
                    }
                };
            }
        };
    }

    @Override
    public final IntStream mapToInt(ToIntFunction<? super P_OUT> mapper) {
        Objects.requireNonNull(mapper);
        return new IntPipeline.StatelessOp<P_OUT>(this, StreamShape.REFERENCE,
                                              StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            Sink<P_OUT> opWrapSink(int flags, Sink<Integer> sink) {
                return new Sink.ChainedReference<P_OUT, Integer>(sink) {
                    @Override
                    public void accept(P_OUT u) {
                        downstream.accept(mapper.applyAsInt(u));
                    }
                };
            }
        };
    }

    @Override
    public final LongStream mapToLong(ToLongFunction<? super P_OUT> mapper) {
        Objects.requireNonNull(mapper);
        return new LongPipeline.StatelessOp<P_OUT>(this, StreamShape.REFERENCE,
                                      StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            Sink<P_OUT> opWrapSink(int flags, Sink<Long> sink) {
                return new Sink.ChainedReference<P_OUT, Long>(sink) {
                    @Override
                    public void accept(P_OUT u) {
                        downstream.accept(mapper.applyAsLong(u));
                    }
                };
            }
        };
    }

    @Override
    public final DoubleStream mapToDouble(ToDoubleFunction<? super P_OUT> mapper) {
        Objects.requireNonNull(mapper);
        return new DoublePipeline.StatelessOp<P_OUT>(this, StreamShape.REFERENCE,
                                        StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            Sink<P_OUT> opWrapSink(int flags, Sink<Double> sink) {
                return new Sink.ChainedReference<P_OUT, Double>(sink) {
                    @Override
                    public void accept(P_OUT u) {
                        downstream.accept(mapper.applyAsDouble(u));
                    }
                };
            }
        };
    }

    @Override
    public final <R> Stream<R> flatMap(Function<? super P_OUT, ? extends Stream<? extends R>> mapper) {
        Objects.requireNonNull(mapper);
        return new StatelessOp<P_OUT, R>(this, StreamShape.REFERENCE,
                                     StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT | StreamOpFlag.NOT_SIZED) {
            @Override
            Sink<P_OUT> opWrapSink(int flags, Sink<R> sink) {
                return new Sink.ChainedReference<P_OUT, R>(sink) {
                    // true if cancellationRequested() has been called
                    boolean cancellationRequestedCalled;

                    @Override
                    public void begin(long size) {
                        downstream.begin(-1);
                    }

                    @Override
                    public void accept(P_OUT u) {
                        try (Stream<? extends R> result = mapper.apply(u)) {
                            if (result != null) {
                                if (!cancellationRequestedCalled) {
                                    result.sequential().forEach(downstream);
                                }
                                else {
                                    Spliterator<? extends R> s = result.sequential().spliterator();
                                    do { } while (!downstream.cancellationRequested() && s.tryAdvance(downstream));
                                }
                            }
                        }
                    }

                    @Override
                    public boolean cancellationRequested() {
                        cancellationRequestedCalled = true;
                        return downstream.cancellationRequested();
                    }
                };
            }
        };
    }

    @Override
    public final IntStream flatMapToInt(Function<? super P_OUT, ? extends IntStream> mapper) {
        Objects.requireNonNull(mapper);
        return new IntPipeline.StatelessOp<P_OUT>(this, StreamShape.REFERENCE,
                                              StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT | StreamOpFlag.NOT_SIZED) {
            @Override
            Sink<P_OUT> opWrapSink(int flags, Sink<Integer> sink) {
                return new Sink.ChainedReference<P_OUT, Integer>(sink) {
                    // true if cancellationRequested() has been called
                    boolean cancellationRequestedCalled;

                    // cache the consumer to avoid creation on every accepted element
                    IntConsumer downstreamAsInt = downstream::accept;

                    @Override
                    public void begin(long size) {
                        downstream.begin(-1);
                    }

                    @Override
                    public void accept(P_OUT u) {
                        try (IntStream result = mapper.apply(u)) {
                            if (result != null) {
                                if (!cancellationRequestedCalled) {
                                    result.sequential().forEach(downstreamAsInt);
                                }
                                else {
                                    Spliterator.OfInt s = result.sequential().spliterator();
                                    do { } while (!downstream.cancellationRequested() && s.tryAdvance(downstreamAsInt));
                                }
                            }
                        }
                    }

                    @Override
                    public boolean cancellationRequested() {
                        cancellationRequestedCalled = true;
                        return downstream.cancellationRequested();
                    }
                };
            }
        };
    }

    @Override
    public final DoubleStream flatMapToDouble(Function<? super P_OUT, ? extends DoubleStream> mapper) {
        Objects.requireNonNull(mapper);
        return new DoublePipeline.StatelessOp<P_OUT>(this, StreamShape.REFERENCE,
                                                     StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT | StreamOpFlag.NOT_SIZED) {
            @Override
            Sink<P_OUT> opWrapSink(int flags, Sink<Double> sink) {
                return new Sink.ChainedReference<P_OUT, Double>(sink) {
                    // true if cancellationRequested() has been called
                    boolean cancellationRequestedCalled;

                    // cache the consumer to avoid creation on every accepted element
                    DoubleConsumer downstreamAsDouble = downstream::accept;

                    @Override
                    public void begin(long size) {
                        downstream.begin(-1);
                    }

                    @Override
                    public void accept(P_OUT u) {
                        try (DoubleStream result = mapper.apply(u)) {
                            if (result != null) {
                                if (!cancellationRequestedCalled) {
                                    result.sequential().forEach(downstreamAsDouble);
                                }
                                else {
                                    Spliterator.OfDouble s = result.sequential().spliterator();
                                    do { } while (!downstream.cancellationRequested() && s.tryAdvance(downstreamAsDouble));
                                }
                            }
                        }
                    }

                    @Override
                    public boolean cancellationRequested() {
                        cancellationRequestedCalled = true;
                        return downstream.cancellationRequested();
                    }
                };
            }
        };
    }

    @Override
    public final LongStream flatMapToLong(Function<? super P_OUT, ? extends LongStream> mapper) {
        Objects.requireNonNull(mapper);
        // We can do better than this, by polling cancellationRequested when stream is infinite
        return new LongPipeline.StatelessOp<P_OUT>(this, StreamShape.REFERENCE,
                                                   StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT | StreamOpFlag.NOT_SIZED) {
            @Override
            Sink<P_OUT> opWrapSink(int flags, Sink<Long> sink) {
                return new Sink.ChainedReference<P_OUT, Long>(sink) {
                    // true if cancellationRequested() has been called
                    boolean cancellationRequestedCalled;

                    // cache the consumer to avoid creation on every accepted element
                    LongConsumer downstreamAsLong = downstream::accept;

                    @Override
                    public void begin(long size) {
                        downstream.begin(-1);
                    }

                    @Override
                    public void accept(P_OUT u) {
                        try (LongStream result = mapper.apply(u)) {
                            if (result != null) {
                                if (!cancellationRequestedCalled) {
                                    result.sequential().forEach(downstreamAsLong);
                                }
                                else {
                                    Spliterator.OfLong s = result.sequential().spliterator();
                                    do { } while (!downstream.cancellationRequested() && s.tryAdvance(downstreamAsLong));
                                }
                            }
                        }
                    }

                    @Override
                    public boolean cancellationRequested() {
                        cancellationRequestedCalled = true;
                        return downstream.cancellationRequested();
                    }
                };
            }
        };
    }

    @Override
    public final Stream<P_OUT> peek(Consumer<? super P_OUT> action) {
        Objects.requireNonNull(action);
        return new StatelessOp<P_OUT, P_OUT>(this, StreamShape.REFERENCE,
                                     0) {
            @Override
            Sink<P_OUT> opWrapSink(int flags, Sink<P_OUT> sink) {
                return new Sink.ChainedReference<P_OUT, P_OUT>(sink) {
                    @Override
                    public void accept(P_OUT u) {
                        action.accept(u);
                        downstream.accept(u);
                    }
                };
            }
        };
    }

    // Stateful intermediate operations from Stream

    @Override
    public final Stream<P_OUT> distinct() {
        return DistinctOps.makeRef(this);
    }

    @Override
    public final Stream<P_OUT> sorted() {
        return SortedOps.makeRef(this);
    }

    @Override
    public final Stream<P_OUT> sorted(Comparator<? super P_OUT> comparator) {
        return SortedOps.makeRef(this, comparator);
    }

    @Override
    public final Stream<P_OUT> limit(long maxSize) {
        if (maxSize < 0)
            throw new IllegalArgumentException(Long.toString(maxSize));
        return SliceOps.makeRef(this, 0, maxSize);
    }

    @Override
    public final Stream<P_OUT> skip(long n) {
        if (n < 0)
            throw new IllegalArgumentException(Long.toString(n));
        if (n == 0)
            return this;
        else
            return SliceOps.makeRef(this, n, -1);
    }

    // Terminal operations from Stream

    @Override
    public void forEach(Consumer<? super P_OUT> action) {
        evaluate(ForEachOps.makeRef(action, false));
    }

    @Override
    public void forEachOrdered(Consumer<? super P_OUT> action) {
        evaluate(ForEachOps.makeRef(action, true));
    }

    @Override
    @SuppressWarnings("unchecked")
    public final <A> A[] toArray(IntFunction<A[]> generator) {
        @SuppressWarnings("rawtypes")
        IntFunction rawGenerator = (IntFunction) generator;
        return (A[]) Nodes.flatten(evaluateToArrayNode(rawGenerator), rawGenerator)
                              .asArray(rawGenerator);
    }

    @Override
    public final Object[] toArray() {
        return toArray(Object[]::new);
    }

    @Override
    public final boolean anyMatch(Predicate<? super P_OUT> predicate) {
        return evaluate(MatchOps.makeRef(predicate, MatchOps.MatchKind.ANY));
    }

    @Override
    public final boolean allMatch(Predicate<? super P_OUT> predicate) {
        return evaluate(MatchOps.makeRef(predicate, MatchOps.MatchKind.ALL));
    }

    @Override
    public final boolean noneMatch(Predicate<? super P_OUT> predicate) {
        return evaluate(MatchOps.makeRef(predicate, MatchOps.MatchKind.NONE));
    }

    @Override
    public final Optional<P_OUT> findFirst() {
        return evaluate(FindOps.makeRef(true));
    }

    @Override
    public final Optional<P_OUT> findAny() {
        return evaluate(FindOps.makeRef(false));
    }

    @Override
    public final P_OUT reduce(final P_OUT identity, final BinaryOperator<P_OUT> accumulator) {
        return evaluate(ReduceOps.makeRef(identity, accumulator, accumulator));
    }

    @Override
    public final Optional<P_OUT> reduce(BinaryOperator<P_OUT> accumulator) {
        return evaluate(ReduceOps.makeRef(accumulator));
    }

    @Override
    public final <R> R reduce(R identity, BiFunction<R, ? super P_OUT, R> accumulator, BinaryOperator<R> combiner) {
        return evaluate(ReduceOps.makeRef(identity, accumulator, combiner));
    }

    /**
     * collect操作，不同于filter与map，collect为结束操作，是通过 Sink 来处理流中的数据
     */
    @Override
    @SuppressWarnings("unchecked")
    public final <R, A> R collect(Collector<? super P_OUT, A, R> collector) {
        A container;
        // 并行模式
        if (isParallel()
                && (collector.characteristics().contains(Collector.Characteristics.CONCURRENT))
                && (!isOrdered() || collector.characteristics().contains(Collector.Characteristics.UNORDERED))) {
            container = collector.supplier().get();
            BiConsumer<A, ? super P_OUT> accumulator = collector.accumulator();
            forEach(u -> accumulator.accept(container, u));
        }
        else {
            // 串行模式
            // ReduceOps.makeRef(collector) 会构造一个TerminalOp对象，传入evaluate方法
            // 发现最终是调用copyInto方法来启动流水线
            container = evaluate(ReduceOps.makeRef(collector));
        }
        return collector.characteristics().contains(Collector.Characteristics.IDENTITY_FINISH)
               ? (R) container
               : collector.finisher().apply(container);
    }

    @Override
    public final <R> R collect(Supplier<R> supplier,
                               BiConsumer<R, ? super P_OUT> accumulator,
                               BiConsumer<R, R> combiner) {
        return evaluate(ReduceOps.makeRef(supplier, accumulator, combiner));
    }

    @Override
    public final Optional<P_OUT> max(Comparator<? super P_OUT> comparator) {
        return reduce(BinaryOperator.maxBy(comparator));
    }

    @Override
    public final Optional<P_OUT> min(Comparator<? super P_OUT> comparator) {
        return reduce(BinaryOperator.minBy(comparator));

    }

    @Override
    public final long count() {
        return mapToLong(e -> 1L).sum();
    }

    // Head是ReferencePipeline数据源，其实内部就是一个集合的并行迭代器
    static class Head<E_IN, E_OUT> extends ReferencePipeline<E_IN, E_OUT> {
        Head(Supplier<? extends Spliterator<?>> source, int sourceFlags, boolean parallel) {
            super(source, sourceFlags, parallel);
        }
        // 构造器，数据源类型就是Spliterator
        Head(Spliterator<?> source, int sourceFlags, boolean parallel) {
            super(source, sourceFlags, parallel);
        }

        @Override
        final boolean opIsStateful() {
            throw new UnsupportedOperationException();
        }

        @Override
        final Sink<E_IN> opWrapSink(int flags, Sink<E_OUT> sink) {
            throw new UnsupportedOperationException();
        }

        // Optimized sequential terminal operations for the head of the pipeline

        @Override
        public void forEach(Consumer<? super E_OUT> action) {
            if (!isParallel()) {
                sourceStageSpliterator().forEachRemaining(action);
            }
            else {
                super.forEach(action);
            }
        }

        @Override
        public void forEachOrdered(Consumer<? super E_OUT> action) {
            if (!isParallel()) {
                sourceStageSpliterator().forEachRemaining(action);
            }
            else {
                super.forEachOrdered(action);
            }
        }
    }
    // 无状态的链式加工，会返回一个StatelessOp对象
    abstract static class StatelessOp<E_IN, E_OUT> extends ReferencePipeline<E_IN, E_OUT> {
        // 构造器，就是将当前的中间操作和旧的Stream组合成一个新的Stream，返回新的Stream，实现链式调用
        StatelessOp(AbstractPipeline<?, E_IN, ?> upstream, StreamShape inputShape, int opFlags) {
            super(upstream, opFlags);
            assert upstream.getOutputShape() == inputShape;
        }

        @Override
        final boolean opIsStateful() {
            return false;
        }
    }
    // 有状态的加工操作会返回一个StatefulOp对象
    abstract static class StatefulOp<E_IN, E_OUT> extends ReferencePipeline<E_IN, E_OUT> {
        StatefulOp(AbstractPipeline<?, E_IN, ?> upstream, StreamShape inputShape, int opFlags) {
            super(upstream, opFlags);
            assert upstream.getOutputShape() == inputShape;
        }

        @Override
        final boolean opIsStateful() {
            return true;
        }

        @Override
        abstract <P_IN> Node<E_OUT> opEvaluateParallel(PipelineHelper<E_OUT> helper, Spliterator<P_IN> spliterator, IntFunction<E_OUT[]> generator);
    }
}
