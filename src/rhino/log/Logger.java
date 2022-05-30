package src.rhino.log;

public interface Logger {

	void debug(String message);

	void info(String message);

	void warn(String message);

	void warn(String message, Throwable t);

	void error(String message);

	void error(String message, Throwable t);
}
