package msgrsc.dao;

import java.util.HashMap;
import java.util.Map;

import msgrsc.utils.Language;

public class DbTermHit {

	private String fieldName;
	
	private String entry;
	
	// Terribly tight coupling - these are useless without accompanying LanguageTable.
	private Integer[] pkValues;
	
	private Map<Language, String> contents;
	
	public DbTermHit() {
		contents = new HashMap<>();
	}
	
	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	
	public void addContent(Language language, String content) {
		contents.put(language, content);
	}
	
	public Map<Language, String> getContents() {
		return contents;
	}

	public String getEntry() {
		return entry;
	}

	public void setEntry(String entry) {
		this.entry = entry;
	}
	
	public Integer[] getPkValues() {
		return pkValues;
	}

	public void setPkValues(Integer[] pkValues) {
		this.pkValues = pkValues;
	}

	@Override
	public String toString() {
		String toString = "field: " + fieldName;
		for (Language language : contents.keySet()) {
			toString += System.lineSeparator() + "\t\t" 
				+ language.code + " content: " + contents.get(language);
		}
		return toString;
	}
}
