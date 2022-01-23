package src.designPattern.prototype;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 原型模式实现标记型接口 Cloneable
 * @author caoyang
 */
public class Main {
    public static void main(String[] args) {
        Person person = new Person();
        person.setAge(18);
        person.setName("james");
        person.setAddress(new Address("China", "Shanghai"));

        Person person1 = person.clone();

        System.out.println("compare person: " + (person == person1));
        System.out.println("compare age: " + (person.getAge() == person.getAge()));
        System.out.println("compare name: " + (person.getName() == person.getName()));
        System.out.println("compare address: " + (person.getAddress() == person.getAddress()));
    }
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class Person implements Cloneable{
    private Address address;
    private String name;
    private int age;


    @Override
    public Person clone() {
        try {
            /*
            super.clone()
            仅仅拷贝了源的内存
            此处为浅拷贝，因为Address为实现克隆
             */
            Person clone = (Person) super.clone();
            // TODO: 复制此处的可变状态，这样此克隆就不能更改初始克隆的内部
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class Address{
    String nation;
    String city;
}
