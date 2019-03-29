package msgrsc.utils;

public class CliLogger implements Logger {

	@Override
	public void log(String message) {
		System.out.println(message);
	}

	@Override
	public void log(Object object) {
		System.out.println(object);
	}
}
