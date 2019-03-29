package msgrsc.dao;

import java.util.Map;

import msgrsc.utils.Language;

public class MessageResource {

	private String key;
	
	private Map<Language, String> translations;

	public MessageResource(String key) {
		this.key = key;
	}
	
	public String getKey() {
		return key;
	}
	
	public void addTranslation(Language language, String translation) {
		translations.put(language, translation);
	}

	public String getTranslation(Language language) {
		return translations.get(language);
	}
	
	public Map<Language, String> getTranslations() {
		return translations;
	}
}
