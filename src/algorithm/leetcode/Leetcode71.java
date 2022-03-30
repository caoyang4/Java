package src.algorithm.leetcode;

import java.util.Deque;
import java.util.LinkedList;

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
