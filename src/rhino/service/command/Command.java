package src.rhino.service.command;

/**
 * Created by zhen on 2018/11/29.
 */
public interface Command {

    /**
     *
     * @return
     */
    String getName();

    /**
     *
     * @return
     */
    Object run();

    class Factory {

        public static Command create(CommandProperties commandProperties) {
            int rhinoType = commandProperties.getRhinoType();
            switch (rhinoType) {
                case 1:
                    return new CircuitBreakerCommand(commandProperties);
                case 3:
                    return new FaultInjectCommand(commandProperties);
                case 9:
                    return new OneLimiterCommand(commandProperties);
                default:
                    throw new UnsupportedOperationException("unsupported rhino type:" + rhinoType);
            }
        }
    }
}
