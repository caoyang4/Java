ArrayBlockingQueue--数组实现的有界队列
会自动阻塞，根据调用api不同，有不同特性，当队列容量不足时，有阻塞能力。
boolean add(E e)：在容量不足时，抛出异常。
void put(E e)：在容量不足时，阻塞等待。
boolean offer(E e)：不阻塞，容量不足时返回false，当前新增数据操作放弃。
boolean offer(E e, long timeout, TimeUnit unit）：容量不足时，阻塞times时长（单位为timeunit），如果在阻塞时长内，有容量空闲，新增数据返回true。如果阻塞时长范围内，无容量空闲，放弃新增数据，返回false。

LinkedBlockingQueue--链式队列，队列容量不足或为0时自动阻塞
void put(E e)：自动阻塞，队列容量满后，自动阻塞。
E take()：自动阻塞，队列容量为0后，自动阻塞。

ConcurrentLinkedQueue--基础链表同步队列
boolean offer(E e)：入队。
E peek()：查看queue中的首数据。
E poll()：取出queue中的首数据。

DelayQueue--延时队列
根据比较机制，实现自定义处理顺序的队列。常用于定时任务，如：定时关机。
int compareTo(Delayed o)：比较大小，自动升序。
比较方法建议和getDelay方法配合完成。如果在DelayQueue是需要按时完成的计划任务，必须配合getDelay方法完成。
long getDelay(TimeUnit unit)：获取计划时长的方法，根据参数TimeUnit来决定，如何返回结果值。

LinkedTransferQueue--转移队列
boolean add(E e)：队列会保存数据，不做阻塞等待。
void transfer(E e)：是TransferQueue的特有方法。必须有消费者（take()方法调用者）。如果没有任意线程消费数据，transfer方法阻塞。一般用于处理即使消息。

SynchronousQueue--同步队列，容量为0
是特殊的TransferQueue，必须先有消费线程等待，才能使用的队列。
boolean add(E e)：父类方法，无阻塞，若没有消费线程阻塞等待数据，则抛出异常。
put(E e)：有阻塞，若没有消费线程阻塞等待数据，则阻塞。
