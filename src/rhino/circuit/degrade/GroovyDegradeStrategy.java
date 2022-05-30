package src.rhino.circuit.degrade;

import org.codehaus.groovy.control.CompilationFailedException;

import src.rhino.circuit.CircuitBreakerProperties;

import groovy.lang.GroovyShell;
import groovy.lang.Script;

/**
 * Created by zhanjun on 2018/3/29.
 */
public class GroovyDegradeStrategy extends AbstractDegradeStrategy {

    private static final GroovyShell groovyShell = new GroovyShell();
    private Script script;

    public GroovyDegradeStrategy(CircuitBreakerProperties properties) {
        super(properties);
        this.script = parse(this.value);
    }

    private Script parse(String value) {
        try {
            return groovyShell.parse(value);
        } catch (CompilationFailedException e) {
            String msg = "降级策略[groovy脚本]配置有误，当前groovy脚本为：" + value;
            throw new IllegalArgumentException(msg, e);
        }
    }

    @Override
    public Object degrade() throws Exception {
        return script.run();
    }
}
