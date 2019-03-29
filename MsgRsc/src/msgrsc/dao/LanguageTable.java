package msgrsc.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Minimal representation of a QIS language table. 
 */
public class LanguageTable {

	public final static String FIELD_NAME = "name";
	
	public final static String FIELD_DESCRIPTION = "description";
	
	private String name;
	
	private List<String> fields;
	
	/** The hits that were found for the searched term in this table. */
	private Map<String, Integer> hits;
	
	public LanguageTable(String name) {
		this.name = name;
		fields = new ArrayList<>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void addField(String field) {
		fields.add(field);
	}
	
	public List<String> getFields() {
		return fields;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		LanguageTable other = (LanguageTable) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
