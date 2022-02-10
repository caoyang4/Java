package src.spring.ioc.circular;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Z {
    X x;
    String name;
}
