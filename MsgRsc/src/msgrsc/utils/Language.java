package msgrsc.utils;

import java.nio.file.Paths;

public enum Language {

	DUTCH("NL"),
	ENGLISH("EN"),
	// The order of the foreign languages is important: this is used to determine for which
	// languages a translation is still required for the TranslationRequest.
	GERMAN("DE"),
	FRENCH("FR"),
	FLEMISH("VLS"),
	NORWEGIAN("NB");
	
	public String code;
	
	Language(String code) {
		this.code = code;
	}
	
	public static Language fromCode(String code) {
		if (code == null) {
			return null;
		}
		
		for (Language language : Language.values()) {
			// If the given string matches either the full name of the language (e.g. "GERMAN")....
			if (language.name().equals(code.toUpperCase())
					// ...or the code for that language (e.g. "DE")...
					|| language.code.equals(code.toUpperCase())) {
				// ...return the instance for that language.
				return language;
			}
		}
		return null;
	}
	
	public static Language fromStringContains(String containingLanguage) {
		if (containingLanguage == null) {
			return null;
		}
		
		for (Language language : Language.values()) {
			if (containingLanguage.toUpperCase().contains(language.name())
					|| containingLanguage.toUpperCase().contains(language.code)) {
				return language;
			}
		}
		return null;
	}
	
	public static Language fromStringContainsFullName(String containingFullName) {
		if (containingFullName == null) {
			return null;
		}
		
		for (Language language : Language.values()) {
			if (containingFullName.toUpperCase().contains(language.name())) {
				return language;
			}
		}
		return null;
	}
	
	/**
	 * Determines the language that the given message resource file is for and
	 * returns that {@link Language} instance.
	 */
	public static Language fromMrFileName(String mrFileName) {
		if (mrFileName == null) {
			return null;
		}
		// Strip away any path included in the given String.
		mrFileName = Paths.get(mrFileName).getFileName().toString();
		// Determine the language code embedded in the file name.
		int start = mrFileName.indexOf('_') + 1;
		int end = mrFileName.indexOf('.');
		if (start < 0 || end < 0) {
			// Invalid message resource file name.
			throw new IllegalArgumentException("Please pass a valid message "
					+ "resource file name!");
		}
		String fileNameLanguageCode = mrFileName.substring(start, end);
		
		return fromCode(fileNameLanguageCode);
	}
	
	/**
	 * Returns an array containing all languages that Keylane employees are not
	 * expected to provide translations for. 
	 */
	public static Language[] foreignLanguages() {
		Language[] foreignLanguages = new Language[Language.values().length - 2];
		int index = 0;
		for (Language language : Language.values()) {
			if (language != DUTCH && language != ENGLISH) {
				foreignLanguages[index++] = language;
			}
		}
		return foreignLanguages;
	}
	
	/**
	 * Returns an array of the languages that Keylane employees have (supposedly) mastered. 
	 */
	public static Language[] masteredLanguages() {
		Language[] masteredLanguages = new Language[2];
		masteredLanguages[0] = DUTCH;
		masteredLanguages[1] = ENGLISH;
		return masteredLanguages;
	}
	
	/**
	 * Returns a capitalized, but slightly less aggressive looking version of this 
	 * Language's code. 
	 */
	public String getPrettyCode() {
		return code.substring(0, 1) + code.substring(1).toLowerCase();
	}
	
	/**
	 * Returns a capitalized, but slightly less aggressive looking version of this 
	 * Language's name. 
	 */
	public String getPrettyName() {
		return name().substring(0, 1) + name().substring(1).toLowerCase();
	}
	
	public String getCode() {
		return code;
	}
}
