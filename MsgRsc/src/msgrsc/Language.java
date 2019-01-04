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
	
	public static Language fromString(String representation) {
		for (Language language : Language.values()) {
			if (language.name().equals(representation.toUpperCase())
					|| language.code.equals(representation.toUpperCase())) {
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
	
	public String getCode() {
		return code;
	}
}
