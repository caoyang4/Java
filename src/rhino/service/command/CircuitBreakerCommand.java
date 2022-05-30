package src.rhino.service.command;

import java.util.Map;

import src.rhino.RhinoType;
import src.rhino.circuit.CircuitBreaker;
import src.rhino.metric.HealthCountCollector;
import src.rhino.metric.HealthCountSummary;
import com.google.common.collect.Maps;

/**
 * Created by zhen on 2018/11/29.
 */
public class CircuitBreakerCommand implements Command {

    private HealthCountCollector healthCountCollector = HealthCountCollector.getInstance();
    private CommandProperties commandProperties;

    public CircuitBreakerCommand(CommandProperties commandProperties) {
        this.commandProperties = commandProperties;
    }

    @Override
    public String getName() {
        return RhinoType.get(commandProperties.getRhinoType());
    }

    @Override
    public Object run() {
        String rhinoKey = commandProperties.getRhinoKey();
        CircuitBreaker target = CircuitBreaker.Factory.getCircuitBreaker(rhinoKey);
        Map<String, Object> result = Maps.newHashMap();
        if (target != null) {
            result.put("circuitBreakerProperties", target.getCircuitBreakerProperties().toJson());
            result.put("status", target.getStatus().toString());
            HealthCountSummary healthCountSummary = healthCountCollector.getHealthCountSummary(rhinoKey);
            result.put("healthCountSummary", healthCountSummary.toJson());
        }
        return result;
    }
}
