package src.basis.fish;

/**
 * @author caoyang
 */
public class TestTryFinally {
    public int i = 0;
    public int j = 0;

    public int getI(){
        try{
            i++;
            int k = i / 0;
            return i;
        } catch (Exception e){
            e.printStackTrace();
            i++;
            return i;
        }
        finally {
            i++;
            j++;
            return i;
        }
    }

    public static void main(String[] args) {
        TestTryFinally test = new TestTryFinally();
        // return 按照 finally -> catch -> try 分支优先执行，finally最优先
        System.out.println(test.getI());
        System.out.println(test.i);
        System.out.println(test.j);
    }
}
