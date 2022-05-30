package src.rhino.service.command;

import src.rhino.RhinoType;
import src.rhino.fault.FaultInject;

/**
 * Created by zhen on 2018/11/29.
 */
public class FaultInjectCommand implements Command {

    private CommandProperties commandProperties;

    public FaultInjectCommand(CommandProperties commandProperties) {
        this.commandProperties = commandProperties;
    }

    @Override
    public String getName() {
        return RhinoType.get(commandProperties.getRhinoType());
    }

    @Override
    public Object run() {
        String rhinoKey = commandProperties.getRhinoKey();
        FaultInject target = FaultInject.Factory.getFaultInject(rhinoKey);
        if (target != null) {
            return target.getFaultInjectProperties().toJson();
        }
        return null;
    }
}
