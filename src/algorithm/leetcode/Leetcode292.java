package src.algorithm.leetcode;

/**
 * Nim 游戏
 * 桌子上有一堆石头,你们轮流进行自己的回合， 你作为先手 。
 * 每一回合，轮到的人拿掉 1 - 3 块石头,拿掉最后一块石头的人就是获胜者。
 * 假设你们每一步都是最优解。请编写一个函数，来判断你是否可以在给定石头数量为 n 的情况下赢得游戏。如果可以赢，返回 true；否则，返回 false
 * @author caoyang
 */
public class Leetcode292 {
    public static boolean canWinNimDP(int n) {
        if(n <= 3){
            return true;
        }
        boolean select1 = true;
        boolean select2 = true;
        boolean select3 = true;
        for (int i = 4; i <= n; i++) {
            boolean select = !(select1 && select2 && select3);
            select1 = select2;
            select2 = select3;
            select3 =  select;
        }
        return select3;
    }
    public static boolean canWinNim(int n){
        return n % 4 != 0;
    }

    public static void main(String[] args) {
        int n = 4;
        boolean res = canWinNim(n);
        System.out.println(res);
    }
}
