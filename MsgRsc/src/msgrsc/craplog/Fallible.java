package msgrsc.craplog;

/**
 * <P>
 * By implementing this interface, a class is admitting it is fallible, and therefore
 * might like to log some errors at some point or relay some helpful information to 
 * the user.
 * </P>
 * <P>
 * Since all implementing classes share the same {@link Logger} and {@link UserInformer}, 
 * this interface should only be used in single-threaded applications.
 * </P>
 */
public interface Fallible {

	UserInformer informer = InformerFactory.getUserInformer();
	
	Logger log = LoggerFactory.getLogger();
	
	
}
