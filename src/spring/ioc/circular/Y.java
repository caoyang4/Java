package src.spring.ioc.circular;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Y {
    X x;
    String name;

}
