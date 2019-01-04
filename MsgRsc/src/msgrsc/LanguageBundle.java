package msgrsc;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LanguageBundle {

	private Map<String, String> messages;
	
	private Path msgRscFile;
	
	private Path tempFile;
	
	private String languageCode;
	
	public LanguageBundle(String languageCode) {
		this.languageCode = languageCode;
		
	}
	
	@Deprecated
	public LanguageBundle(String languageCode, String msgRscDir) {
		messages = new HashMap<>();
		String fileName = "MessageResources_" + languageCode.toLowerCase() + ".properties";
		String tempFileName = "temp" + languageCode.toLowerCase() + ".properties";
		msgRscFile = Paths.get(msgRscDir, fileName);
		tempFile = Paths.get(msgRscDir, tempFileName);
		this.languageCode = languageCode;
	}
	
	public void addMessage(String key, String message) {
		if (message != null && !message.equals(""))
			messages.put(key, message);
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
}
