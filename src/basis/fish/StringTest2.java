package src.basis.fish;

public class StringTest2 {
    public static String str = "young";
    public static char[] chars = {'t', 'e', 's', 't'};

    public static void change(String str, char[] chars){
        str = "good";
        chars[0] = 'b';
    }

    public static void main(String[] args) {
        change(str, chars);
        System.out.println(str);
        System.out.println(chars);
    }
}
