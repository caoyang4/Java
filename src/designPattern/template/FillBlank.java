package src.designPattern.template;

/**
 * @author caoyang
 * @create 2022-05-25 15:42
 */
public abstract class FillBlank {

    // 钩子函数
    public boolean signature() {
        return false;
    }

    abstract void fillName();

    abstract void fillAddress();

    void fill(){
        fillName();
        fillAddress();
        if (signature()) {
            System.out.println("need signature");
        } else {
            System.out.println("no need signature");
        }
    }

}
