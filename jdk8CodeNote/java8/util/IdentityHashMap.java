package java.util;

import java.lang.reflect.Array;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import sun.misc.SharedSecrets;


public class IdentityHashMap<K,V> extends AbstractMap<K,V> implements Map<K,V>, java.io.Serializable, Cloneable
{
    private static final int DEFAULT_CAPACITY = 32;

    private static final int MINIMUM_CAPACITY = 4;

    private static final int MAXIMUM_CAPACITY = 1 << 29;

    transient Object[] table; // non-private to simplify nested class access

    int size;

    transient int modCount;

    static final Object NULL_KEY = new Object();

    private static Object maskNull(Object key) {
        return (key == null ? NULL_KEY : key);
    }

    static final Object unmaskNull(Object key) {
        return (key == NULL_KEY ? null : key);
    }

    public IdentityHashMap() {
        init(DEFAULT_CAPACITY);
    }

    public IdentityHashMap(int expectedMaxSize) {
        if (expectedMaxSize < 0)
            throw new IllegalArgumentException("expectedMaxSize is negative: " + expectedMaxSize);
        init(capacity(expectedMaxSize));
    }

    private static int capacity(int expectedMaxSize) {
        // assert expectedMaxSize >= 0;
        return
            (expectedMaxSize > MAXIMUM_CAPACITY / 3) ? MAXIMUM_CAPACITY :
            (expectedMaxSize <= 2 * MINIMUM_CAPACITY / 3) ? MINIMUM_CAPACITY :
            Integer.highestOneBit(expectedMaxSize + (expectedMaxSize << 1));
    }

    private void init(int initCapacity) {
        // assert (initCapacity & -initCapacity) == initCapacity; // power of 2
        // assert initCapacity >= MINIMUM_CAPACITY;
        // assert initCapacity <= MAXIMUM_CAPACITY;

        table = new Object[2 * initCapacity];
    }

    public IdentityHashMap(Map<? extends K, ? extends V> m) {
        // Allow for a bit of growth
        this((int) ((1 + m.size()) * 1.1));
        putAll(m);
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    private static int hash(Object x, int length) {
        int h = System.identityHashCode(x);
        // Multiply by -127, and left-shift to use least bit as part of hash
        return ((h << 1) - (h << 8)) & (length - 1);
    }

    private static int nextKeyIndex(int i, int len) {
        return (i + 2 < len ? i + 2 : 0);
    }

    @SuppressWarnings("unchecked")
    public V get(Object key) {
        Object k = maskNull(key);
        Object[] tab = table;
        int len = tab.length;
        int i = hash(k, len);
        while (true) {
            Object item = tab[i];
            if (item == k)
                return (V) tab[i + 1];
            if (item == null)
                return null;
            i = nextKeyIndex(i, len);
        }
    }

    public boolean containsKey(Object key) {
        Object k = maskNull(key);
        Object[] tab = table;
        int len = tab.length;
        int i = hash(k, len);
        while (true) {
            Object item = tab[i];
            if (item == k)
                return true;
            if (item == null)
                return false;
            i = nextKeyIndex(i, len);
        }
    }

    public boolean containsValue(Object value) {
        Object[] tab = table;
        for (int i = 1; i < tab.length; i += 2)
            if (tab[i] == value && tab[i - 1] != null)
                return true;

        return false;
    }

    private boolean containsMapping(Object key, Object value) {
        Object k = maskNull(key);
        Object[] tab = table;
        int len = tab.length;
        int i = hash(k, len);
        while (true) {
            Object item = tab[i];
            if (item == k)
                return tab[i + 1] == value;
            if (item == null)
                return false;
            i = nextKeyIndex(i, len);
        }
    }

    public V put(K key, V value) {
        final Object k = maskNull(key);

        retryAfterResize: for (;;) {
            final Object[] tab = table;
            final int len = tab.length;
            int i = hash(k, len);

            for (Object item; (item = tab[i]) != null;
                 i = nextKeyIndex(i, len)) {
                if (item == k) {
                    @SuppressWarnings("unchecked")
                        V oldValue = (V) tab[i + 1];
                    tab[i + 1] = value;
                    return oldValue;
                }
            }

            final int s = size + 1;
            // Use optimized form of 3 * s.
            // Next capacity is len, 2 * current capacity.
            if (s + (s << 1) > len && resize(len))
                continue retryAfterResize;

            modCount++;
            tab[i] = k;
            tab[i + 1] = value;
            size = s;
            return null;
        }
    }

    private boolean resize(int newCapacity) {
        // assert (newCapacity & -newCapacity) == newCapacity; // power of 2
        int newLength = newCapacity * 2;

        Object[] oldTable = table;
        int oldLength = oldTable.length;
        if (oldLength == 2 * MAXIMUM_CAPACITY) { // can't expand any further
            if (size == MAXIMUM_CAPACITY - 1)
                throw new IllegalStateException("Capacity exhausted.");
            return false;
        }
        if (oldLength >= newLength)
            return false;

        Object[] newTable = new Object[newLength];

        for (int j = 0; j < oldLength; j += 2) {
            Object key = oldTable[j];
            if (key != null) {
                Object value = oldTable[j+1];
                oldTable[j] = null;
                oldTable[j+1] = null;
                int i = hash(key, newLength);
                while (newTable[i] != null)
                    i = nextKeyIndex(i, newLength);
                newTable[i] = key;
                newTable[i + 1] = value;
            }
        }
        table = newTable;
        return true;
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        int n = m.size();
        if (n == 0)
            return;
        if (n > size)
            resize(capacity(n)); // conservatively pre-expand

        for (Entry<? extends K, ? extends V> e : m.entrySet())
            put(e.getKey(), e.getValue());
    }

    public V remove(Object key) {
        Object k = maskNull(key);
        Object[] tab = table;
        int len = tab.length;
        int i = hash(k, len);

        while (true) {
            Object item = tab[i];
            if (item == k) {
                modCount++;
                size--;
                @SuppressWarnings("unchecked")
                    V oldValue = (V) tab[i + 1];
                tab[i + 1] = null;
                tab[i] = null;
                closeDeletion(i);
                return oldValue;
            }
            if (item == null)
                return null;
            i = nextKeyIndex(i, len);
        }
    }

    private boolean removeMapping(Object key, Object value) {
        Object k = maskNull(key);
        Object[] tab = table;
        int len = tab.length;
        int i = hash(k, len);

        while (true) {
            Object item = tab[i];
            if (item == k) {
                if (tab[i + 1] != value)
                    return false;
                modCount++;
                size--;
                tab[i] = null;
                tab[i + 1] = null;
                closeDeletion(i);
                return true;
            }
            if (item == null)
                return false;
            i = nextKeyIndex(i, len);
        }
    }

    private void closeDeletion(int d) {
        // Adapted from Knuth Section 6.4 Algorithm R
        Object[] tab = table;
        int len = tab.length;

        // Look for items to swap into newly vacated slot
        // starting at index immediately following deletion,
        // and continuing until a null slot is seen, indicating
        // the end of a run of possibly-colliding keys.
        Object item;
        for (int i = nextKeyIndex(d, len); (item = tab[i]) != null;
             i = nextKeyIndex(i, len) ) {
            // The following test triggers if the item at slot i (which
            // hashes to be at slot r) should take the spot vacated by d.
            // If so, we swap it in, and then continue with d now at the
            // newly vacated i.  This process will terminate when we hit
            // the null slot at the end of this run.
            // The test is messy because we are using a circular table.
            int r = hash(item, len);
            if ((i < r && (r <= d || d <= i)) || (r <= d && d <= i)) {
                tab[d] = item;
                tab[d + 1] = tab[i + 1];
                tab[i] = null;
                tab[i + 1] = null;
                d = i;
            }
        }
    }

    public void clear() {
        modCount++;
        Object[] tab = table;
        for (int i = 0; i < tab.length; i++)
            tab[i] = null;
        size = 0;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof IdentityHashMap) {
            IdentityHashMap<?,?> m = (IdentityHashMap<?,?>) o;
            if (m.size() != size)
                return false;

            Object[] tab = m.table;
            for (int i = 0; i < tab.length; i+=2) {
                Object k = tab[i];
                if (k != null && !containsMapping(k, tab[i + 1]))
                    return false;
            }
            return true;
        } else if (o instanceof Map) {
            Map<?,?> m = (Map<?,?>)o;
            return entrySet().equals(m.entrySet());
        } else {
            return false;  // o is not a Map
        }
    }

    public int hashCode() {
        int result = 0;
        Object[] tab = table;
        for (int i = 0; i < tab.length; i +=2) {
            Object key = tab[i];
            if (key != null) {
                Object k = unmaskNull(key);
                result += System.identityHashCode(k) ^
                          System.identityHashCode(tab[i + 1]);
            }
        }
        return result;
    }

    public Object clone() {
        try {
            IdentityHashMap<?,?> m = (IdentityHashMap<?,?>) super.clone();
            m.entrySet = null;
            m.table = table.clone();
            return m;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    private abstract class IdentityHashMapIterator<T> implements Iterator<T> {
        int index = (size != 0 ? 0 : table.length); // current slot.
        int expectedModCount = modCount; // to support fast-fail
        int lastReturnedIndex = -1;      // to allow remove()
        boolean indexValid; // To avoid unnecessary next computation
        Object[] traversalTable = table; // reference to main table or copy

        public boolean hasNext() {
            Object[] tab = traversalTable;
            for (int i = index; i < tab.length; i+=2) {
                Object key = tab[i];
                if (key != null) {
                    index = i;
                    return indexValid = true;
                }
            }
            index = tab.length;
            return false;
        }

        protected int nextIndex() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            if (!indexValid && !hasNext())
                throw new NoSuchElementException();

            indexValid = false;
            lastReturnedIndex = index;
            index += 2;
            return lastReturnedIndex;
        }

        public void remove() {
            if (lastReturnedIndex == -1)
                throw new IllegalStateException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();

            expectedModCount = ++modCount;
            int deletedSlot = lastReturnedIndex;
            lastReturnedIndex = -1;
            // back up index to revisit new contents after deletion
            index = deletedSlot;
            indexValid = false;

            // Removal code proceeds as in closeDeletion except that
            // it must catch the rare case where an element already
            // seen is swapped into a vacant slot that will be later
            // traversed by this iterator. We cannot allow future
            // next() calls to return it again.  The likelihood of
            // this occurring under 2/3 load factor is very slim, but
            // when it does happen, we must make a copy of the rest of
            // the table to use for the rest of the traversal. Since
            // this can only happen when we are near the end of the table,
            // even in these rare cases, this is not very expensive in
            // time or space.

            Object[] tab = traversalTable;
            int len = tab.length;

            int d = deletedSlot;
            Object key = tab[d];
            tab[d] = null;        // vacate the slot
            tab[d + 1] = null;

            // If traversing a copy, remove in real table.
            // We can skip gap-closure on copy.
            if (tab != IdentityHashMap.this.table) {
                IdentityHashMap.this.remove(key);
                expectedModCount = modCount;
                return;
            }

            size--;

            Object item;
            for (int i = nextKeyIndex(d, len); (item = tab[i]) != null;
                 i = nextKeyIndex(i, len)) {
                int r = hash(item, len);
                // See closeDeletion for explanation of this conditional
                if ((i < r && (r <= d || d <= i)) ||
                    (r <= d && d <= i)) {

                    // If we are about to swap an already-seen element
                    // into a slot that may later be returned by next(),
                    // then clone the rest of table for use in future
                    // next() calls. It is OK that our copy will have
                    // a gap in the "wrong" place, since it will never
                    // be used for searching anyway.

                    if (i < deletedSlot && d >= deletedSlot &&
                        traversalTable == IdentityHashMap.this.table) {
                        int remaining = len - deletedSlot;
                        Object[] newTable = new Object[remaining];
                        System.arraycopy(tab, deletedSlot,
                                         newTable, 0, remaining);
                        traversalTable = newTable;
                        index = 0;
                    }

                    tab[d] = item;
                    tab[d + 1] = tab[i + 1];
                    tab[i] = null;
                    tab[i + 1] = null;
                    d = i;
                }
            }
        }
    }

    private class KeyIterator extends IdentityHashMapIterator<K> {
        @SuppressWarnings("unchecked")
        public K next() {
            return (K) unmaskNull(traversalTable[nextIndex()]);
        }
    }

    private class ValueIterator extends IdentityHashMapIterator<V> {
        @SuppressWarnings("unchecked")
        public V next() {
            return (V) traversalTable[nextIndex() + 1];
        }
    }

    private class EntryIterator
        extends IdentityHashMapIterator<Map.Entry<K,V>>
    {
        private Entry lastReturnedEntry;

        public Map.Entry<K,V> next() {
            lastReturnedEntry = new Entry(nextIndex());
            return lastReturnedEntry;
        }

        public void remove() {
            lastReturnedIndex =
                ((null == lastReturnedEntry) ? -1 : lastReturnedEntry.index);
            super.remove();
            lastReturnedEntry.index = lastReturnedIndex;
            lastReturnedEntry = null;
        }

        private class Entry implements Map.Entry<K,V> {
            private int index;

            private Entry(int index) {
                this.index = index;
            }

            @SuppressWarnings("unchecked")
            public K getKey() {
                checkIndexForEntryUse();
                return (K) unmaskNull(traversalTable[index]);
            }

            @SuppressWarnings("unchecked")
            public V getValue() {
                checkIndexForEntryUse();
                return (V) traversalTable[index+1];
            }

            @SuppressWarnings("unchecked")
            public V setValue(V value) {
                checkIndexForEntryUse();
                V oldValue = (V) traversalTable[index+1];
                traversalTable[index+1] = value;
                // if shadowing, force into main table
                if (traversalTable != IdentityHashMap.this.table)
                    put((K) traversalTable[index], value);
                return oldValue;
            }

            public boolean equals(Object o) {
                if (index < 0)
                    return super.equals(o);

                if (!(o instanceof Map.Entry))
                    return false;
                Map.Entry<?,?> e = (Map.Entry<?,?>)o;
                return (e.getKey() == unmaskNull(traversalTable[index]) &&
                       e.getValue() == traversalTable[index+1]);
            }

            public int hashCode() {
                if (lastReturnedIndex < 0)
                    return super.hashCode();

                return (System.identityHashCode(unmaskNull(traversalTable[index])) ^
                       System.identityHashCode(traversalTable[index+1]));
            }

            public String toString() {
                if (index < 0)
                    return super.toString();

                return (unmaskNull(traversalTable[index]) + "="
                        + traversalTable[index+1]);
            }

            private void checkIndexForEntryUse() {
                if (index < 0)
                    throw new IllegalStateException("Entry was removed");
            }
        }
    }

    // Views

    private transient Set<Map.Entry<K,V>> entrySet;

    public Set<K> keySet() {
        Set<K> ks = keySet;
        if (ks == null) {
            ks = new KeySet();
            keySet = ks;
        }
        return ks;
    }

    private class KeySet extends AbstractSet<K> {
        public Iterator<K> iterator() {
            return new KeyIterator();
        }
        public int size() {
            return size;
        }
        public boolean contains(Object o) {
            return containsKey(o);
        }
        public boolean remove(Object o) {
            int oldSize = size;
            IdentityHashMap.this.remove(o);
            return size != oldSize;
        }
        /*
         * Must revert from AbstractSet's impl to AbstractCollection's, as
         * the former contains an optimization that results in incorrect
         * behavior when c is a smaller "normal" (non-identity-based) Set.
         */
        public boolean removeAll(Collection<?> c) {
            Objects.requireNonNull(c);
            boolean modified = false;
            for (Iterator<K> i = iterator(); i.hasNext(); ) {
                if (c.contains(i.next())) {
                    i.remove();
                    modified = true;
                }
            }
            return modified;
        }
        public void clear() {
            IdentityHashMap.this.clear();
        }
        public int hashCode() {
            int result = 0;
            for (K key : this)
                result += System.identityHashCode(key);
            return result;
        }
        public Object[] toArray() {
            return toArray(new Object[0]);
        }
        @SuppressWarnings("unchecked")
        public <T> T[] toArray(T[] a) {
            int expectedModCount = modCount;
            int size = size();
            if (a.length < size)
                a = (T[]) Array.newInstance(a.getClass().getComponentType(), size);
            Object[] tab = table;
            int ti = 0;
            for (int si = 0; si < tab.length; si += 2) {
                Object key;
                if ((key = tab[si]) != null) { // key present ?
                    // more elements than expected -> concurrent modification from other thread
                    if (ti >= size) {
                        throw new ConcurrentModificationException();
                    }
                    a[ti++] = (T) unmaskNull(key); // unmask key
                }
            }
            // fewer elements than expected or concurrent modification from other thread detected
            if (ti < size || expectedModCount != modCount) {
                throw new ConcurrentModificationException();
            }
            // final null marker as per spec
            if (ti < a.length) {
                a[ti] = null;
            }
            return a;
        }

        public Spliterator<K> spliterator() {
            return new KeySpliterator<>(IdentityHashMap.this, 0, -1, 0, 0);
        }
    }

    public Collection<V> values() {
        Collection<V> vs = values;
        if (vs == null) {
            vs = new Values();
            values = vs;
        }
        return vs;
    }

    private class Values extends AbstractCollection<V> {
        public Iterator<V> iterator() {
            return new ValueIterator();
        }
        public int size() {
            return size;
        }
        public boolean contains(Object o) {
            return containsValue(o);
        }
        public boolean remove(Object o) {
            for (Iterator<V> i = iterator(); i.hasNext(); ) {
                if (i.next() == o) {
                    i.remove();
                    return true;
                }
            }
            return false;
        }
        public void clear() {
            IdentityHashMap.this.clear();
        }
        public Object[] toArray() {
            return toArray(new Object[0]);
        }
        @SuppressWarnings("unchecked")
        public <T> T[] toArray(T[] a) {
            int expectedModCount = modCount;
            int size = size();
            if (a.length < size)
                a = (T[]) Array.newInstance(a.getClass().getComponentType(), size);
            Object[] tab = table;
            int ti = 0;
            for (int si = 0; si < tab.length; si += 2) {
                if (tab[si] != null) { // key present ?
                    // more elements than expected -> concurrent modification from other thread
                    if (ti >= size) {
                        throw new ConcurrentModificationException();
                    }
                    a[ti++] = (T) tab[si+1]; // copy value
                }
            }
            // fewer elements than expected or concurrent modification from other thread detected
            if (ti < size || expectedModCount != modCount) {
                throw new ConcurrentModificationException();
            }
            // final null marker as per spec
            if (ti < a.length) {
                a[ti] = null;
            }
            return a;
        }

        public Spliterator<V> spliterator() {
            return new ValueSpliterator<>(IdentityHashMap.this, 0, -1, 0, 0);
        }
    }

    public Set<Map.Entry<K,V>> entrySet() {
        Set<Map.Entry<K,V>> es = entrySet;
        if (es != null)
            return es;
        else
            return entrySet = new EntrySet();
    }

    private class EntrySet extends AbstractSet<Map.Entry<K,V>> {
        public Iterator<Map.Entry<K,V>> iterator() {
            return new EntryIterator();
        }
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?,?> entry = (Map.Entry<?,?>)o;
            return containsMapping(entry.getKey(), entry.getValue());
        }
        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?,?> entry = (Map.Entry<?,?>)o;
            return removeMapping(entry.getKey(), entry.getValue());
        }
        public int size() {
            return size;
        }
        public void clear() {
            IdentityHashMap.this.clear();
        }
        /*
         * Must revert from AbstractSet's impl to AbstractCollection's, as
         * the former contains an optimization that results in incorrect
         * behavior when c is a smaller "normal" (non-identity-based) Set.
         */
        public boolean removeAll(Collection<?> c) {
            Objects.requireNonNull(c);
            boolean modified = false;
            for (Iterator<Map.Entry<K,V>> i = iterator(); i.hasNext(); ) {
                if (c.contains(i.next())) {
                    i.remove();
                    modified = true;
                }
            }
            return modified;
        }

        public Object[] toArray() {
            return toArray(new Object[0]);
        }

        @SuppressWarnings("unchecked")
        public <T> T[] toArray(T[] a) {
            int expectedModCount = modCount;
            int size = size();
            if (a.length < size)
                a = (T[]) Array.newInstance(a.getClass().getComponentType(), size);
            Object[] tab = table;
            int ti = 0;
            for (int si = 0; si < tab.length; si += 2) {
                Object key;
                if ((key = tab[si]) != null) { // key present ?
                    // more elements than expected -> concurrent modification from other thread
                    if (ti >= size) {
                        throw new ConcurrentModificationException();
                    }
                    a[ti++] = (T) new AbstractMap.SimpleEntry<>(unmaskNull(key), tab[si + 1]);
                }
            }
            // fewer elements than expected or concurrent modification from other thread detected
            if (ti < size || expectedModCount != modCount) {
                throw new ConcurrentModificationException();
            }
            // final null marker as per spec
            if (ti < a.length) {
                a[ti] = null;
            }
            return a;
        }

        public Spliterator<Map.Entry<K,V>> spliterator() {
            return new EntrySpliterator<>(IdentityHashMap.this, 0, -1, 0, 0);
        }
    }


    private static final long serialVersionUID = 8188218128353913216L;

    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException  {
        // Write out and any hidden stuff
        s.defaultWriteObject();

        // Write out size (number of Mappings)
        s.writeInt(size);

        // Write out keys and values (alternating)
        Object[] tab = table;
        for (int i = 0; i < tab.length; i += 2) {
            Object key = tab[i];
            if (key != null) {
                s.writeObject(unmaskNull(key));
                s.writeObject(tab[i + 1]);
            }
        }
    }

    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException  {
        // Read in any hidden stuff
        s.defaultReadObject();

        // Read in size (number of Mappings)
        int size = s.readInt();
        if (size < 0)
            throw new java.io.StreamCorruptedException
                ("Illegal mappings count: " + size);
        int cap = capacity(size);
        SharedSecrets.getJavaOISAccess().checkArray(s, Object[].class, cap);
        init(cap);

        // Read the keys and values, and put the mappings in the table
        for (int i=0; i<size; i++) {
            @SuppressWarnings("unchecked")
                K key = (K) s.readObject();
            @SuppressWarnings("unchecked")
                V value = (V) s.readObject();
            putForCreate(key, value);
        }
    }

    private void putForCreate(K key, V value)
        throws java.io.StreamCorruptedException
    {
        Object k = maskNull(key);
        Object[] tab = table;
        int len = tab.length;
        int i = hash(k, len);

        Object item;
        while ( (item = tab[i]) != null) {
            if (item == k)
                throw new java.io.StreamCorruptedException();
            i = nextKeyIndex(i, len);
        }
        tab[i] = k;
        tab[i + 1] = value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        Objects.requireNonNull(action);
        int expectedModCount = modCount;

        Object[] t = table;
        for (int index = 0; index < t.length; index += 2) {
            Object k = t[index];
            if (k != null) {
                action.accept((K) unmaskNull(k), (V) t[index + 1]);
            }

            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Objects.requireNonNull(function);
        int expectedModCount = modCount;

        Object[] t = table;
        for (int index = 0; index < t.length; index += 2) {
            Object k = t[index];
            if (k != null) {
                t[index + 1] = function.apply((K) unmaskNull(k), (V) t[index + 1]);
            }

            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }
    }

    static class IdentityHashMapSpliterator<K,V> {
        final IdentityHashMap<K,V> map;
        int index;             // current index, modified on advance/split
        int fence;             // -1 until first use; then one past last index
        int est;               // size estimate
        int expectedModCount;  // initialized when fence set

        IdentityHashMapSpliterator(IdentityHashMap<K,V> map, int origin,
                                   int fence, int est, int expectedModCount) {
            this.map = map;
            this.index = origin;
            this.fence = fence;
            this.est = est;
            this.expectedModCount = expectedModCount;
        }

        final int getFence() { // initialize fence and size on first use
            int hi;
            if ((hi = fence) < 0) {
                est = map.size;
                expectedModCount = map.modCount;
                hi = fence = map.table.length;
            }
            return hi;
        }

        public final long estimateSize() {
            getFence(); // force init
            return (long) est;
        }
    }

    static final class KeySpliterator<K,V>
        extends IdentityHashMapSpliterator<K,V>
        implements Spliterator<K> {
        KeySpliterator(IdentityHashMap<K,V> map, int origin, int fence, int est,
                       int expectedModCount) {
            super(map, origin, fence, est, expectedModCount);
        }

        public KeySpliterator<K,V> trySplit() {
            int hi = getFence(), lo = index, mid = ((lo + hi) >>> 1) & ~1;
            return (lo >= mid) ? null :
                new KeySpliterator<K,V>(map, lo, index = mid, est >>>= 1,
                                        expectedModCount);
        }

        @SuppressWarnings("unchecked")
        public void forEachRemaining(Consumer<? super K> action) {
            if (action == null)
                throw new NullPointerException();
            int i, hi, mc; Object key;
            IdentityHashMap<K,V> m; Object[] a;
            if ((m = map) != null && (a = m.table) != null &&
                (i = index) >= 0 && (index = hi = getFence()) <= a.length) {
                for (; i < hi; i += 2) {
                    if ((key = a[i]) != null)
                        action.accept((K)unmaskNull(key));
                }
                if (m.modCount == expectedModCount)
                    return;
            }
            throw new ConcurrentModificationException();
        }

        @SuppressWarnings("unchecked")
        public boolean tryAdvance(Consumer<? super K> action) {
            if (action == null)
                throw new NullPointerException();
            Object[] a = map.table;
            int hi = getFence();
            while (index < hi) {
                Object key = a[index];
                index += 2;
                if (key != null) {
                    action.accept((K)unmaskNull(key));
                    if (map.modCount != expectedModCount)
                        throw new ConcurrentModificationException();
                    return true;
                }
            }
            return false;
        }

        public int characteristics() {
            return (fence < 0 || est == map.size ? SIZED : 0) | Spliterator.DISTINCT;
        }
    }

    static final class ValueSpliterator<K,V>
        extends IdentityHashMapSpliterator<K,V>
        implements Spliterator<V> {
        ValueSpliterator(IdentityHashMap<K,V> m, int origin, int fence, int est,
                         int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public ValueSpliterator<K,V> trySplit() {
            int hi = getFence(), lo = index, mid = ((lo + hi) >>> 1) & ~1;
            return (lo >= mid) ? null :
                new ValueSpliterator<K,V>(map, lo, index = mid, est >>>= 1,
                                          expectedModCount);
        }

        public void forEachRemaining(Consumer<? super V> action) {
            if (action == null)
                throw new NullPointerException();
            int i, hi, mc;
            IdentityHashMap<K,V> m; Object[] a;
            if ((m = map) != null && (a = m.table) != null &&
                (i = index) >= 0 && (index = hi = getFence()) <= a.length) {
                for (; i < hi; i += 2) {
                    if (a[i] != null) {
                        @SuppressWarnings("unchecked") V v = (V)a[i+1];
                        action.accept(v);
                    }
                }
                if (m.modCount == expectedModCount)
                    return;
            }
            throw new ConcurrentModificationException();
        }

        public boolean tryAdvance(Consumer<? super V> action) {
            if (action == null)
                throw new NullPointerException();
            Object[] a = map.table;
            int hi = getFence();
            while (index < hi) {
                Object key = a[index];
                @SuppressWarnings("unchecked") V v = (V)a[index+1];
                index += 2;
                if (key != null) {
                    action.accept(v);
                    if (map.modCount != expectedModCount)
                        throw new ConcurrentModificationException();
                    return true;
                }
            }
            return false;
        }

        public int characteristics() {
            return (fence < 0 || est == map.size ? SIZED : 0);
        }

    }

    static final class EntrySpliterator<K,V>
        extends IdentityHashMapSpliterator<K,V>
        implements Spliterator<Map.Entry<K,V>> {
        EntrySpliterator(IdentityHashMap<K,V> m, int origin, int fence, int est,
                         int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public EntrySpliterator<K,V> trySplit() {
            int hi = getFence(), lo = index, mid = ((lo + hi) >>> 1) & ~1;
            return (lo >= mid) ? null :
                new EntrySpliterator<K,V>(map, lo, index = mid, est >>>= 1,
                                          expectedModCount);
        }

        public void forEachRemaining(Consumer<? super Map.Entry<K, V>> action) {
            if (action == null)
                throw new NullPointerException();
            int i, hi, mc;
            IdentityHashMap<K,V> m; Object[] a;
            if ((m = map) != null && (a = m.table) != null &&
                (i = index) >= 0 && (index = hi = getFence()) <= a.length) {
                for (; i < hi; i += 2) {
                    Object key = a[i];
                    if (key != null) {
                        @SuppressWarnings("unchecked") K k =
                            (K)unmaskNull(key);
                        @SuppressWarnings("unchecked") V v = (V)a[i+1];
                        action.accept
                            (new AbstractMap.SimpleImmutableEntry<K,V>(k, v));

                    }
                }
                if (m.modCount == expectedModCount)
                    return;
            }
            throw new ConcurrentModificationException();
        }

        public boolean tryAdvance(Consumer<? super Map.Entry<K,V>> action) {
            if (action == null)
                throw new NullPointerException();
            Object[] a = map.table;
            int hi = getFence();
            while (index < hi) {
                Object key = a[index];
                @SuppressWarnings("unchecked") V v = (V)a[index+1];
                index += 2;
                if (key != null) {
                    @SuppressWarnings("unchecked") K k =
                        (K)unmaskNull(key);
                    action.accept
                        (new AbstractMap.SimpleImmutableEntry<K,V>(k, v));
                    if (map.modCount != expectedModCount)
                        throw new ConcurrentModificationException();
                    return true;
                }
            }
            return false;
        }

        public int characteristics() {
            return (fence < 0 || est == map.size ? SIZED : 0) | Spliterator.DISTINCT;
        }
    }

}
