package msgrsc.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper that replaces special characters with their Unicode representation.
 * Courtesy of Thomas Macht (mostly for hardcoding all them codes). 
 */
public class TranslationToolHelper {
	/**
	 * Saves the code (value) that should be used in the resource files per special character (key)
	 */
	protected final Map<String, String> specialCharacterReplacements = new HashMap<>();

	public TranslationToolHelper() {
		// Initialize special characters
		// TODO: Add characters for other languages as needed or make this generic so that for instance
		// all non-ASCII chars are replaced by corresponding codes
		
		// Swedish characters
		specialCharacterReplacements.put("\u00E4", "\\u00E4"); // auml
		specialCharacterReplacements.put("\u00C4", "\\u00C4"); // Auml
		specialCharacterReplacements.put("\u00E5", "\\u00E5"); // aring
		specialCharacterReplacements.put("\u00C5", "\\u00C5"); // Aring
		specialCharacterReplacements.put("\u00F6", "\\u00F6"); // ouml
		specialCharacterReplacements.put("\u00D6", "\\u00D6"); // Ouml
		
		// Norwegian characters
		// aelig: see French
		// AElig: see French
		// aring: see Swedish
		// Aring: see Swedish
		specialCharacterReplacements.put("\u00F8", "\\u00F8"); // oslash
		specialCharacterReplacements.put("\u00D8", "\\u00D8"); // Oslash

		// German characters
		// auml: see Swedish
		// Auml: see Swedish
		// ouml: see Swedish
		// Ouml: see Swedish
		specialCharacterReplacements.put("\u00FC", "\\u00FC"); // uuml
		specialCharacterReplacements.put("\u00DC", "\\u00DC"); // Uuml
		specialCharacterReplacements.put("\u00DF", "\\u00DF"); // szlig
		
		// Dutch characters
		specialCharacterReplacements.put("\u00E1", "\\u00E1"); // aacute
		specialCharacterReplacements.put("\u00E9", "\\u00E9"); // eacute
		specialCharacterReplacements.put("\u00ED", "\\u00ED"); // iacute
		specialCharacterReplacements.put("\u00F3", "\\u00F3"); // oacute
		specialCharacterReplacements.put("\u00FA", "\\u00FA"); // uacute
		specialCharacterReplacements.put("\u00EB", "\\u00EB"); // euml
		specialCharacterReplacements.put("\u00EF", "\\u00EF"); // iuml
		
		// French characters
		specialCharacterReplacements.put("\u00C0", "\\u00C0"); // Agrave
		specialCharacterReplacements.put("\u00E0", "\\u00E0"); // agrave
		specialCharacterReplacements.put("\u00C2", "\\u00C2"); // Acirc
		specialCharacterReplacements.put("\u00E2", "\\u00E2"); // acirc
		specialCharacterReplacements.put("\u00C6", "\\u00C6"); // AElig
		specialCharacterReplacements.put("\u00E6", "\\u00E6"); // aelig
		specialCharacterReplacements.put("\u00C7", "\\u00C7"); // Ccedil
		specialCharacterReplacements.put("\u00E7", "\\u00E7"); // ccedil
		specialCharacterReplacements.put("\u00C8", "\\u00C8"); // Egrave
		specialCharacterReplacements.put("\u00E8", "\\u00E8"); // egrave
		specialCharacterReplacements.put("\u00C9", "\\u00C9"); // Eacute
		// eacute: see Dutch
		specialCharacterReplacements.put("\u00CA", "\\u00CA"); // Ecirc
		specialCharacterReplacements.put("\u00EA", "\\u00EA"); // ecirc
		specialCharacterReplacements.put("\u00CB", "\\u00CB"); // Euml
		// euml: see Dutch
		specialCharacterReplacements.put("\u00CE", "\\u00CE"); // Icirc	
		specialCharacterReplacements.put("\u00EE", "\\u00EE"); // icirc
		specialCharacterReplacements.put("\u00CF", "\\u00CF"); // Iuml
		// iuml: see Dutch
		specialCharacterReplacements.put("\u00D4", "\\u00D4"); // Ocirc
		specialCharacterReplacements.put("\u00F4", "\\u00F4"); // ocirc
		specialCharacterReplacements.put("\u008C", "\\u008C"); // OElig
		specialCharacterReplacements.put("\u009C", "\\u009C"); // oelig
		specialCharacterReplacements.put("\u00D9", "\\u00D9"); // Ugrave
		specialCharacterReplacements.put("\u00F9", "\\u00F9"); // ugrave
		specialCharacterReplacements.put("\u00DB", "\\u00DB"); // Ucirc
		specialCharacterReplacements.put("\u00FB", "\\u00FB"); // ucirc
		// Uuml: see Swedish
		// uuml: see Swedish
		
		// Quotes
		specialCharacterReplacements.put("\u2018", "\\u2018"); // lsquo
		specialCharacterReplacements.put("\u201A", "\\u201A"); // rsquo
		specialCharacterReplacements.put("\u2019", "\\u2019"); // sbquo
		specialCharacterReplacements.put("\u201C", "\\u201C"); // ldquo
		specialCharacterReplacements.put("\u201D", "\\u201D"); // rdquo
		specialCharacterReplacements.put("\u201E", "\\u201E"); // bdquo
		specialCharacterReplacements.put("\u00AB", "\\u00AB"); // laquo
		specialCharacterReplacements.put("\u00BB", "\\u00BB"); // raquo
		
		// Other characters
		specialCharacterReplacements.put("\u20AC", "\\u20AC"); // euro
		specialCharacterReplacements.put("\u2026", "\\u2026"); // hellip
		specialCharacterReplacements.put("\u2264", "\\u2264"); // less-than or equal to
		specialCharacterReplacements.put("\u00B0", "\\u00B0"); // degree sign
		specialCharacterReplacements.put("\u00A0", ""); // no-break space
	}
	
	/**
	 * Checks whether the given text contains any special characters.
	 * @return {@link true} if the text contains any special characters, {@code false} otherwise.  
	 */
	public boolean containsSpecialCharacters(String text) {
		boolean containsSpecialCharacters = false;
		for (String specialCharacter : specialCharacterReplacements.keySet()) {
			if (text.contains(specialCharacter)) {
				containsSpecialCharacters = true;
			}
		}
		return containsSpecialCharacters;
	}
	
	/**
	 * Reverts any work that {@link #replaceSpecialCharactersByCodes(String)} might have done.
	 * @return the input String, with all escaped unicode characters un-escaped.
	 */
	public String replaceCodeBySpecialCharacters(String text) {
		for (Map.Entry<String, String> entry : specialCharacterReplacements.entrySet()) {
			
			String encoded = entry.getValue();
			String replacement = entry.getKey();
			if (encoded.equals("")) {
				continue;
			}
			text = text.replace(encoded, replacement);
		}
		return text;
	}
	
	/**
	 * Replaces special characters by Unicode codes.
	 * @return the input String, with all special characters replaces by Unicode codes. 
	 */
	public String replaceSpecialCharactersByCodes(String text) {
		String result = text;
		// Replace special characters by Unicode codes.
		for (String key : specialCharacterReplacements.keySet()) {
			result = result.replace(key, specialCharacterReplacements.get(key));
		}
		
		return result;

	}	

}
