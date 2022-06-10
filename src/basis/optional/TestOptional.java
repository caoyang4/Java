package src.basis.optional;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author caoyang
 * @create 2022-06-10 14:03
 */
public class TestOptional {

    public Integer plus(Integer a, Integer b) {
        return Optional.ofNullable(a).orElse(0) + Optional.ofNullable(b).orElse(0);
    }

    @Test
    public void test1(){
        System.out.println(plus(null, 1));
        System.out.println(plus(2, 3));
    }

    @Test
    public void test2(){
        Optional op1 = Optional.ofNullable(null);
        Optional<User> op2 = Optional.of(new User(233, "james"));
        Optional<User> op3 = Optional.ofNullable(null);
        System.out.println(op1);
        System.out.println(op2);
        System.out.println(op3);
    }
    @Test
    public void test3(){
        Optional<User> op = Optional.of(new User(233, "kobe"));
        User user = op.get();
        System.out.println(user);
        Optional<String> name = op.map(User::getName);
        System.out.println(name);
    }
    @Test
    public void test4(){
        Optional<User> empty = Optional.empty();
        User user = empty.orElseGet(() -> new User(123, "laotie"));
        System.out.println(user);
        try {
            User user1 = empty.orElseThrow(NullPointerException::new);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    static class User{
        int id;
        String name;
    }
}
