package src.designPattern.facade;

/**
 * @author caoyang
 * @create 2022-05-25 14:59
 */
public class Sender {
    public static void main(String[] args) {
        PostOffice office = new PostOffice(new LetterServiceImpl());
        office.sendLetter("exciting!", "hell");
    }
}
