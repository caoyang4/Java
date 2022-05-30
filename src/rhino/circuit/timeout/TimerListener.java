package src.rhino.circuit.timeout;


/**
 * @author zhanjun
 */
public interface TimerListener {

    /**
     * The 'tick' is called each time the interval occurs.
     * <p>
     * This method should NOT block or do any work but instead fire its work asynchronously to perform on another thread otherwise it will prevent the Timer from functioning.
     * <p>
     * This contract is used to keep this implementation single-threaded and simplistic.
     * <p>
     * If you need a ThreadLocal set, you can store the state in the TimerListener, then when tick() is called, set the ThreadLocal to your desired value.
     */
    void tick();

    /**
     * How often this TimerListener should 'tick' defined in milliseconds.
     * @return
     */
    long getIntervalTimeInMilliseconds();
}