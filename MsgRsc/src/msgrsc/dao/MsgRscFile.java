package msgrsc.dao;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import msgrsc.utils.Language;

public class MsgRscFile {

	public enum Type {
		MSG,
		INFO
	}
	
	private String fullPath;
	
	private List<String> missingMessageKeys;
	
	private Map<String, String> messagesOfInterest;
	
	private Language language;
	
	private Type type;
	
	public MsgRscFile() {
		messagesOfInterest = new HashMap<>();
	}
	
	public String getFullPath() {
		return fullPath;
	}

	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
		// Determine the language of this file from the file name.
		String fileName = Paths.get(fullPath).getFileName().toString();
		String languageCode = fileName.substring(fileName.indexOf("_") 
				+ 1, fileName.indexOf("."));
		// This is hacky....but sooo convenient.
		language = Language.fromCode(languageCode);
		
		if (fileName.contains("Information")) {
			type = Type.INFO;
		} else {
			type = Type.MSG;
		}
	}
	
	public Language getLanguage() {
		return language;
	}
	
	public Type getType() {
		return type;
	}

	public boolean containsMissingMessages() {
		return missingMessageKeys != null && missingMessageKeys.size() > 0;
	}
	
	public List<String> getMissingMessageKeys() {
		return missingMessageKeys;
	}

	public void setMissingMessageKeys(List<String> missingMessageKeys) {
		this.missingMessageKeys = missingMessageKeys;
	}

	public void putMessageOfInterest(String key, String message) {
		messagesOfInterest.put(key, message);
	}
	
	public Map<String, String> getMessagesOfInterest() {
		return messagesOfInterest;
	}

	public boolean containsMessagesOfInterest() {
		return messagesOfInterest.size() > 0;
	}
}
