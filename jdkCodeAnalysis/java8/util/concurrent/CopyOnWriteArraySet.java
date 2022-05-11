package java.util.concurrent;
import java.util.Collection;
import java.util.Set;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.function.Consumer;

/**
 * CopyOnWriteArraySet内部是构造了一个CopyOnWriteArrayList
 * 内部的 add 是调用CopyOnWriteArrayList的addIfAbsent，即不存在才添加，保证元素去重
 */
public class CopyOnWriteArraySet<E> extends AbstractSet<E> implements java.io.Serializable {
    private static final long serialVersionUID = 5457747651344034263L;

    private final CopyOnWriteArrayList<E> al;

    public CopyOnWriteArraySet() {
        al = new CopyOnWriteArrayList<E>();
    }

    public CopyOnWriteArraySet(Collection<? extends E> c) {
        if (c.getClass() == CopyOnWriteArraySet.class) {
            @SuppressWarnings("unchecked") CopyOnWriteArraySet<E> cc = (CopyOnWriteArraySet<E>)c;
            al = new CopyOnWriteArrayList<E>(cc.al);
        }
        else {
            al = new CopyOnWriteArrayList<E>();
            al.addAllAbsent(c);
        }
    }

    public int size() {
        return al.size();
    }

    public boolean isEmpty() {
        return al.isEmpty();
    }

    public boolean contains(Object o) {
        return al.contains(o);
    }

    public Object[] toArray() {
        return al.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return al.toArray(a);
    }

    public void clear() {
        al.clear();
    }

    public boolean remove(Object o) {
        return al.remove(o);
    }

    public boolean add(E e) {
        return al.addIfAbsent(e);
    }

    public boolean containsAll(Collection<?> c) {
        return al.containsAll(c);
    }

    public boolean addAll(Collection<? extends E> c) {
        return al.addAllAbsent(c) > 0;
    }

    public boolean removeAll(Collection<?> c) {
        return al.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return al.retainAll(c);
    }

    public Iterator<E> iterator() {
        return al.iterator();
    }

    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Set))
            return false;
        Set<?> set = (Set<?>)(o);
        Iterator<?> it = set.iterator();

        // Uses O(n^2) algorithm that is only appropriate
        // for small sets, which CopyOnWriteArraySets should be.

        //  Use a single snapshot of underlying array
        Object[] elements = al.getArray();
        int len = elements.length;
        // Mark matched elements to avoid re-checking
        boolean[] matched = new boolean[len];
        int k = 0;
        outer: while (it.hasNext()) {
            if (++k > len)
                return false;
            Object x = it.next();
            for (int i = 0; i < len; ++i) {
                if (!matched[i] && eq(x, elements[i])) {
                    matched[i] = true;
                    continue outer;
                }
            }
            return false;
        }
        return k == len;
    }

    public boolean removeIf(Predicate<? super E> filter) {
        return al.removeIf(filter);
    }

    public void forEach(Consumer<? super E> action) {
        al.forEach(action);
    }

    public Spliterator<E> spliterator() {
        return Spliterators.spliterator
            (al.getArray(), Spliterator.IMMUTABLE | Spliterator.DISTINCT);
    }

    private static boolean eq(Object o1, Object o2) {
        return (o1 == null) ? o2 == null : o1.equals(o2);
    }
}
