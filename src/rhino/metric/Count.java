package src.rhino.metric;


/**
 * @author zhanjun
 */
public class Count {

    private int value;

    public Count() {
    }

    public Count(int value) {
        this.value = value;
    }

    public void add(int next) {
        this.value = value + next;
    }

    public int getValue() {
        return value;
    }
}
