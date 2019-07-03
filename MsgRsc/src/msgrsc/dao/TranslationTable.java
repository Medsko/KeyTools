package msgrsc.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import msgrsc.utils.Language;

/**
 * A collection of {@link Translation}s, facilitating access to
 * <ol>
 * 	<li>all {@link Translation}s for a given {@link Language};</li>
 * 	<li>all {@link Language}s a particular {@link Translation} is requested for.</li>
 * </ol>
 * 
 */
public class TranslationTable {

	private Map<String, Translation> translationsByKey;

	public TranslationTable() {
		translationsByKey = new HashMap<>();
	}
	
	public void addTranslation(Translation translation) {
		translationsByKey.put(translation.getKey(), translation);
	}
	
	public List<Translation> getTranslationsFor(Language language) {
		return translationsByKey.values().stream()
			.filter(translation -> language.equals(translation.getLanguage()))
			.collect(Collectors.toList());
	}
	
}
