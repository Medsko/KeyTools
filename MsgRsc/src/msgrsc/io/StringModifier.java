package msgrsc.io;

/**
 * Classic Strategy pattern interface for modifying {@link String}s.
 */
@FunctionalInterface
public interface StringModifier {

	/**
	 * The single method for this interface takes a {@link String} parameter,
	 * modifies it, and returns the result. Pretty simple, right?
	 * 
	 * @param text - the input {@link String} to modify.
	 * @return the {@link String} modified in the matter specified in the 
	 * concrete implementation of this interface.
	 */
	String modify(String text);
	
}