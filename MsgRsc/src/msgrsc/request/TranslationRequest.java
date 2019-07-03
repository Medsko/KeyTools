package msgrsc.request;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import msgrsc.dao.DbTranslation;
import msgrsc.utils.Language;

/**
 * Represents a request for translations for a particular bug or RFC.
 */
public class TranslationRequest {

	private String bugNumber;
	
	private Language activeLanguage;
	
	/** 
	 * Set of all languages that have been active for this {@link TranslationRequest} 
	 * at some point.
	 */
	private Set<Language> activatedLanguages;
	
	private Map<String, boolean[]> requestedResources; 
	
	private Map<String, String> dutchMessages;
	
	private Map<String, String> englishMessages;
	
	private List<DbTranslation> dbTranslations;
		
	public TranslationRequest(String bugNumber) {
		this.bugNumber = bugNumber;
		requestedResources = new HashMap<>();
		activatedLanguages = new HashSet<>();
		dutchMessages = new HashMap<>();
		englishMessages = new HashMap<>();
		dbTranslations = new ArrayList<>();
	}

	public void addMessage(String key, String message) {
		if (activeLanguage == Language.DUTCH) {
			dutchMessages.put(key, message);
		} else if (activeLanguage == Language.ENGLISH) {
			englishMessages.put(key, message);
		}
	}
	
	public void addMessageRequest(String messageKey) {
		boolean[] requiredTranslations = requestedResources.get(messageKey);
		if (requiredTranslations == null) {
			// Add a new array to keep track of to which foreign languages the  
			// message should be translated.
			requiredTranslations = new boolean[Language.foreignLanguages().length];
			requestedResources.put(messageKey, requiredTranslations);
		}
		requiredTranslations[activeLanguage.ordinal() - Language.masteredLanguages().length] = true;
	}
	
	public void addMessageRequests(Collection<String> messageKeys) {
		for (String messageKey : messageKeys) {
			addMessageRequest(messageKey);
		}
	}

	public Set<String> getRequestedKeys() {
		return requestedResources.keySet();
	}

	public String getDutchMessage(String key) {
		String message = dutchMessages.get(key);
		if (message != null)
			return message;
		else
			return "";
	}
	
	public String getEnglishMessage(String key) {
		String message = englishMessages.get(key);
		if (message != null)
			return message;
		else
			return "";
	}
	
	public void addDbTranslation(DbTranslation dbTranslation) {
		dbTranslations.add(dbTranslation);
	}
	
	public void addDbTranslations(Collection<DbTranslation> translationsToAdd) {
		dbTranslations.addAll(translationsToAdd);
	}
	
	public List<DbTranslation> getDbTranslations() {
		return dbTranslations;
	}

	/**
	 * Returns an array that indicates for which languages the given message resource should
	 * be translated. The order of languages is conform {@link Language}.ordinal().
	 */
	public boolean[] getRequestedTranlations(String key) {
		return requestedResources.get(key);
	}
	
	public void setActiveLanguage(Language language) {
		activeLanguage = language;
		activatedLanguages.add(language);
	}

	public int getCountRequestedTranslations() {
		return requestedResources.size();
	}
	
	public boolean containsKey(String key) {
		return requestedResources.containsKey(key);
	}
	
	public String getBugNumber() {
		return bugNumber;
	}
}
