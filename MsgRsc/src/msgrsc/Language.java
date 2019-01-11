package msgrsc;

public enum Language {

	GERMAN("DE"),
	FRENCH("FR"),
	FLEMISH("VLS"),
	NORWEGIAN("NB");
		
	public String code;
	
	Language(String code) {
		this.code = code;
	}
	
	public static Language fromRepresentation(String representation) {
		for (Language language : Language.values()) {
			// If the given string matches either the full name of the language (e.g. "GERMAN")....
			if (language.name().equals(representation.toUpperCase())
					// ...or the code for that language (e.g. "DE")...
					|| language.code.equals(representation.toUpperCase())) {
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
	
	public String getCode() {
		return code;
	}
}
