package msgrsc;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LanguageBundle {

	private Map<String, String> messages;
	
	private Path msgRscFile;
	
	private Path tempFile;
	
	private String languageCode;
	
	private List<String> missingTranslationKeys;
	
	private Language language;
	
	private LanguageBundle() {
		messages = new HashMap<>();
		missingTranslationKeys = new ArrayList<>();
	}
	
	public LanguageBundle(String languageCode) {
		this();
		this.languageCode = languageCode;
		language = Language.fromRepresentation(languageCode);
	}
	
	public LanguageBundle(Language language) {
		this();
		this.language = language;
	}
		
	public void addMessage(String key, String message) {
		if (message != null && !message.equals("")) {
			messages.put(key, message);
		} else {
			missingTranslationKeys.add(key);
		}
	}
	
	public String getMessage(String key) {
		return messages.remove(key);
	}
	
	public Set<String> getKeySet() {
		return messages.keySet();
	}

	public Path getMsgRscFile() {
		return msgRscFile;
	}

	public Path getTempFile() {
		return tempFile;
	}

	public Language getLanguage() {
		return language;
	}
	
	public int size() {
		return messages.size();
	}
}
