package src.juc.pratice.dag;

import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author caoyang
 * @create 2023-04-26 14:21
 */
@Data
public class DagTask {

    private String name;
    private List<DagTask> fathers;

    public DagTask(String name){
        this.name = name;
        fathers = new ArrayList<>();
    }
    public void addFather(DagTask... tasks){
        fathers.addAll(Arrays.asList(tasks));
    }
    public void doTask() {
        System.out.println("Task: " + name);
    }
}
