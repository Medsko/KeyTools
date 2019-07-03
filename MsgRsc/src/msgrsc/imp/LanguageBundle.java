package msgrsc.imp;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import msgrsc.utils.Language;

/**
 * New and improved, this bundle holds all message resources for all languages that
 * might be required for a particular bug or RFC.  
 */
public class LanguageBundle {

	private Map<Language, Map<String, String>> unsortedBundle;
	
	private Map<Language, Map<String, String>> sortedBundle;
	
	private Language activeLanguage;
	
	private Map<Language, List<String>> missingTranslations;
	
	/** 
	 * The numeric part of the bug number for which this language bundle was 
	 * constructed. Sort of doubles as it's identifier.  
	 */
	private String bareBugNumber;
	
	public LanguageBundle(String bareBugNumber) {
		unsortedBundle = new HashMap<>();
		missingTranslations = new HashMap<>();
		this.bareBugNumber = bareBugNumber;
	}
	

	public void addMessage(String key, String message) {
		if (activeLanguage == null) {
			throw new IllegalStateException("The LanguageBundle has not been"
					+ " activated for any language!");
		}
		
		if (unsortedBundle == null) {
			// This bundle has been set to output mode.
			throw new IllegalStateException("The LanguageBundle has been put"
					+ " to output mode! No new messages please!");
		}
		
		Map<String, String> messageMap = unsortedBundle.get(activeLanguage);
		if (messageMap == null) {
			// First message resource for this language. Initialize the map.
			messageMap = new HashMap<>();
			unsortedBundle.put(activeLanguage, messageMap);
		}
		
		if (messageMap.put(key, message) != null) {
			// The message for this key was already filled with another value.
			// This could be a valid situation: if a message key was requested twice, it might be nice to select the second one.
			// TODO: figure out a course of action for these situations.
		}
	}
	
	/**
	 * Returns the translation that was read from the Excel file for the given key.
	 * If no translation for a matching key was provided, an empty String is returned.  
	 */
	public String getMessage(String key) {
		if (unsortedBundle.get(activeLanguage) == null) {
			// No translations for this language have been read.
			return null;
		}
		String message = unsortedBundle.get(activeLanguage).get(key);
		if (message == null) {
			return "";
		}
		return message;	
	}
	
	public boolean containsKey(String key) {
		return unsortedBundle.containsKey(key);
	}
	
	public void setActiveLanguage(Language language) {
		if (language == Language.DUTCH || language == Language.ENGLISH)
			throw new IllegalArgumentException("It makes no sense to import"
					+ " Dutch or English message resources!");
		activeLanguage = language;
	}
	
	/**
	 * Adds the given message key for a missing translation to the list of
	 * missing translations for the currently active {@link Language}.
	 * 
	 * @param messageKey - the key of a message resource for which the 
	 * translation was missing from the import file. 
	 */
	public void addMissingTranslation(String messageKey) {
		if (activeLanguage == null) {
			throw new IllegalStateException("The LanguageBundle has not been"
					+ " activated for any language!");
		}
		
		if (missingTranslations.get(activeLanguage) == null) {
			missingTranslations.put(activeLanguage, new ArrayList<String>());
		}
		missingTranslations.get(activeLanguage).add(messageKey);
	}
	
	/**
	 * Tests whether this {@link LanguageBundle} is empty.
	 * @return {@code true} if no message resources have been loaded into this
	 * bundle for the given bug number, {@code false} otherwise. 
	 */
	public boolean isEmpty() {
		boolean isEmpty = true;
		for (Map<String, String> bundle : unsortedBundle.values()) {
			if (bundle.size() > 0) {
				isEmpty = false;
			}
		}
		return isEmpty;
	}
	
	public String getBareBugNumber() {
		return bareBugNumber;
	}
	
	
	/*
	 * This method and the accompanying BundleSorter inner class were eventually not
	 * deemed useful. They remain here, however, because the solution it provides
	 * could prove effective in other situations - and I don't want to look up the
	 * StackOverflow page again where I stole it from :)
	 */
	@SuppressWarnings("unused")
	private void sort() {
		BundleSorter sorter = new BundleSorter(unsortedBundle);
		sortedBundle = new TreeMap<>(sorter);
		sortedBundle.putAll(unsortedBundle);
		// Set the unsortedBundle to null, so the caller can not add any more
		// message resources while the bundle is in output mode.
		unsortedBundle = null;
	}

	private class BundleSorter implements Comparator<Language> {

		Map<Language, Map<String, String>> reference;
		
		public BundleSorter(Map<Language, Map<String, String>> reference) {
			this.reference = reference;
		}
		
		@Override
		public int compare(Language o1, Language o2) {
			if (reference.get(o1).size() > reference.get(o2).size())
				return 1;
			if (reference.get(o1).size() < reference.get(o2).size())
				return -1;
			return 0;
		}
	}
}
