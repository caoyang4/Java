package src.jvm;

import java.util.HashSet;
import java.util.Set;

/**
 * 内存泄漏
 * 哈希改变
 * @author caoyang
 * @create 2022-05-31 23:00
 */
public class MemoryLeak2 {
    public static void main(String[] args) {
        Set<Point> set = new HashSet<>();
        Point p = new Point();
        // hashcode: 5
        p.setX(2);
        set.add(p);
        // size: 1
        System.out.println("add p, set size: " + set.size());
        // hashcode: 7
        p.setX(5);
        // remove:false, 改变了哈希值，导致内存泄漏
        System.out.println("remove p: " + set.remove(p));
        set.add(p);
        // size: 2
        System.out.println("add p, set size: " + set.size());
    }
}
class Point{
    int x;

    public int getX() {return x;}

    public void setX(int x) {this.x = x;}

    @Override
    public int hashCode() {
        return x + 3;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || obj.getClass() != getClass()) return false;
        Point o = (Point) obj;
        return x == o.x;
    }
}
