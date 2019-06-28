package msgrsc.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Minimal representation of a QIS language table. 
 */
public class LanguageTable {

	public final static String FIELD_NAME = "name";
	
	public final static String FIELD_DESCRIPTION = "description";
	
	private String name;
	
	private List<String> fields;
	
	private String[] pkFields;
	
	/** The hits that were found for the searched term in this table. */	
	private List<DbTermHit> hits;
	
	public LanguageTable(String name) {
		this.name = name;
		fields = new ArrayList<>();
		hits = new ArrayList<>();
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
	
	public List<DbTermHit> getHits() {
		return hits;
	}

	public String[] getPkFields() {
		return pkFields;
	}

	public void setPkFields(String[] pkFields) {
		this.pkFields = pkFields;
	}

	public void addHit(DbTermHit hit) {
		hits.add(hit);
	}
	
	public DbTermHit getHit(Integer[] pk) {
		for (DbTermHit hit : hits) {
			if (Arrays.equals(hit.getPkValues(), pk)) {
				return hit;
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		String toString = "Table " + name + ", fields: ";
		for (int i=0; i<fields.size(); i++) {
			if (i > 0) 
				toString += ", ";
			toString += fields.get(i);
		}
		return toString;
	}
	
	/**
	 * Filters out all {@link DbTermHit}s that do not conform to given condition.
	 */
	public void filterHits(Predicate<DbTermHit> condition) {
		hits = hits.stream()
				.filter(condition)
				.collect(Collectors.toList());
	}
	
	public String toStringWithHits() {
		String toString = "Table " + name + ", hits: ";
		for (DbTermHit hit : hits) {
			toString += System.lineSeparator() + "\t";
			toString += hit;
		}
		return toString;
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
