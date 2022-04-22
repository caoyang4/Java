高并发
![gaobingfa.png](gaobingfa.png)
![juc.png](juc.png)
https://www.pdai.tech/md/java/thread/java-thread-x-juc-overview.html
1. Lock框架和Tools类
   ![lock.png](lock.png)
   1. Collections: 并发集合
      ![collections.png](collections.png)
      ![hashmap.png](hashmap.png)
2. Atomic: 原子类
3. Executors: 线程池
   ![executors.png](executors.png)
   ![scheduledPool.png](scheduledPool.png)
   ![forkJoinPool.png](forkJoinPool.png)
4. Unsafe
   ![unsafe.png](unsafe.png)
   Unsafe本质上提供了3种CAS方法：compareAndSwapObject、compareAndSwapInt和compareAndSwapLong
5. threadLocal 内存泄漏示意图
   ![threadLocalOOM.png](threadLocalOOM.png)
6. aqs 获取锁流程
   ![AQSAcquireLock.png](AQSAcquireLock.png)