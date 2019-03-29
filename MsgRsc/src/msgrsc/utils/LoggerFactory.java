package msgrsc.utils;

public class LoggerFactory {

	public static Logger getLogger() {
		Logger logger;
		logger = new CliLogger();
		return logger;
	}
	
}
