package msgrsc.utils;

public class InformerFactory {

	public static UserInformer getUserInformer() {
		UserInformer informer;
		
		informer = new CliUserInformer();
		
		return informer;
	}
	
}
