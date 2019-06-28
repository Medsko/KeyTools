package msgrsc.dao;

import java.util.HashMap;
import java.util.Map;

import msgrsc.utils.Language;

public class MessageResource {

	// TODO: use this class when it is handy!
	
	private String key;
	
	private Map<Language, String> translations;
	
	public MessageResource(String key) {
		this.key = key;
		translations = new HashMap<>();
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
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MessageResource other = (MessageResource) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

	/**
	 * Returns the complete key-translation pair of this message resource
	 * for the given language. If no translation for the language is present,
	 * null is returned.
	 * 
	 * @return the complete key-translation pair, or null if no translation is
	 * present for the given language.  
	 */
	public String toString(Language language) {
		String translation = translations.get(language);
		if (translation != null)
			return key + "=" + translations.get(language);
		return null;
	}
	
	public String toString() {
		String toString = "Key: " + key + ", translations: " + System.lineSeparator();
		for (Language language : translations.keySet()) {
			toString += language.getPrettyName() + ": " + translations.get(language) 
				+ System.lineSeparator();
		}
		return toString;
	}
}
