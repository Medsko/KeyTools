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
					|| bugNumber.matches("(SSD|ssd)-\\d{4-5}"));
	}
	
	public static String replaceSpecialChars(String input, Language language) {
		switch (language) {
			case FRENCH:
				input = replaceFrenchSpecialChars(input);
				break;
			case NORWEGIAN:
				input = replaceScandinavianSpecialChars(input);
			case GERMAN:
				input = replaceGermanSpecialChars(input);
			default:
		}
		return input = replaceCommonSpecialChars(input);
	}
	
	public static String replaceAllSpecialChars(String input) {
		return specialHelper.replaceSpecialCharactersByCodes(input);
	}
	
	public static boolean isEmpty(String string) {
		return string == null || "".equals(string);
	}
	
	private static String replaceCommonSpecialChars(String input) {
		return input.replace("�", "\\u00E8")
				.replace("�", "\\u00C8")
				.replace("�", "\\u00E9")
				.replace("�", "\\u00C9")
				.replace("�", "\\u00EB")
				.replace("�", "\\u00CB")
				.replace("�", "\\u00EA")
				.replace("�", "\\u00FC")
				.replace("�", "\\u00DC")
				.replace("�", "\\u00E0")
				.replace("�", "\\u00C0")
				.replace("�", "\\u00F6")
				.replace("�", "\\u00EF");
	}
	
	private static String replaceFrenchSpecialChars(String input) {
		return input.replace("�", "\\u00E7");
	}
	
	private static String replaceGermanSpecialChars(String input) {
		return input.replace("�", "\\u00E4")
				.replace("�", "\\u00C4")
				.replace("�", "\\u00DF")
				.replace("�", "\\u00D6");
	}
	
	private static String replaceScandinavianSpecialChars(String input) {
		return input.replace("�", "\\u00E5")
				.replace("�", "\\u00C5")
				.replace("�", "\\u00F8")
				.replace("�", "\\u00D8")
				.replace("�", "\\u00E6")
				.replace("�", "\\u00C6");
	}
}
