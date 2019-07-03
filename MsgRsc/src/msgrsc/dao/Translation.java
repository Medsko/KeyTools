package msgrsc.dao;

import msgrsc.utils.Language;

/**
 * Unified data object for a database translation or message resource. 
 */
public class Translation {

	private Language language;
	
	private String key;
	
	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setLanguage(Language language) {
		this.language = language;
	}

	public Language getLanguage() {
		return language;
	}
	
}
