package src.container;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author caoyang
 */
public class TestList {
    // List非线程安全
    // List<Object> objects = new ArrayList<>();
    // synchronizedList 能保证 add remove 等安全
    // 对于其他线程通过 size() 获取长度，仍然不能保证获取正确的数据长度
    List<Object> objects = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) {
        TestList testList = new TestList();
        new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                System.out.println("add object" + i);
                testList.objects.add(new Object());
            }
        }).start();

        new Thread(() -> {
            while (true){
                System.out.println(testList.objects.size());
                if(testList.objects.size() == 5){
                    System.out.println("objects长度到达 5 了！");
                    break;
                }
            }
        }).start();
    }


}
