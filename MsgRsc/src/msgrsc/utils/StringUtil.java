package msgrsc.utils;

import java.nio.file.Paths;

public class StringUtil {

	private static final TranslationToolHelper specialHelper = new TranslationToolHelper();
	
	private StringUtil() {}
	
	/**
	 * Returns the given string with whitespace removed, and characters after found
	 * whitespace converted to upper case. If given string is null, an empty string
	 * is returned. For Pascal case, pass result to {@link #capitalize(String)}.
	 *  
	 * @param containingSpaces - a string typically containing whitespace. 
	 * @return camel cased version of the given string.
	 */
	public static String toCamelCase(String containingSpaces) {
		String result = "";
		if (containingSpaces == null)
			return result;
		boolean isPrevCharSpace = false;
		for (char ch : containingSpaces.toCharArray()) {
			if (Character.isWhitespace(ch)) {
				isPrevCharSpace = true;
				continue;
			}	
			if (isPrevCharSpace) {
				ch = Character.toUpperCase(ch);
				isPrevCharSpace = false;
			}
			result += ch;
		}
		return result;
	}
	
	/**
	 * Replaces the first character in the given string with an upper case version of 
	 * that character.
	 */
	public static String capitalize(String string) {
		return string.replace(string.charAt(0), Character.toUpperCase(string.charAt(0)));
	}
	
	public static String determineTempFileName(String fileName) {
		String bareFileName = Paths.get(fileName).getFileName().toString();
		String extension = bareFileName.substring(bareFileName.indexOf('.'));
		String tempFileName = bareFileName.substring(0, bareFileName.indexOf('.')) 
				+ "temp" + extension;
		return tempFileName;
	}
	
	public static String toBugNumber(String bareBugNumber) {
		if (bareBugNumber == null) return null;
		return "QSD-" + bareBugNumber;
	}
	
	public static String toBareBugNumber(String bugNumber) {
		if (bugNumber == null) return null; // Kotlin style hehe
		return bugNumber.replaceAll("[QqSsDd-]", "");
	}
	
	public static boolean isValidBugNumber(String bugNumber) {
		return bugNumber != null && 
				(bugNumber.matches("(QSD|qsd)-\\d{5}") 
					|| bugNumber.matches("(SSD|ssd)-\\d{4,5}"));
	}
	
	/**
	 * Checks whether the given String contains a translation placeholder.
	 * @return {@code true} if the given text contains a placeholder.
	 */
	public static boolean containsPlaceholder(String text) {
		return text != null &&
				text.matches("(QSD|qsd|SSD|ssd)-?\\d{4,5}");
	}
		
	public static String replaceAllSpecialChars(String input) {
		return specialHelper.replaceSpecialCharactersByCodes(input);
	}
	
	public static boolean isEmpty(String string) {
		return string == null || "".equals(string);
	}
}
