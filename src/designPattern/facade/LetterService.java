package src.designPattern.facade;

/**
 * @author caoyang
 * @create 2022-05-25 14:51
 */
public interface LetterService {
    void write(String context);

    void putLetter();

    void sendLetter(String address);
}
