package src.guava;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * guava 测试
 * @author caoyang
 */
public class GuavaTest {
    public static void main(String[] args) {
        List<String> lists = Lists.newArrayList("a","b","g",null,"8","9");
        String result1 = Joiner.on(",").skipNulls().join(lists);
        System.out.println(result1);
        String result2 = Joiner.on("-").useForNull("null").join(lists);
        System.out.println(result2);
    }
}
