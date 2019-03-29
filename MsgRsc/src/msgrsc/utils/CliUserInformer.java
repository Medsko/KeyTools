package msgrsc.utils;

public class CliUserInformer implements UserInformer {

	@Override
	public void informUser(String message) {
		System.out.println(message);
	}

}
