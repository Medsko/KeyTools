package msgrsc.craplog;

public interface Logger {

	void log(String message);
	
	void log(Object object);
	
	void debug(Object object);
	
	void error(Object object);
}
