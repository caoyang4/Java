package src.algorithm.leetcode;

import java.util.Deque;
import java.util.LinkedList;

/**
 * 71. 简化路径
 * 给你一个字符串 path ，表示指向某一文件或目录的 Unix 风格 绝对路径 （以 '/' 开头），请你将其转化为更加简洁的规范路径
 * @author caoyang
 */
public class Leetcode71 {
    /**
     * 栈解法
     * @param path
     * @return
     */
    public static String simplifyPath(String path) {
        while (path.contains("/./") || path.contains("//")){
            path = path.replace("/./", "/").replace("//", "/");
        }

        Deque<String> deque = new LinkedList<>();
        for (String s : path.split("/")) {
            System.out.println(s);
            if(!"..".equals(s) && !"".equals(s) && !".".equals(s)){
                deque.add(s);
            }else {
                if(!deque.isEmpty() && "..".equals(s)){
                    deque.removeLast();
                }
            }
        }
        if(deque.isEmpty()){
            path = "/";
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            while (!deque.isEmpty()){
                stringBuilder.append("/").append(deque.pop());
            }
            path = stringBuilder.toString();
        }
        return path;
    }

    public static void main(String[] args) {
        String path = "/.";
        path = simplifyPath(path);
        System.out.println(path);
    }
}
