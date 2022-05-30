package src.rhino.log;

public class SimpleLogger implements Logger {

    private String loggerName;

    public SimpleLogger(String loggerName) {
        this.loggerName = loggerName;
    }

    @Override
    public void debug(String message) {
        System.out.println(message);
    }

    @Override
    public void info(String message) {
        System.out.println(message);
    }

    @Override
    public void warn(String message) {
        System.out.println(message);
    }

    @Override
    public void warn(String message, Throwable t) {
        System.out.println(message);
        t.printStackTrace(System.out);
    }

    @Override
    public void error(String message) {
        System.err.println(message);
    }

    @Override
    public void error(String message, Throwable t) {
        System.err.println(message);
        t.printStackTrace(System.err);
    }
}
