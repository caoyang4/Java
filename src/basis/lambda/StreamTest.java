package src.basis.lambda;

import java.util.ArrayList;
import java.util.List;

/**
 * 流式处理
 * @author caoyang
 */
public class StreamTest {
    public static void main(String[] args) {
        /*
         * 1. 要求获取字符串长度大于3的内容
         * 2. 跳过前3个数据
         * 3. 最后保存数据中带有"鸡丁"
         * 4. 打印
        * */
        List<String> list = new ArrayList<>();
        list.add("手撕大骨头");
        list.add("小鸡炖蘑菇");
        list.add("铁锅炖大鹅");
        list.add("红烧肉");
        list.add("红烧排骨");
        list.add("糖醋里脊");
        list.add("宫保鸡丁");
        list.add("辣子鸡丁");
        list.add("清蒸奶猫腿");
        list.add("西湖牛肉羹");
        list.add("酒酿小圆子");

        list.stream().filter(s -> s.length() > 3)
                .skip(3)
                .filter(s -> s.contains("鸡丁"))
                .forEach(System.out::println);

    }
}
