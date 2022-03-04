package src.juc.collections;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TestDelayQueue2 {

    public static void main(String[] args) {
        Random rand=new Random(47);
        DelayQueue<DelayedTask> queue=new DelayQueue<>();
        ExecutorService exec=Executors.newCachedThreadPool();
        for(int i = 0; i < 10; i++){
            queue.put(new DelayedTask(rand.nextInt(1000)));
        }
        queue.add(new DelayedTask.MyDelayedTask(2000, exec));
        exec.execute(new DelayedTaskConsumer(queue));
    }

}

class DelayedTask implements Delayed, Runnable{
    private static int counter=0;
    private final int id=counter++;
    private final int delta;
    private final long trigger;
    protected static List<DelayedTask> sequence=new ArrayList<>();

    public DelayedTask(int delayInMilliseconds){
        delta=delayInMilliseconds;
        trigger=System.nanoTime() + TimeUnit.NANOSECONDS.convert(delta, TimeUnit.MILLISECONDS);
        sequence.add(this);
    }
    @Override
    public int compareTo(Delayed o) {
        DelayedTask that=(DelayedTask)o;
        return Long.compare(this.trigger, that.trigger);
    }

    @Override
    public void run() {
        System.out.println(this+"  is running");
    }

    @Override
    public String toString(){
        return "Task:"+id+" delta:"+delta;
    }
    public String summary(){
        return "id:"+id+"  delta:"+delta;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(trigger-System.nanoTime(), TimeUnit.NANOSECONDS);
    }
    public static class MyDelayedTask extends DelayedTask{
        private ExecutorService exec;
        public MyDelayedTask(int delayInMilliseconds, ExecutorService exec) {
            super(delayInMilliseconds);
            this.exec=exec;
        }
        @Override
        public void run(){
            for(DelayedTask dt:sequence){
                System.out.println(dt.summary()+"  ");
            }
            System.out.println(this+" Calling ShutdownNow()");
            exec.shutdownNow();
        }
    }
}
class DelayedTaskConsumer implements Runnable{
    private DelayQueue<DelayedTask> dq;
    public DelayedTaskConsumer(DelayQueue<DelayedTask> q){
        dq=q;
    }
    @Override
    public void run(){
        try {
            while(!Thread.interrupted()){
                dq.take().run();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("finished delayed task consume!!!!!");
    }
}
