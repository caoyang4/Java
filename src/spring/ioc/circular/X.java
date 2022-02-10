package src.spring.ioc.circular;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class X {
    Y y;
    Z z;
    String name;
}
