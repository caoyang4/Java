package src.designPattern.builder;

/**
 * @author caoyang
 */
public interface PersonBuilder {
    Engineer buildLocation();
    Engineer buildHeight();
    Engineer buildWeight();
    Engineer build();
}
