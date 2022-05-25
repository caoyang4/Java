package src.designPattern.facade;

/**
 * @author caoyang
 * @create 2022-05-25 14:54
 */
public class LetterServiceImpl implements LetterService{
    @Override
    public void write(String context) {
        System.out.println("letter contents: "+context);
    }

    @Override
    public void putLetter() {
        System.out.println("put letter into envelope");
    }

    @Override
    public void sendLetter(String address) {
        System.out.println("send letter to " + address);
    }
}
