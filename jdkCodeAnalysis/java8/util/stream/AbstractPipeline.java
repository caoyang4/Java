package java.util.stream;

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * AbstractPipeline有4个子类，分别表示流处理元素为double、int，long和reference类型的管道。
 * 这4个子类都是抽象类，每个子类下都有3个静态内部类的实现类，Head、StatefulOp、StatelessOp，
 * 其中Head用于创建一个全新的流，StatefulOp表示有状态的一类操作，StatelessOp表示无状态的一类操作，
 * 这里的有状态是指前面流元素的处理会直接影响后面流元素的处理，多线程并行处理下每次运行的结果都不相同
 *
 * 当前节点同时持有前一个节点与后一个节点的指针，并且保留了头结点的引用，双向链表
 */
abstract class AbstractPipeline<E_IN, E_OUT, S extends BaseStream<E_OUT, S>> extends PipelineHelper<E_OUT> implements BaseStream<E_OUT, S> {
    private static final String MSG_STREAM_LINKED = "stream has already been operated upon or closed";
    private static final String MSG_CONSUMED = "source already consumed or closed";
    // 初始的包含待处理的流，相当于头结点
    @SuppressWarnings("rawtypes")
    private final AbstractPipeline sourceStage;
    // 上一个流处理动作，相当于前驱结点
    @SuppressWarnings("rawtypes")
    private final AbstractPipeline previousStage;
    // 当前流处理动作的标识
    protected final int sourceOrOpFlags;
    // 下一个流处理动作，相当于后继节点
    @SuppressWarnings("rawtypes")
    private AbstractPipeline nextStage;
    // 非终止类型的操作标识个数
    private int depth;

    private int combinedFlags;

    private Spliterator<?> sourceSpliterator;

    private Supplier<? extends Spliterator<?>> sourceSupplier;

    private boolean linkedOrConsumed;

    private boolean sourceAnyStateful;

    private Runnable sourceCloseAction;
    //是否并行流处理
    private boolean parallel;

    AbstractPipeline(Supplier<? extends Spliterator<?>> source, int sourceFlags, boolean parallel) {
        this.previousStage = null;
        this.sourceSupplier = source;
        this.sourceStage = this;
        this.sourceOrOpFlags = sourceFlags & StreamOpFlag.STREAM_MASK;
        // The following is an optimization of:
        // StreamOpFlag.combineOpFlags(sourceOrOpFlags, StreamOpFlag.INITIAL_OPS_VALUE);
        this.combinedFlags = (~(sourceOrOpFlags << 1)) & StreamOpFlag.INITIAL_OPS_VALUE;
        this.depth = 0;
        this.parallel = parallel;
    }

    AbstractPipeline(Spliterator<?> source, int sourceFlags, boolean parallel) {
        this.previousStage = null;
        this.sourceSpliterator = source;
        this.sourceStage = this;
        this.sourceOrOpFlags = sourceFlags & StreamOpFlag.STREAM_MASK;
        // The following is an optimization of:
        // StreamOpFlag.combineOpFlags(sourceOrOpFlags, StreamOpFlag.INITIAL_OPS_VALUE);
        this.combinedFlags = (~(sourceOrOpFlags << 1)) & StreamOpFlag.INITIAL_OPS_VALUE;
        this.depth = 0;
        this.parallel = parallel;
    }

    AbstractPipeline(AbstractPipeline<?, E_IN, ?> previousStage, int opFlags) {
        if (previousStage.linkedOrConsumed)
            throw new IllegalStateException(MSG_STREAM_LINKED);
        previousStage.linkedOrConsumed = true;
        // 前一个stage指向自己
        previousStage.nextStage = this;
        // 自己指向前一个stage
        this.previousStage = previousStage;
        this.sourceOrOpFlags = opFlags & StreamOpFlag.OP_MASK;
        this.combinedFlags = StreamOpFlag.combineOpFlags(opFlags, previousStage.combinedFlags);
        // 也保留了头结点的引用
        this.sourceStage = previousStage.sourceStage;
        if (opIsStateful())
            sourceStage.sourceAnyStateful = true;
        this.depth = previousStage.depth + 1;
    }


    // Terminal evaluation methods

    final <R> R evaluate(TerminalOp<E_OUT, R> terminalOp) {
        assert getOutputShape() == terminalOp.inputShape();
        if (linkedOrConsumed)
            throw new IllegalStateException(MSG_STREAM_LINKED);
        linkedOrConsumed = true;

        return isParallel()
               ? terminalOp.evaluateParallel(this, sourceSpliterator(terminalOp.getOpFlags()))
               : terminalOp.evaluateSequential(this, sourceSpliterator(terminalOp.getOpFlags()));
    }

    @SuppressWarnings("unchecked")
    final Node<E_OUT> evaluateToArrayNode(IntFunction<E_OUT[]> generator) {
        if (linkedOrConsumed)
            throw new IllegalStateException(MSG_STREAM_LINKED);
        linkedOrConsumed = true;


        if (isParallel() && previousStage != null && opIsStateful()) {

            depth = 0;
            return opEvaluateParallel(previousStage, previousStage.sourceSpliterator(0), generator);
        }
        else {
            return evaluate(sourceSpliterator(0), true, generator);
        }
    }

    @SuppressWarnings("unchecked")
    final Spliterator<E_OUT> sourceStageSpliterator() {
        if (this != sourceStage)
            throw new IllegalStateException();

        if (linkedOrConsumed)
            throw new IllegalStateException(MSG_STREAM_LINKED);
        linkedOrConsumed = true;

        if (sourceStage.sourceSpliterator != null) {
            @SuppressWarnings("unchecked")
            Spliterator<E_OUT> s = sourceStage.sourceSpliterator;
            sourceStage.sourceSpliterator = null;
            return s;
        }
        else if (sourceStage.sourceSupplier != null) {
            @SuppressWarnings("unchecked")
            Spliterator<E_OUT> s = (Spliterator<E_OUT>) sourceStage.sourceSupplier.get();
            sourceStage.sourceSupplier = null;
            return s;
        }
        else {
            throw new IllegalStateException(MSG_CONSUMED);
        }
    }

    // BaseStream

    @Override
    @SuppressWarnings("unchecked")
    public final S sequential() {
        sourceStage.parallel = false;
        return (S) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public final S parallel() {
        sourceStage.parallel = true;
        return (S) this;
    }

    @Override
    public void close() {
        linkedOrConsumed = true;
        sourceSupplier = null;
        sourceSpliterator = null;
        if (sourceStage.sourceCloseAction != null) {
            Runnable closeAction = sourceStage.sourceCloseAction;
            sourceStage.sourceCloseAction = null;
            closeAction.run();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public S onClose(Runnable closeHandler) {
        Objects.requireNonNull(closeHandler);
        Runnable existingHandler = sourceStage.sourceCloseAction;
        sourceStage.sourceCloseAction =
                (existingHandler == null)
                ? closeHandler
                : Streams.composeWithExceptions(existingHandler, closeHandler);
        return (S) this;
    }

    // Primitive specialization use co-variant overrides, hence is not final
    @Override
    @SuppressWarnings("unchecked")
    public Spliterator<E_OUT> spliterator() {
        if (linkedOrConsumed)
            throw new IllegalStateException(MSG_STREAM_LINKED);
        linkedOrConsumed = true;

        if (this == sourceStage) {
            if (sourceStage.sourceSpliterator != null) {
                @SuppressWarnings("unchecked")
                Spliterator<E_OUT> s = (Spliterator<E_OUT>) sourceStage.sourceSpliterator;
                sourceStage.sourceSpliterator = null;
                return s;
            }
            else if (sourceStage.sourceSupplier != null) {
                @SuppressWarnings("unchecked")
                Supplier<Spliterator<E_OUT>> s = (Supplier<Spliterator<E_OUT>>) sourceStage.sourceSupplier;
                sourceStage.sourceSupplier = null;
                return lazySpliterator(s);
            }
            else {
                throw new IllegalStateException(MSG_CONSUMED);
            }
        }
        else {
            return wrap(this, () -> sourceSpliterator(0), isParallel());
        }
    }

    @Override
    public final boolean isParallel() {
        return sourceStage.parallel;
    }


    final int getStreamFlags() {
        return StreamOpFlag.toStreamFlags(combinedFlags);
    }

    @SuppressWarnings("unchecked")
    private Spliterator<?> sourceSpliterator(int terminalFlags) {
        // Get the source spliterator of the pipeline
        Spliterator<?> spliterator = null;
        if (sourceStage.sourceSpliterator != null) {
            spliterator = sourceStage.sourceSpliterator;
            sourceStage.sourceSpliterator = null;
        }
        else if (sourceStage.sourceSupplier != null) {
            spliterator = (Spliterator<?>) sourceStage.sourceSupplier.get();
            sourceStage.sourceSupplier = null;
        }
        else {
            throw new IllegalStateException(MSG_CONSUMED);
        }

        if (isParallel() && sourceStage.sourceAnyStateful) {
            // Adapt the source spliterator, evaluating each stateful op
            // in the pipeline up to and including this pipeline stage.
            // The depth and flags of each pipeline stage are adjusted accordingly.
            int depth = 1;
            for (@SuppressWarnings("rawtypes") AbstractPipeline u = sourceStage, p = sourceStage.nextStage, e = this;
                 u != e;
                 u = p, p = p.nextStage) {

                int thisOpFlags = p.sourceOrOpFlags;
                if (p.opIsStateful()) {
                    depth = 0;

                    if (StreamOpFlag.SHORT_CIRCUIT.isKnown(thisOpFlags)) {
                        // Clear the short circuit flag for next pipeline stage
                        // This stage encapsulates short-circuiting, the next
                        // stage may not have any short-circuit operations, and
                        // if so spliterator.forEachRemaining should be used
                        // for traversal
                        thisOpFlags = thisOpFlags & ~StreamOpFlag.IS_SHORT_CIRCUIT;
                    }

                    spliterator = p.opEvaluateParallelLazy(u, spliterator);

                    // Inject or clear SIZED on the source pipeline stage
                    // based on the stage's spliterator
                    thisOpFlags = spliterator.hasCharacteristics(Spliterator.SIZED)
                            ? (thisOpFlags & ~StreamOpFlag.NOT_SIZED) | StreamOpFlag.IS_SIZED
                            : (thisOpFlags & ~StreamOpFlag.IS_SIZED) | StreamOpFlag.NOT_SIZED;
                }
                p.depth = depth++;
                p.combinedFlags = StreamOpFlag.combineOpFlags(thisOpFlags, u.combinedFlags);
            }
        }

        if (terminalFlags != 0)  {
            // Apply flags from the terminal operation to last pipeline stage
            combinedFlags = StreamOpFlag.combineOpFlags(terminalFlags, combinedFlags);
        }

        return spliterator;
    }

    // PipelineHelper

    @Override
    final StreamShape getSourceShape() {
        @SuppressWarnings("rawtypes")
        AbstractPipeline p = AbstractPipeline.this;
        while (p.depth > 0) {
            p = p.previousStage;
        }
        return p.getOutputShape();
    }

    @Override
    final <P_IN> long exactOutputSizeIfKnown(Spliterator<P_IN> spliterator) {
        return StreamOpFlag.SIZED.isKnown(getStreamAndOpFlags()) ? spliterator.getExactSizeIfKnown() : -1;
    }

    @Override
    final <P_IN, S extends Sink<E_OUT>> S wrapAndCopyInto(S sink, Spliterator<P_IN> spliterator) {
        copyInto(wrapSink(Objects.requireNonNull(sink)), spliterator);
        return sink;
    }

    /**
     * 该方法从数据源Spliterator获取的元素，推入Sink 进行处理，
     * 如果有短路操作，在每个元素处理后会通过Sink.cancellationRequested()判断是否立即返回
     */
    @Override
    final <P_IN> void copyInto(Sink<P_IN> wrappedSink, Spliterator<P_IN> spliterator) {
        Objects.requireNonNull(wrappedSink);
        // 无短路操作
        if (!StreamOpFlag.SHORT_CIRCUIT.isKnown(getStreamAndOpFlags())) {
            // 通知开始遍历
            wrappedSink.begin(spliterator.getExactSizeIfKnown());
            // 依次处理每个元素
            spliterator.forEachRemaining(wrappedSink);
            // 通知结束遍历
            wrappedSink.end();
        } else {
            // 有短路操作
            copyIntoWithCancel(wrappedSink, spliterator);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    final <P_IN> void copyIntoWithCancel(Sink<P_IN> wrappedSink, Spliterator<P_IN> spliterator) {
        @SuppressWarnings({"rawtypes","unchecked"})
        AbstractPipeline p = AbstractPipeline.this;
        while (p.depth > 0) {
            p = p.previousStage;
        }
        wrappedSink.begin(spliterator.getExactSizeIfKnown());
        p.forEachWithCancel(spliterator, wrappedSink);
        wrappedSink.end();
    }

    @Override
    final int getStreamAndOpFlags() {
        return combinedFlags;
    }

    final boolean isOrdered() {
        return StreamOpFlag.ORDERED.isKnown(combinedFlags);
    }

    @Override
    @SuppressWarnings("unchecked")
    final <P_IN> Sink<P_IN> wrapSink(Sink<E_OUT> sink) {
        Objects.requireNonNull(sink);
        for ( @SuppressWarnings("rawtypes") AbstractPipeline p=AbstractPipeline.this; p.depth > 0; p=p.previousStage) {
            // 从自身stage开始，不断调用前一个stage的opWrapSink，直到头节点
            sink = p.opWrapSink(p.previousStage.combinedFlags, sink);
        }
        return (Sink<P_IN>) sink;
    }

    @Override
    @SuppressWarnings("unchecked")
    final <P_IN> Spliterator<E_OUT> wrapSpliterator(Spliterator<P_IN> sourceSpliterator) {
        if (depth == 0) {
            return (Spliterator<E_OUT>) sourceSpliterator;
        }
        else {
            return wrap(this, () -> sourceSpliterator, isParallel());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    final <P_IN> Node<E_OUT> evaluate(Spliterator<P_IN> spliterator,
                                      boolean flatten,
                                      IntFunction<E_OUT[]> generator) {
        if (isParallel()) {
            // @@@ Optimize if op of this pipeline stage is a stateful op
            return evaluateToNode(this, spliterator, flatten, generator);
        }
        else {
            Node.Builder<E_OUT> nb = makeNodeBuilder(
                    exactOutputSizeIfKnown(spliterator), generator);
            return wrapAndCopyInto(nb, spliterator).build();
        }
    }


    // Shape-specific abstract methods, implemented by XxxPipeline classes

    abstract StreamShape getOutputShape();

    abstract <P_IN> Node<E_OUT> evaluateToNode(PipelineHelper<E_OUT> helper,
                                               Spliterator<P_IN> spliterator,
                                               boolean flattenTree,
                                               IntFunction<E_OUT[]> generator);

    abstract <P_IN> Spliterator<E_OUT> wrap(PipelineHelper<E_OUT> ph,
                                            Supplier<Spliterator<P_IN>> supplier,
                                            boolean isParallel);

    abstract Spliterator<E_OUT> lazySpliterator(Supplier<? extends Spliterator<E_OUT>> supplier);

    abstract void forEachWithCancel(Spliterator<E_OUT> spliterator, Sink<E_OUT> sink);

    @Override
    abstract Node.Builder<E_OUT> makeNodeBuilder(long exactSizeIfKnown, IntFunction<E_OUT[]> generator);


    abstract boolean opIsStateful();
    // opWrapSink()方法的作用是将当前操作与下游 Sink 结合成新 Sink
    abstract Sink<E_IN> opWrapSink(int flags, Sink<E_OUT> sink);

    <P_IN> Node<E_OUT> opEvaluateParallel(PipelineHelper<E_OUT> helper,
                                          Spliterator<P_IN> spliterator,
                                          IntFunction<E_OUT[]> generator) {
        throw new UnsupportedOperationException("Parallel evaluation is not supported");
    }

    @SuppressWarnings("unchecked")
    <P_IN> Spliterator<E_OUT> opEvaluateParallelLazy(PipelineHelper<E_OUT> helper,
                                                     Spliterator<P_IN> spliterator) {
        return opEvaluateParallel(helper, spliterator, i -> (E_OUT[]) new Object[i]).spliterator();
    }
}
