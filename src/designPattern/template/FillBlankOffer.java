package src.designPattern.template;

/**
 * @author caoyang
 * @create 2022-05-25 15:46
 */
public class FillBlankOffer extends FillBlank{
    @Override
    void fillName() {
        System.out.println("james");
    }

    @Override
    void fillAddress() {
        System.out.println("lakers");
    }

    @Override
    public boolean signature() {
        return false;
    }
}
