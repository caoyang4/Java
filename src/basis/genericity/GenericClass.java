package src.basis.genericity;

import lombok.Data;

/**
 * 泛型类 与 泛型方法
 * @author caoyang
 */
@Data
public class GenericClass<T> {
    private T t;

    <K> void showK(K k){
        System.out.println(k);
    }
}
