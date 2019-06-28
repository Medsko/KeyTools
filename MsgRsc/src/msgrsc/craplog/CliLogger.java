package msgrsc.craplog;

public class CliLogger implements Logger {

	private boolean isDebug;
	
	@Override
	public void log(String message) {
		System.out.println(message);
	}

	@Override
	public void log(Object object) {
		System.out.println(object);
	}

	@Override
	public void debug(Object object) {
		if (isDebug)
			log(object);
	}

	public void error(Object object) {
		log(object);
	}
	
	public boolean isDebug() {
		return isDebug;
	}
}
