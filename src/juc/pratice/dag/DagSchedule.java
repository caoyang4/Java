package src.juc.pratice.dag;

import java.util.*;
import java.util.concurrent.*;

/**
 * @author caoyang
 * @create 2023-04-26 14:28
 */
public class DagSchedule {

    private final Set<DagTask> tasks;
    private final Set<DagTask> finishedTask;
    private final ExecutorService pool;

    public DagSchedule(Set<DagTask> tasks){
        this.tasks = tasks;
        this.finishedTask = new HashSet<>();
        this.pool = Executors.newFixedThreadPool(2);
    }


    public Set<DagTask> getIndependentTasks(Set<DagTask> tasks){
        // 获取可以独立执行的任务
        Set<DagTask> independentTasks = new HashSet<>();
        for (DagTask task : tasks) {
            // 任务未完成 && 不存在父任务，或父类任务都完成
            if (!finishedTask.contains(task) && finishedTask.containsAll(task.getFathers())) {
                independentTasks.add(task);
            }
        }
        return independentTasks;
    }

    public void runTask(DagTask task){
        task.doTask();
        finishedTask.add(task);
    }

    public void execute(){
        while (finishedTask.size() < tasks.size()){
            Set<DagTask> independentTasks = getIndependentTasks(tasks);
            List<Future<?>> futures = new ArrayList<>();
            for (DagTask task : independentTasks) {
                futures.add(pool.submit(() -> runTask(task)));
            }
            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException ignored) {
                }
            }
        }
        pool.shutdown();
    }

    public static void main(String[] args) {
        /*
         设置有向无环图的结构
                         6                8       10
                      /    \            /
                    4       5         9
                  /   \    / \
                 2      3    7
                  \    /
                    1
         */
        DagTask task1 = new DagTask("Task1");
        DagTask task2 = new DagTask("Task2");
        DagTask task3 = new DagTask("Task3");
        DagTask task4 = new DagTask("Task4");
        DagTask task5 = new DagTask("Task5");
        DagTask task6 = new DagTask("Task6");
        DagTask task7 = new DagTask("Task7");
        DagTask task8 = new DagTask("Task8");
        DagTask task9 = new DagTask("Task9");
        DagTask task10 = new DagTask("Task10");
        task1.addFather(task2, task3);
        task2.addFather(task4);
        task3.addFather(task4, task5);
        task7.addFather(task5);
        task4.addFather(task6);
        task5.addFather(task6);
        task5.addFather(task6);

        task9.addFather(task8);
        Set<DagTask> tasks = new HashSet<>(
                Arrays.asList(
                        task1, task2, task3, task4, task5,
                        task6, task7, task8, task9, task10
                ));
        DagSchedule dag = new DagSchedule(tasks);
        dag.execute();
    }

}
