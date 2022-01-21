package src.basis.genericity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author caoyang
 */
public class MainTest {
    public static void main(String[] args) {
        GenericClass<String> generic = new GenericClass<>();
        generic.setT("generic");
        System.out.println(generic.getT());
        generic.showK(new Pig("花姑娘"));

        ((GenericInterface<String>) s -> {
            System.out.println(s + " biubiubiu...");
        }).shoot("AK47");
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class Pig{
    private String name;
}
