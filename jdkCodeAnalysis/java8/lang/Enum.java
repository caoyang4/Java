package java.lang;

import java.io.Serializable;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
// 枚举类会继承该类
public abstract class Enum<E extends Enum<E>> implements Comparable<E>, Serializable {
    private final String name;

    public final String name() {
        return name;
    }
    // 枚举的索引
    private final int ordinal;

    public final int ordinal() {
        return ordinal;
    }

    protected Enum(String name, int ordinal) {
        this.name = name;
        this.ordinal = ordinal;
    }

    public String toString() {
        return name;
    }

    public final boolean equals(Object other) {
        return this==other;
    }

    public final int hashCode() {
        return super.hashCode();
    }

    protected final Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public final int compareTo(E o) {
        Enum<?> other = (Enum<?>)o;
        Enum<E> self = this;
        if (self.getClass() != other.getClass() && // optimization
            self.getDeclaringClass() != other.getDeclaringClass())
            throw new ClassCastException();
        return self.ordinal - other.ordinal;
    }

    @SuppressWarnings("unchecked")
    public final Class<E> getDeclaringClass() {
        Class<?> clazz = getClass();
        Class<?> zuper = clazz.getSuperclass();
        return (zuper == Enum.class) ? (Class<E>)clazz : (Class<E>)zuper;
    }

    public static <T extends Enum<T>> T valueOf(Class<T> enumType, String name) {
        T result = enumType.enumConstantDirectory().get(name);
        if (result != null)
            return result;
        if (name == null)
            throw new NullPointerException("Name is null");
        throw new IllegalArgumentException("No enum constant " + enumType.getCanonicalName() + "." + name);
    }

    protected final void finalize() { }

    // 此处不允许序列化创建枚举类型，单例的最安全实现
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        throw new InvalidObjectException("can't deserialize enum");
    }
    private void readObjectNoData() throws ObjectStreamException {
        throw new InvalidObjectException("can't deserialize enum");
    }
}
