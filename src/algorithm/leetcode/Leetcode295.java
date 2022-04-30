package src.algorithm.leetcode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 295. 数据流的中位数
 * @author caoyang
 */
public class Leetcode295 {
    class MedianFinder {
        List<Integer> list;
        public MedianFinder() {
            list = new ArrayList<>();
        }

        public void addNum(int num) {
            list.add(num);
            if(list.size() > 1){list.sort((o1,o2) -> {return o1-o2;});}
        }

        public double findMedian() {
            int middle = list.size() >> 1;
            if(list.size() % 2 == 1){
                return list.get(middle);
            }
            double left = list.get(middle-1);
            double right = list.get(middle);
            return (left+right)/2;
        }
    }

    public static void main(String[] args) {

    }
}
