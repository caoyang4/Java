package src.designPattern.builder;

import lombok.Data;
import lombok.Getter;

/**
 * @author caoyang
 */
public class Engineer implements PersonBuilder{
    private Location location;
    private Weight weight;
    private Height height;

    @Override
    public Engineer buildLocation() {
        location = new Location("shanghai");
        return this;
    }

    @Override
    public Engineer buildHeight() {
        height = new Height(172.1);
        return this;
    }

    @Override
    public Engineer buildWeight() {
        weight = new Weight(67.5);
        return this;
    }

    @Override
    public Engineer build() {
        return this;
    }

    @Override
    public String toString() {
        return "Engineer{" +
                "\n\tlocation=" + location +
                ", \n\tweight=" + weight +
                ", \n\theight=" + height +
                "\n}";
    }

    public static void main(String[] args) {
        // 链式编程
        Engineer engineer = new Engineer().buildLocation().buildHeight().buildWeight().build();
        System.out.println(engineer);
    }
}

@Getter
class Location{
    private String city;

    public Location(String city) {
        this.city = city;
    }

    @Override
    public String toString() {
        return "Location{" +
                "city='" + city + '\'' +
                '}';
    }
}

@Getter
class Height{
    private double height;

    public Height(double height) {
        this.height = height;
    }

    @Override
    public String toString() {
        return "Height{" +
                "height=" + height +
                '}';
    }
}

@Getter
class Weight{
    private double weight;

    public Weight(double weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "Weight{" +
                "weight=" + weight +
                '}';
    }
}