package java.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 包装类
 * 主要解决 NullPointException
 */
public final class Optional<T> {
    private static final Optional<?> EMPTY = new Optional<>();
    // 如果非空则有值，如果是null则无值
    private final T value;
    // 创建一个空实例
    private Optional() {
        this.value = null;
    }
    // 返回一个容器里面为空的Optional对象，也就是说这个容器对象里面的那个值value肯定是为空的null
    public static<T> Optional<T> empty() {
        @SuppressWarnings("unchecked")
        Optional<T> t = (Optional<T>) EMPTY;
        return t;
    }

    private Optional(T value) {
        this.value = Objects.requireNonNull(value);
    }
    // 创建一个有值的Optional对象，传入值必须有值，不然就会抛出NullPointerException异常
    public static <T> Optional<T> of(T value) {
        return new Optional<>(value);
    }

    public static <T> Optional<T> ofNullable(T value) {
        return value == null ? empty() : of(value);
    }

    public T get() {
        // 如果value为null, 则抛出异常
        if (value == null) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    public boolean isPresent() {
        return value != null;
    }

    // 如果容器中的值不为空执行Consumer（消费者）函数
    public void ifPresent(Consumer<? super T> consumer) {
        if (value != null)
            consumer.accept(value);
    }

    public Optional<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        // 如果存在一个值与Predicate函数的操作匹配则返回值，如果不匹配返回一个空的容器
        if (!isPresent())
            return this;
        else
            return predicate.test(value) ? this : empty();
    }
    // 如果存在值，则将提供的映射函数应用于该值，如果结果为非null，则返回描述结果的Optional。
    // 否则返回空Optional
    public<U> Optional<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        if (!isPresent())
            return empty();
        else {
            return Optional.ofNullable(mapper.apply(value));
        }
    }

    public<U> Optional<U> flatMap(Function<? super T, Optional<U>> mapper) {
        Objects.requireNonNull(mapper);
        if (!isPresent())
            return empty();
        else {
            return Objects.requireNonNull(mapper.apply(value));
        }
    }
    // 如果存在值，则返回该值，否则返回other
    public T orElse(T other) {
        return value != null ? value : other;
    }
    // 如果存在值，则返回该值，否则返回由供应函数产生的结果。
    public T orElseGet(Supplier<? extends T> other) {
        return value != null ? value : other.get();
    }

    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        if (value != null) {
            return value;
        } else {
            throw exceptionSupplier.get();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Optional)) {
            return false;
        }

        Optional<?> other = (Optional<?>) obj;
        return Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return value != null ? String.format("Optional[%s]", value) : "Optional.empty";
    }
}
