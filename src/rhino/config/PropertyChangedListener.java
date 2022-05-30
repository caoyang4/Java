package src.rhino.config;

/**
 * @author zhanjun
 * @date 2017/11/05
 */
public abstract class PropertyChangedListener<T> {

    /**
     * 监听单个属性的变动
     * @param oldProperty
     * @param newProperty
     */
    public abstract void trigger(T oldProperty, T newProperty);
}
