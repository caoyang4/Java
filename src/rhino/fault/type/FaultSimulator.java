package src.rhino.fault.type;

import src.rhino.fault.FaultInjectProperties;

/**
 * @author zhanjun on 2017/6/30.
 */
public interface FaultSimulator {

    /**
     * method to simulate fault
     * @param <T>
     * @return
     * @throws Exception
     */
    <T> T simulate() throws Exception;

    /**
     * method to simulate return specific value
     * @param returnType
     * @param <T>
     * @return
     * @throws Exception
     */
    <T> T simulate(Class<T> returnType) throws Exception;

    class Factory {

        private static FaultSimulator EMPTY = FaultType.EMPTY.create(FaultInjectProperties.Factory.create(null));

        public static FaultSimulator create(FaultInjectProperties properties) {
            return FaultType.get(properties.getType()).create(properties);
        }

        public static FaultSimulator empty() {
            return EMPTY;
        }
    }
}
