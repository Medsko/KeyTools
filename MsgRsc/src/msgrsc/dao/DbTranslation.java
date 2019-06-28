package msgrsc.dao;

import java.util.HashMap;
import java.util.Map;

import msgrsc.utils.Language;

public class DbTranslation {

	private String table;
	
	private String key;
	
	private String columnName;
	
	private String textEnglish;
	
	private String textDutch;
	
	private Map<Language, String> translations;
	
	private boolean[] requestedFor;

	public void addTranslation(Language language, String translation) {
		if (translations == null) {
			translations = new HashMap<>();
		}
		translations.put(language, translation);
	}
	
	public String getTranslation(Language language) {
		if (translations == null) {
			return null;
		}
		return translations.get(language);
	}
	
	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
	
	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getTextEnglish() {
		return textEnglish;
	}

	public void setTextEnglish(String textEnglish) {
		this.textEnglish = textEnglish;
	}

	public String getTextDutch() {
		return textDutch;
	}

	public void setTextDutch(String textDutch) {
		this.textDutch = textDutch;
	}

	public boolean[] getRequestedFor() {
		return requestedFor;
	}

	public void setRequestedFor(boolean[] requestedFor) {
		this.requestedFor = requestedFor;
	}
}
