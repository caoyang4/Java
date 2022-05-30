package src.rhino.fault.type;

import src.rhino.fault.FaultInjectProperties;

/**
 * Created by zhanjun on 2017/6/30.
 */
public enum FaultType {

    EMPTY(0) {
        @Override
        public FaultSimulator create(FaultInjectProperties properties) {
            return new NoOpFaultSimulator(properties);
        }
    },

    DELAY(1) {
        @Override
        public FaultSimulator create(FaultInjectProperties properties) {
            return new DelayFaultSimulator(properties);
        }
    },

    EXCEPTION(2) {
        @Override
        public FaultSimulator create(FaultInjectProperties properties) {
            return new ExceptionFaultSimulator(properties);
        }
    },

    MOCK(3){
        @Override
        public FaultSimulator create(FaultInjectProperties properties) {
            return new MockDataSimulator(properties);
        }
    };

    private int value;

    FaultType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public abstract FaultSimulator create(FaultInjectProperties properties);

    public static FaultType get(int value) {
        switch (value) {
            case 1: return DELAY;
            case 2: return EXCEPTION;
            case 3: return MOCK;
            default: return EMPTY;
        }
    }

    public boolean isDelay() {
        return this == DELAY;
    }

    public boolean isException() {
        return this == EXCEPTION;
    }
}
