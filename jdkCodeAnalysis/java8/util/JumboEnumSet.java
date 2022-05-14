package java.util;
/**
 * EnumSet的子类
 * JumboEnumSet适用于枚举值个数大于64个的枚举类，
 * 其底层实现跟RegularEnumSet一样都是根据位是否为1来判断该枚举值是否添加到了Set中，
 * 不过因为枚举值个数大于64个，无法用64位的long类型来记录所有的枚举值，
 * 所以将RegularEnumSet中long类型的elements改成了一个long类型数组，添加某个枚举值时，
 * 先将某个枚举值的ordinal属性除以64，算出该枚举值所属的elements数组索引，再用ordinal属性对64求余，将对应的位标识位1
 */
class JumboEnumSet<E extends Enum<E>> extends EnumSet<E> {
    private static final long serialVersionUID = 334349849919042784L;

    private long elements[];

    // Redundant - maintained for performance
    private int size = 0;

    JumboEnumSet(Class<E>elementType, Enum<?>[] universe) {
        super(elementType, universe);
        elements = new long[(universe.length + 63) >>> 6];
    }

    void addRange(E from, E to) {
        int fromIndex = from.ordinal() >>> 6;
        int toIndex = to.ordinal() >>> 6;

        if (fromIndex == toIndex) {
            elements[fromIndex] = (-1L >>>  (from.ordinal() - to.ordinal() - 1))
                            << from.ordinal();
        } else {
            elements[fromIndex] = (-1L << from.ordinal());
            for (int i = fromIndex + 1; i < toIndex; i++)
                elements[i] = -1;
            elements[toIndex] = -1L >>> (63 - to.ordinal());
        }
        size = to.ordinal() - from.ordinal() + 1;
    }

    void addAll() {
        for (int i = 0; i < elements.length; i++)
            elements[i] = -1;
        elements[elements.length - 1] >>>= -universe.length;
        size = universe.length;
    }

    void complement() {
        for (int i = 0; i < elements.length; i++)
            elements[i] = ~elements[i];
        elements[elements.length - 1] &= (-1L >>> -universe.length);
        size = universe.length - size;
    }

    public Iterator<E> iterator() {
        return new EnumSetIterator<>();
    }

    private class EnumSetIterator<E extends Enum<E>> implements Iterator<E> {
        long unseen;

        int unseenIndex = 0;

        long lastReturned = 0;

        int lastReturnedIndex = 0;

        EnumSetIterator() {
            unseen = elements[0];
        }

        @Override
        public boolean hasNext() {
            while (unseen == 0 && unseenIndex < elements.length - 1)
                unseen = elements[++unseenIndex];
            return unseen != 0;
        }

        @Override
        @SuppressWarnings("unchecked")
        public E next() {
            if (!hasNext())
                throw new NoSuchElementException();
            lastReturned = unseen & -unseen;
            lastReturnedIndex = unseenIndex;
            unseen -= lastReturned;
            return (E) universe[(lastReturnedIndex << 6)
                                + Long.numberOfTrailingZeros(lastReturned)];
        }

        @Override
        public void remove() {
            if (lastReturned == 0)
                throw new IllegalStateException();
            final long oldElements = elements[lastReturnedIndex];
            elements[lastReturnedIndex] &= ~lastReturned;
            if (oldElements != elements[lastReturnedIndex]) {
                size--;
            }
            lastReturned = 0;
        }
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean contains(Object e) {
        if (e == null)
            return false;
        Class<?> eClass = e.getClass();
        if (eClass != elementType && eClass.getSuperclass() != elementType)
            return false;

        int eOrdinal = ((Enum<?>)e).ordinal();
        return (elements[eOrdinal >>> 6] & (1L << eOrdinal)) != 0;
    }

    // Modification Operations

    public boolean add(E e) {
        typeCheck(e);

        int eOrdinal = e.ordinal();
        // 左移6位就是除以64，算出该枚举所属的elements数组索引
        int eWordNum = eOrdinal >>> 6;
        long oldElements = elements[eWordNum];
        // eOrdinal的值是大于64的，此处右移，实际移动的位数是eOrdinal对64求余的结果
        // 比如eOrdinal的值是68,1L<<68的结果就是1<<4,10000
        elements[eWordNum] |= (1L << eOrdinal);
        // 判断原elements数组索引的元素是否发生改变，如果改变则说明添加的枚举值原来不存在
        boolean result = (elements[eWordNum] != oldElements);
        // 为true说明添加了一个新元素，size加1
        if (result)
            size++;
        return result;
    }

    public boolean remove(Object e) {
        if (e == null)
            return false;
        Class<?> eClass = e.getClass();
        if (eClass != elementType && eClass.getSuperclass() != elementType)
            return false;
        int eOrdinal = ((Enum<?>)e).ordinal();
        int eWordNum = eOrdinal >>> 6;

        long oldElements = elements[eWordNum];
        elements[eWordNum] &= ~(1L << eOrdinal);
        boolean result = (elements[eWordNum] != oldElements);
        if (result)
            size--;
        return result;
    }

    // Bulk Operations

    public boolean containsAll(Collection<?> c) {
        if (!(c instanceof JumboEnumSet))
            return super.containsAll(c);

        JumboEnumSet<?> es = (JumboEnumSet<?>)c;
        if (es.elementType != elementType)
            return es.isEmpty();

        for (int i = 0; i < elements.length; i++)
            if ((es.elements[i] & ~elements[i]) != 0)
                return false;
        return true;
    }

    public boolean addAll(Collection<? extends E> c) {
        if (!(c instanceof JumboEnumSet))
            return super.addAll(c);

        JumboEnumSet<?> es = (JumboEnumSet<?>)c;
        if (es.elementType != elementType) {
            if (es.isEmpty())
                return false;
            else
                throw new ClassCastException(
                    es.elementType + " != " + elementType);
        }

        for (int i = 0; i < elements.length; i++)
            elements[i] |= es.elements[i];
        return recalculateSize();
    }

    public boolean removeAll(Collection<?> c) {
        if (!(c instanceof JumboEnumSet))
            return super.removeAll(c);

        JumboEnumSet<?> es = (JumboEnumSet<?>)c;
        if (es.elementType != elementType)
            return false;

        for (int i = 0; i < elements.length; i++)
            elements[i] &= ~es.elements[i];
        return recalculateSize();
    }

    public boolean retainAll(Collection<?> c) {
        if (!(c instanceof JumboEnumSet))
            return super.retainAll(c);

        JumboEnumSet<?> es = (JumboEnumSet<?>)c;
        if (es.elementType != elementType) {
            boolean changed = (size != 0);
            clear();
            return changed;
        }

        for (int i = 0; i < elements.length; i++)
            elements[i] &= es.elements[i];
        return recalculateSize();
    }

    public void clear() {
        Arrays.fill(elements, 0);
        size = 0;
    }

    public boolean equals(Object o) {
        if (!(o instanceof JumboEnumSet))
            return super.equals(o);

        JumboEnumSet<?> es = (JumboEnumSet<?>)o;
        if (es.elementType != elementType)
            return size == 0 && es.size == 0;

        return Arrays.equals(es.elements, elements);
    }

    private boolean recalculateSize() {
        int oldSize = size;
        size = 0;
        for (long elt : elements)
            size += Long.bitCount(elt);

        return size != oldSize;
    }

    public EnumSet<E> clone() {
        JumboEnumSet<E> result = (JumboEnumSet<E>) super.clone();
        result.elements = result.elements.clone();
        return result;
    }
}
