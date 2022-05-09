package java.util.concurrent;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/**
 * ConcurrentLinkedDeque是一种基于链接节点的无限并发链表，非阻塞式双端队列。可以安全地并发执行插入、删除和访问操作。
 * 当许多线程同时访问一个公共集合时，ConcurrentLinkedDeque是一个合适的选择。
 * 和大多数其他并发的集合类型一样，这个类不允许使用空元素
 * 因为链表的异步性质，确定当前元素的数量需要遍历所有的元素，所以如果在遍历期间有其他线程修改了这个集合，size方法就可能会报告不准确的结果。
 * 同时，对链表的批量操作也不是一个原子操作
 */

/**
 * 以pollFirst出队操作为例进行一个总结说明：
 *  1.通过first()获取到队列头部的第一个结点
 *  2.如果为活动结点（item非空），则将活动结点item置空，即执行logical deletion（逻辑删除）操作
 *  3.继续执行unlinking阶段
 *  4.继续执行gc-unlinking阶段
 *  5.在unlinking阶段根据结点位置进行不同情况的处理：
 *
 *  1.如果出队的结点是队列的第一个结点p，则执行unlinkFirst，其过程如下：
 *    找到p之后的第一个有效结点，直到最后一个结点为止，p的后继结点指向这个找到的结点
 *    skipDeletedPredecessors完成unlinking阶段，使队列的活动结点无法访问被删除的结点
 *    进行gc-unlinking阶段，通过updateHead、updateTail使被删除的结点无法从head/tail可达，
 *    最后让被删除结点后继指向自己，前驱指向终结结点
 *
 * 2.如果出队的结点是队列的最后一个结点p，则执行unlinkLast，其过程与第1种情况类似，只是方向不同
 *
 * 3.如果出队的结点时队列的中间位置，则执行unlink中的一个分支代码：
 *   先找到删除结点x的有效前驱和有效后继，统计中间已经处于逻辑删除的结点个数
 *   如果统计个数已经超过阈值个数或者是内部结点删除，有效前驱和后继互连，即活动结点不能访问逻辑删除结点了（unlinking阶段）
 *   有效前驱和后继是队头或队尾，尝试进行gc-unlink，通过updateHead、updateTail使被删除的结点无法从head/tail可达，最后让被删除结点指向自己或者执行终结结点
 */
public class ConcurrentLinkedDeque<E> extends AbstractCollection<E> implements Deque<E>, java.io.Serializable {

    private static final long serialVersionUID = 876323262645176354L;

    // 头节点
    private transient volatile Node<E> head;
    // 尾结点
    private transient volatile Node<E> tail;
    // 终止结点，在gc-unlinking阶段将无用结点链接到这两个结点上，自行处理减少内内存滞留风险
    private static final Node<Object> PREV_TERMINATOR, NEXT_TERMINATOR;

    @SuppressWarnings("unchecked")
    Node<E> prevTerminator() {
        return (Node<E>) PREV_TERMINATOR;
    }

    @SuppressWarnings("unchecked")
    Node<E> nextTerminator() {
        return (Node<E>) NEXT_TERMINATOR;
    }

    /**
     * 双向链表
     */
    static final class Node<E> {
        // 前驱节点
        volatile Node<E> prev;
        volatile E item;
        // 后继节点
        volatile Node<E> next;

        Node() {  // default constructor for NEXT_TERMINATOR, PREV_TERMINATOR
        }

        Node(E item) {
            UNSAFE.putObject(this, itemOffset, item);
        }

        boolean casItem(E cmp, E val) {
            return UNSAFE.compareAndSwapObject(this, itemOffset, cmp, val);
        }

        void lazySetNext(Node<E> val) {
            UNSAFE.putOrderedObject(this, nextOffset, val);
        }

        boolean casNext(Node<E> cmp, Node<E> val) {
            return UNSAFE.compareAndSwapObject(this, nextOffset, cmp, val);
        }

        void lazySetPrev(Node<E> val) {
            UNSAFE.putOrderedObject(this, prevOffset, val);
        }

        boolean casPrev(Node<E> cmp, Node<E> val) {
            return UNSAFE.compareAndSwapObject(this, prevOffset, cmp, val);
        }

        // Unsafe mechanics

        private static final sun.misc.Unsafe UNSAFE;
        private static final long prevOffset;
        private static final long itemOffset;
        private static final long nextOffset;

        static {
            try {
                UNSAFE = sun.misc.Unsafe.getUnsafe();
                Class<?> k = Node.class;
                prevOffset = UNSAFE.objectFieldOffset(k.getDeclaredField("prev"));
                itemOffset = UNSAFE.objectFieldOffset(k.getDeclaredField("item"));
                nextOffset = UNSAFE.objectFieldOffset(k.getDeclaredField("next"));
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }

    /**
     * Links e as first element.
     */
    private void linkFirst(E e) {
        checkNotNull(e);
        // 新建一个结点，并且要求结点不为空
        final Node<E> newNode = new Node<E>(e);

        restartFromHead:
        for (;;)
            //从头结点向前开始遍历链表
            for (Node<E> h = head, p = h, q;;) {
                // 前驱节点不为null，前驱的前驱节点不为null
                if ((q = p.prev) != null &&
                    (q = (p = q).prev) != null)
                    // head被更新（已经超过了松弛度阈值）则p更新为head
                    // 未更新则直接更新为前驱的前驱结点
                    p = (h != (h = head)) ? h : q;
                // p已经出队，没办法从p再继续判断了，无法到达其他结点，需要重新开始循环
                else if (p.next == p) // PREV_TERMINATOR
                    continue restartFromHead;
                else {
                    // p为第一个结点，更新新结点next指向p
                    newNode.lazySetNext(p); // CAS piggyback
                    // cas更新p的前驱指向新结点，更新失败则重新循环更新
                    if (p.casPrev(null, newNode)) {
                        // Successful CAS is the linearization point
                        // for e to become an element of this deque,
                        // and for newNode to become "live".
                        // 新结点入队成功，头结点已经更新了(此时的新结点距离h已经 >= 2个结点距离)，尝试更新head
                        if (p != h) // hop two nodes at a time
                            casHead(h, newNode);  // Failure is OK.
                        return;
                    }
                    // Lost CAS race to another thread; re-read prev
                }
            }
    }

    /**
     * 添加进尾结点
     */
    private void linkLast(E e) {
        checkNotNull(e);
        // 新建一个结点，并且要求结点不为空
        final Node<E> newNode = new Node<E>(e);

        restartFromTail:
        for (;;)
            // 从尾结点向后遍历链表
            for (Node<E> t = tail, p = t, q;;) {
                // 后继节点不为null，后继的后继节点不为null
                if ((q = p.next) != null &&
                    (q = (p = q).next) != null)
                    // Check for tail updates every other hop.
                    // If p == q, we are sure to follow tail instead.
                    // tail被更新（已经超过了松弛度阈值）则p更新为tail
                    // 未更新则直接更新为后继的后继结点
                    p = (t != (t = tail)) ? t : q;
                else if (p.prev == p) // NEXT_TERMINATOR
                    continue restartFromTail;
                else {
                    // p为最后一个结点，更新新结点prev指向p
                    newNode.lazySetPrev(p); // CAS piggyback
                    // cas更新p的后继指向新结点，更新失败则重新循环更新
                    if (p.casNext(null, newNode)) {
                        // Successful CAS is the linearization point
                        // for e to become an element of this deque,
                        // and for newNode to become "live".
                        if (p != t) // hop two nodes at a time
                            casTail(t, newNode);  // Failure is OK.
                        return;
                    }
                    // Lost CAS race to another thread; re-read next
                }
            }
    }
    // 删除结点执行unlinking/gc-unlinking的阈值，当逻辑删除结点达到阈值才触发，算是性能优化
    private static final int HOPS = 2;

    /**
     * 被pollFirst，pollLast，removeFirstOccurrence，removeLastOccurrence
     * 和迭代器的remove所使用，移除非空结点，出队操作和删除时使用，
     * 主要处理处于队列中间的结点
     */
    void unlink(Node<E> x) {
        final Node<E> prev = x.prev;
        final Node<E> next = x.next;
        // 前驱为null表示x为头结点
        if (prev == null) {
            unlinkFirst(x, next);
        // 后继为null表示x为尾结点
        } else if (next == null) {
            unlinkLast(x, prev);
        // 中间节点
        } else {
            Node<E> activePred, activeSucc;
            boolean isFirst, isLast;
            // 记录逻辑删除结点数
            int hops = 1;

            // 找到有效的前驱结点
            for (Node<E> p = prev; ; ++hops) {
                // 有效前驱结点设置
                if (p.item != null) {
                    activePred = p;
                    isFirst = false;
                    // item为空跳出循环
                    break;
                }
                Node<E> q = p.prev;
                // p是第一个结点
                if (q == null) {
                    // p已经出队，直接返回
                    if (p.next == p)
                        return;
                    // p的item为null，next还未更新，变量设置
                    activePred = p;
                    isFirst = true;
                    break;
                }
                // p == p.prev表示p已经出队
                else if (p == q)
                    return;
                // 继续循环向前查找
                else
                    p = q;
            }

            // 找到有效的后继结点
            for (Node<E> p = next; ; ++hops) {
                // 有效后继结点设置
                if (p.item != null) {
                    activeSucc = p;
                    isLast = false;
                    break;
                }
                Node<E> q = p.next;
                // p是最后一个结点
                if (q == null) {
                    // p已经出队
                    if (p.prev == p)
                        return;
                    // p的item为null，prev还未更新，变量设置
                    activeSucc = p;
                    isLast = true;
                    break;
                }
                // p == p.next表示p已经出队
                else if (p == q)
                    return;
                else
                    // 继续循环向后查找
                    p = q;
            }

            // 达到逻辑删除结点阈值或者是内部删除结点则需要进行额外处理unlink/gc-unlink
            if (hops < HOPS
                // always squeeze out interior deleted nodes
                && (isFirst | isLast))
                return;

            // 移除有效前驱和后继结点之间的有效结点，包括x，使得前驱和后继互连
            skipDeletedSuccessors(activePred);
            skipDeletedPredecessors(activeSucc);

            // Try to gc-unlink, if possible
            // 有效前驱和后继是队头或队尾，尝试gc-unlink
            if ((isFirst | isLast) &&

                // 检查前驱后继状态，确保未改变
                (activePred.next == activeSucc) &&
                (activeSucc.prev == activePred) &&
                (isFirst ? activePred.prev == null : activePred.item != null) &&
                (isLast  ? activeSucc.next == null : activeSucc.item != null)) {
                // 更新head和tail 确保x不可达
                updateHead(); // Ensure x is not reachable from head
                updateTail(); // Ensure x is not reachable from tail

                // Finally, actually gc-unlink
                // 最后更新x，使得从x到活动节点不可达
                x.lazySetPrev(isFirst ? prevTerminator() : x);
                x.lazySetNext(isLast  ? nextTerminator() : x);
            }
        }
    }

    /**
     * 从队列头将第一个非空结点出队
     */
    private void unlinkFirst(Node<E> first, Node<E> next) {
        for (Node<E> o = null, p = next, q;;) {
            // p为活动节点或p为最后一个节点
            if (p.item != null || (q = p.next) == null) {
                // 如果第一次循环就执行到此则不需要进行操作直接返回，p本来就是first的后继
                // p的前驱不能指向自己，first的后继更新成p
                if (o != null && p.prev != p && first.casNext(next, p)) {
                    // unlink阶段
                    skipDeletedPredecessors(p);
                    // 检查first和p，确保没被更新修改才进行gc-unlink操作
                    if (first.prev == null &&
                        (p.next == null || p.item != null) &&
                        p.prev == first) {

                        updateHead(); // Ensure o is not reachable from head
                        updateTail(); // Ensure o is not reachable from tail

                        // Finally, actually gc-unlink
                        o.lazySetNext(o);
                        o.lazySetPrev(prevTerminator());
                    }
                }
                return;
            }
            else if (p == q)
                return;
            else {
                o = p;
                p = q;
            }
        }
    }

    /**
     * 从队列尾将第一个非空结点出队
     */
    private void unlinkLast(Node<E> last, Node<E> prev) {
        // assert last != null;
        // assert prev != null;
        // assert last.item == null;
        for (Node<E> o = null, p = prev, q;;) {
            if (p.item != null || (q = p.prev) == null) {
                if (o != null && p.next != p && last.casPrev(prev, p)) {
                    skipDeletedSuccessors(p);
                    if (last.next == null &&
                        (p.prev == null || p.item != null) &&
                        p.next == last) {

                        updateHead(); // Ensure o is not reachable from head
                        updateTail(); // Ensure o is not reachable from tail

                        // Finally, actually gc-unlink
                        o.lazySetPrev(o);
                        o.lazySetNext(nextTerminator());
                    }
                }
                return;
            }
            else if (p == q)
                return;
            else {
                o = p;
                p = q;
            }
        }
    }

    /**
     * 更新head结点，确保在调用此方法之前unlinked的任何结点在该方法返回之后都不能从head访问，
     * 不保证消除松弛度，仅仅是head将指向处于活动状态的结点
     */
    private final void updateHead() {
        // head要么指向一个活动结点要么尝试指向第一个结点直到成功
        Node<E> h, p, q;
        restartFromHead:
        while ((h = head).item == null && (p = h.prev) != null) {
            for (;;) {
                // head前驱的前驱为空或head前驱的前驱的前驱为空
                // 即head前有1个或2个结点
                if ((q = p.prev) == null ||
                    (q = (p = q).prev) == null) {
                    // It is possible that p is PREV_TERMINATOR,
                    // but if so, the CAS is guaranteed to fail.
                    // 将head更新指向为第一个结点
                    if (casHead(h, p))
                        return;
                    else
                        // 未成功更新说明已经被其他线程更新了，重新循环判断
                        continue restartFromHead;
                }
                // h前有超过2个的结点，表明当前h指向的结点已经与第一个结点距离超过2，同时h已经不指向head了，重新循环
                else if (h != head)
                    continue restartFromHead;
                else
                    // h前有超过2个的结点，同时h还指向head，则更新p为q再次判断，相当于p向前跳了1或2个结点位置
                    p = q;
            }
        }
    }

    private final void updateTail() {
        // Either tail already points to an active node, or we keep
        // trying to cas it to the last node until it does.
        Node<E> t, p, q;
        restartFromTail:
        while ((t = tail).item == null && (p = t.next) != null) {
            for (;;) {
                if ((q = p.next) == null ||
                    (q = (p = q).next) == null) {
                    // It is possible that p is NEXT_TERMINATOR,
                    // but if so, the CAS is guaranteed to fail.
                    if (casTail(t, p))
                        return;
                    else
                        continue restartFromTail;
                }
                else if (t != tail)
                    continue restartFromTail;
                else
                    p = q;
            }
        }
    }

    private void skipDeletedPredecessors(Node<E> x) {
        whileActive:
        do {
            Node<E> prev = x.prev;
            Node<E> p = prev;
            findActive:
            for (;;) {
                if (p.item != null)
                    break findActive;
                Node<E> q = p.prev;
                if (q == null) {
                    if (p.next == p)
                        continue whileActive;
                    break findActive;
                }
                else if (p == q)
                    continue whileActive;
                else
                    p = q;
            }

            // found active CAS target
            if (prev == p || x.casPrev(prev, p))
                return;

        } while (x.item != null || x.next == null);
    }

    /**
     * 将刚刚找到的后继结点的前驱指向结点p，即完成它们的互联，这一步就是所谓的unlinking，使队列的活动结点无法访问被删除的结点。
     */
    private void skipDeletedSuccessors(Node<E> x) {
        whileActive:
        do {
            Node<E> next = x.next;
            Node<E> p = next;
            findActive:
            for (;;) {
                // p的item非空，说明p为活动结点，退出循环进行关联更新操作
                if (p.item != null)
                    break findActive;
                // p的item为空，再继续向前查找其前驱
                Node<E> q = p.next;
                // p的前驱结点为空
                if (q == null) {
                    // 若p结点处于gc-unlinking状态，即通过p已经无法到达其他活动结点，则需重头开始继续循环判断
                    if (p.prev == p)
                        continue whileActive;
                    // 表明找到了有效结点，退出循环
                    break findActive;
                }
                // p的前驱结点非空，p.prev == p
                // p已经此刻的p结点处于gc-unlinking状态，即通过p已经无法到达其他有效结点
                else if (p == q)
                    // 无法再向前遍历，只能重头开始循环判断
                    continue whileActive;
                else
                    // 到此表示p的item为空，p的前驱非空且不处于gc-unlinking状态
                    // 循环向前继续判断前驱结点
                    p = q;
            }

            // 找到活动或有效的前驱节点，前驱CAS更新成功返回否则继续循环判断更新
            if (next == p || x.casNext(next, p))
                return;

        } while (x.item != null || x.prev == null);
    }

    /**
     * 找到结点的后继，假如当前结点已经无效结点时，则返回第一个结点
     */
    final Node<E> succ(Node<E> p) {
        Node<E> q = p.next;
        return (p == q) ? first() : q;
    }

    /**
     * 找到结点的前驱，假如当前结点已经无效结点时，则返回第一个结点
     */
    final Node<E> pred(Node<E> p) {
        Node<E> q = p.prev;
        return (p == q) ? last() : q;
    }
    // 返回第一个结点，有可能是逻辑删除结点
    Node<E> first() {
        restartFromHead:
        for (;;)
            for (Node<E> h = head, p = h, q;;) {
                // p的前驱和前驱的前驱都非空
                // 表示p结点之前有2个以上的活动结点
                if ((q = p.prev) != null &&
                    (q = (p = q).prev) != null)
                    // 可能head已经被更新了则判断下更新h同时更新p
                    // 或者head还未更新则直接将p指向q
                    p = (h != (h = head)) ? h : q;
                // p的前驱为空或者前驱的前驱为空
                // p == h 表明p的前驱为空（第一个条件里判断），p就是第一个结点
                // p == h 不满足则p的前驱非空，前驱的前驱为空，则p的前驱为第一个结点，此时尝试更新head并返回第一个结点
                else if (p == h
                         // It is possible that p is PREV_TERMINATOR,
                         // but if so, the CAS is guaranteed to fail.
                         || casHead(h, p))
                    return p;
                // 第二个条件中尝试更新head失败，则说明其他线程更新了head，重新开始循环处理
                else
                    continue restartFromHead;
            }
    }

    Node<E> last() {
        restartFromTail:
        for (;;)
            for (Node<E> t = tail, p = t, q;;) {
                if ((q = p.next) != null &&
                    (q = (p = q).next) != null)
                    // Check for tail updates every other hop.
                    // If p == q, we are sure to follow tail instead.
                    p = (t != (t = tail)) ? t : q;
                else if (p == t
                         // It is possible that p is NEXT_TERMINATOR,
                         // but if so, the CAS is guaranteed to fail.
                         || casTail(t, p))
                    return p;
                else
                    continue restartFromTail;
            }
    }

    // Minor convenience utilities

    private static void checkNotNull(Object v) {
        if (v == null)
            throw new NullPointerException();
    }

    private E screenNullResult(E v) {
        if (v == null)
            throw new NoSuchElementException();
        return v;
    }

    private ArrayList<E> toArrayList() {
        ArrayList<E> list = new ArrayList<E>();
        for (Node<E> p = first(); p != null; p = succ(p)) {
            E item = p.item;
            if (item != null)
                list.add(item);
        }
        return list;
    }

    // 无参构造方法创建了空结点同时头尾结点指向这个空结点
    public ConcurrentLinkedDeque() {
        head = tail = new Node<E>(null);
    }
    // 集合参数构造时先将所有集合结点构成链表
    public ConcurrentLinkedDeque(Collection<? extends E> c) {
        // Copy c into a private chain of Nodes
        Node<E> h = null, t = null;
        for (E e : c) {
            checkNotNull(e);
            Node<E> newNode = new Node<E>(e);
            if (h == null)
                h = t = newNode;
            else {
                // cas 更新prev，next
                t.lazySetNext(newNode);
                newNode.lazySetPrev(t);
                t = newNode;
            }
        }
        initHeadTail(h, t);
    }
    // 初始化头尾
    private void initHeadTail(Node<E> h, Node<E> t) {
        if (h == t) {
            if (h == null)
                h = t = new Node<E>(null);
            else {
                // Avoid edge case of a single Node with non-null item.
                Node<E> newNode = new Node<E>(null);
                t.lazySetNext(newNode);
                newNode.lazySetPrev(t);
                t = newNode;
            }
        }
        head = h;
        tail = t;
    }
    // 将元素e添加到队列头部，即从头部入队操作
    public void addFirst(E e) {
        linkFirst(e);
    }
    public boolean offerFirst(E e) {
        linkFirst(e);
        return true;
    }
    // 将元素e添加到队列尾部，即从尾部入队操作
    public void addLast(E e) {
        linkLast(e);
    }
    public boolean offerLast(E e) {
        linkLast(e);
        return true;
    }

    public E peekFirst() {
        for (Node<E> p = first(); p != null; p = succ(p)) {
            E item = p.item;
            if (item != null)
                return item;
        }
        return null;
    }

    public E peekLast() {
        for (Node<E> p = last(); p != null; p = pred(p)) {
            E item = p.item;
            if (item != null)
                return item;
        }
        return null;
    }

    public E getFirst() {
        return screenNullResult(peekFirst());
    }

    public E getLast() {
        return screenNullResult(peekLast());
    }

    public E pollFirst() {
        for (Node<E> p = first(); p != null; p = succ(p)) {
            E item = p.item;
            if (item != null && p.casItem(item, null)) {
                unlink(p);
                return item;
            }
        }
        return null;
    }

    public E pollLast() {
        for (Node<E> p = last(); p != null; p = pred(p)) {
            E item = p.item;
            if (item != null && p.casItem(item, null)) {
                unlink(p);
                return item;
            }
        }
        return null;
    }

    public E removeFirst() {
        return screenNullResult(pollFirst());
    }

    public E removeLast() {
        return screenNullResult(pollLast());
    }

    // *** Queue and stack methods ***

    public boolean offer(E e) {
        return offerLast(e);
    }

    public boolean add(E e) {
        return offerLast(e);
    }

    public E poll()           { return pollFirst(); }
    public E peek()           { return peekFirst(); }

    public E remove()         { return removeFirst(); }

    public E pop()            { return removeFirst(); }

    public E element()        { return getFirst(); }

    public void push(E e)     { addFirst(e); }

    public boolean removeFirstOccurrence(Object o) {
        checkNotNull(o);
        for (Node<E> p = first(); p != null; p = succ(p)) {
            E item = p.item;
            if (item != null && o.equals(item) && p.casItem(item, null)) {
                unlink(p);
                return true;
            }
        }
        return false;
    }

    public boolean removeLastOccurrence(Object o) {
        checkNotNull(o);
        for (Node<E> p = last(); p != null; p = pred(p)) {
            E item = p.item;
            if (item != null && o.equals(item) && p.casItem(item, null)) {
                unlink(p);
                return true;
            }
        }
        return false;
    }

    public boolean contains(Object o) {
        if (o == null) return false;
        for (Node<E> p = first(); p != null; p = succ(p)) {
            E item = p.item;
            if (item != null && o.equals(item))
                return true;
        }
        return false;
    }

    public boolean isEmpty() {
        return peekFirst() == null;
    }

    public int size() {
        int count = 0;
        for (Node<E> p = first(); p != null; p = succ(p))
            if (p.item != null)
                // Collection.size() spec says to max out
                if (++count == Integer.MAX_VALUE)
                    break;
        return count;
    }

    public boolean remove(Object o) {
        return removeFirstOccurrence(o);
    }

    public boolean addAll(Collection<? extends E> c) {
        if (c == this)
            // As historically specified in AbstractQueue#addAll
            throw new IllegalArgumentException();

        // Copy c into a private chain of Nodes
        Node<E> beginningOfTheEnd = null, last = null;
        for (E e : c) {
            checkNotNull(e);
            Node<E> newNode = new Node<E>(e);
            if (beginningOfTheEnd == null)
                beginningOfTheEnd = last = newNode;
            else {
                last.lazySetNext(newNode);
                newNode.lazySetPrev(last);
                last = newNode;
            }
        }
        if (beginningOfTheEnd == null)
            return false;

        // Atomically append the chain at the tail of this collection
        restartFromTail:
        for (;;)
            for (Node<E> t = tail, p = t, q;;) {
                if ((q = p.next) != null &&
                    (q = (p = q).next) != null)
                    // Check for tail updates every other hop.
                    // If p == q, we are sure to follow tail instead.
                    p = (t != (t = tail)) ? t : q;
                else if (p.prev == p) // NEXT_TERMINATOR
                    continue restartFromTail;
                else {
                    // p is last node
                    beginningOfTheEnd.lazySetPrev(p); // CAS piggyback
                    if (p.casNext(null, beginningOfTheEnd)) {
                        // Successful CAS is the linearization point
                        // for all elements to be added to this deque.
                        if (!casTail(t, last)) {
                            // Try a little harder to update tail,
                            // since we may be adding many elements.
                            t = tail;
                            if (last.next == null)
                                casTail(t, last);
                        }
                        return true;
                    }
                    // Lost CAS race to another thread; re-read next
                }
            }
    }

    public void clear() {
        while (pollFirst() != null)
            ;
    }

    public Object[] toArray() {
        return toArrayList().toArray();
    }

    public <T> T[] toArray(T[] a) {
        return toArrayList().toArray(a);
    }

    /**
     * 由于其双向链表的实现，迭代器可分为升序迭代器（Itr）和倒序迭代器（DescendingItr），
     * 通过AbstractItr封装公共操作方法，Itr和DescendingItr分别实现对应不同的方法，
     * 一个从头节点开始向后进行遍历，一个从尾节点向后进行遍历
     */
    public Iterator<E> iterator() {
        return new Itr();
    }

    public Iterator<E> descendingIterator() {
        return new DescendingItr();
    }

    private abstract class AbstractItr implements Iterator<E> {
        private Node<E> nextNode;

        private E nextItem;

        private Node<E> lastRet;

        abstract Node<E> startNode();
        abstract Node<E> nextNode(Node<E> p);

        AbstractItr() {
            advance();
        }

        private void advance() {
            lastRet = nextNode;

            Node<E> p = (nextNode == null) ? startNode() : nextNode(nextNode);
            for (;; p = nextNode(p)) {
                if (p == null) {
                    // p might be active end or TERMINATOR node; both are OK
                    nextNode = null;
                    nextItem = null;
                    break;
                }
                E item = p.item;
                if (item != null) {
                    nextNode = p;
                    nextItem = item;
                    break;
                }
            }
        }

        public boolean hasNext() {
            return nextItem != null;
        }

        public E next() {
            E item = nextItem;
            if (item == null) throw new NoSuchElementException();
            advance();
            return item;
        }

        public void remove() {
            Node<E> l = lastRet;
            if (l == null) throw new IllegalStateException();
            l.item = null;
            unlink(l);
            lastRet = null;
        }
    }

    private class Itr extends AbstractItr {
        Node<E> startNode() { return first(); }
        Node<E> nextNode(Node<E> p) { return succ(p); }
    }

    private class DescendingItr extends AbstractItr {
        Node<E> startNode() { return last(); }
        Node<E> nextNode(Node<E> p) { return pred(p); }
    }

    static final class CLDSpliterator<E> implements Spliterator<E> {
        static final int MAX_BATCH = 1 << 25;  // max batch array size;
        final ConcurrentLinkedDeque<E> queue;
        Node<E> current;    // current node; null until initialized
        int batch;          // batch size for splits
        boolean exhausted;  // true when no more nodes
        CLDSpliterator(ConcurrentLinkedDeque<E> queue) {
            this.queue = queue;
        }

        public Spliterator<E> trySplit() {
            Node<E> p;
            final ConcurrentLinkedDeque<E> q = this.queue;
            int b = batch;
            int n = (b <= 0) ? 1 : (b >= MAX_BATCH) ? MAX_BATCH : b + 1;
            if (!exhausted &&
                ((p = current) != null || (p = q.first()) != null)) {
                if (p.item == null && p == (p = p.next))
                    current = p = q.first();
                if (p != null && p.next != null) {
                    Object[] a = new Object[n];
                    int i = 0;
                    do {
                        if ((a[i] = p.item) != null)
                            ++i;
                        if (p == (p = p.next))
                            p = q.first();
                    } while (p != null && i < n);
                    if ((current = p) == null)
                        exhausted = true;
                    if (i > 0) {
                        batch = i;
                        return Spliterators.spliterator
                            (a, 0, i, Spliterator.ORDERED | Spliterator.NONNULL |
                             Spliterator.CONCURRENT);
                    }
                }
            }
            return null;
        }

        public void forEachRemaining(Consumer<? super E> action) {
            Node<E> p;
            if (action == null) throw new NullPointerException();
            final ConcurrentLinkedDeque<E> q = this.queue;
            if (!exhausted &&
                ((p = current) != null || (p = q.first()) != null)) {
                exhausted = true;
                do {
                    E e = p.item;
                    if (p == (p = p.next))
                        p = q.first();
                    if (e != null)
                        action.accept(e);
                } while (p != null);
            }
        }

        public boolean tryAdvance(Consumer<? super E> action) {
            Node<E> p;
            if (action == null) throw new NullPointerException();
            final ConcurrentLinkedDeque<E> q = this.queue;
            if (!exhausted &&
                ((p = current) != null || (p = q.first()) != null)) {
                E e;
                do {
                    e = p.item;
                    if (p == (p = p.next))
                        p = q.first();
                } while (e == null && p != null);
                if ((current = p) == null)
                    exhausted = true;
                if (e != null) {
                    action.accept(e);
                    return true;
                }
            }
            return false;
        }

        public long estimateSize() { return Long.MAX_VALUE; }

        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.NONNULL |
                Spliterator.CONCURRENT;
        }
    }

    public Spliterator<E> spliterator() {
        return new CLDSpliterator<E>(this);
    }

    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {

        // Write out any hidden stuff
        s.defaultWriteObject();

        // Write out all elements in the proper order.
        for (Node<E> p = first(); p != null; p = succ(p)) {
            E item = p.item;
            if (item != null)
                s.writeObject(item);
        }

        // Use trailing null as sentinel
        s.writeObject(null);
    }

    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();

        // Read in elements until trailing null sentinel found
        Node<E> h = null, t = null;
        Object item;
        while ((item = s.readObject()) != null) {
            @SuppressWarnings("unchecked")
            Node<E> newNode = new Node<E>((E) item);
            if (h == null)
                h = t = newNode;
            else {
                t.lazySetNext(newNode);
                newNode.lazySetPrev(t);
                t = newNode;
            }
        }
        initHeadTail(h, t);
    }

    private boolean casHead(Node<E> cmp, Node<E> val) {
        return UNSAFE.compareAndSwapObject(this, headOffset, cmp, val);
    }

    private boolean casTail(Node<E> cmp, Node<E> val) {
        return UNSAFE.compareAndSwapObject(this, tailOffset, cmp, val);
    }

    // Unsafe mechanics

    private static final sun.misc.Unsafe UNSAFE;
    private static final long headOffset;
    private static final long tailOffset;
    static {
        PREV_TERMINATOR = new Node<Object>();
        PREV_TERMINATOR.next = PREV_TERMINATOR;
        NEXT_TERMINATOR = new Node<Object>();
        NEXT_TERMINATOR.prev = NEXT_TERMINATOR;
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> k = ConcurrentLinkedDeque.class;
            headOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("head"));
            tailOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("tail"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
