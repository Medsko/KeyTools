package msgrsc.utils;

public class XmlUtils {

	private XmlUtils() {}
	
	/**
	 * Gets the specified attribute from the given tag.
	 * @param tag - the XML tag to get the attribute from.
	 * @param attribute - the name of the target attribute.
	 * @return the value for the specified attribute, or null if attribute
	 * 	could not be determined.
	 */
	public static String getAtrribute(String tag, String attribute) {
		if (tag == null || attribute == null) return null;
		
		int start = tag.indexOf(attribute + "=") + attribute.length() + 2;
		int end = tag.substring(start).indexOf("\"") + start;
		
		if (start >= 0 && end >= 0) {
			return tag.substring(start, end);
		}
		return null;
	}
	
	/**
	 * Set the given value as the specified attribute in the given tag.
	 * @param tag - the XML tag to set the attribute on.
	 * @param attribute - the name of the target attribute.
	 * @param value - the value to set.
	 * @return the XML tag with the modified attribute, or null if attribute
	 * 	could not be determined.
	 */
	public static String setAttribute(String tag, String attribute, String value) {
		if (tag == null || attribute == null || value == null) return null;		
		
		int start = tag.indexOf(attribute + "=") + attribute.length() + 2;
		int end = tag.substring(start).indexOf("\"") + start;

		if (start >= 0 && end >= 0) {
			return tag.substring(0, start) + value + tag.substring(end);
		}
		return null;
	}
}
