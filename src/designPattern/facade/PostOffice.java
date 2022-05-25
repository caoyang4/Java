package src.designPattern.facade;

/**
 * @author caoyang
 * @create 2022-05-25 14:56
 */
public class PostOffice {
    LetterService letterService;

    public PostOffice(LetterService letterService) {
        this.letterService = letterService;
    }

    public void sendLetter(String context, String address){
        letterService.write(context);
        letterService.putLetter();
        letterService.sendLetter(address);
    }
}
