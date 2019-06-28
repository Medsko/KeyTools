package msgrsc.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class LiquibaseElement {

	// The string up to the first white space.
	private String tag;
	
	// The part of the element that holds a value.
	private String value;
	
	// The specified language code for a translation element.
	private String languageCode;
	
	// TODO: this is not scalable. Attributes should be held in a Map.
	
	// The 'name' attribute of a column tag.
	private String columnName;
	
	// The 'tableName' attribute of an insert-/updateTranslations tag.
	private String tableName;
	
	// The line number where the tag starts.
	private Integer lineTagStart;
	
	private List<LiquibaseElement> children;
	
	private LiquibaseElement parent;
	
	public LiquibaseElement(String tag) {
		this.tag = tag;
	}
	
	public void addChild(LiquibaseElement child) {
		if (children == null)
			children = new ArrayList<>();
		children.add(child);
	}
	
	public List<LiquibaseElement> getChildrenBy(Predicate<LiquibaseElement> condition) {
		List<LiquibaseElement> favoriteChildren = new ArrayList<>();
		getChildrenBy(favoriteChildren, condition);
		return favoriteChildren;
	}
	
	/**
	 * Recursively walks through all elements in this element, and adds
	 * all children that match the given condition to the list. 
	 */
	public void getChildrenBy(List<LiquibaseElement> list, 
			Predicate<LiquibaseElement> condition) {
		if (children != null) {
			// Recursive call for each of this element's children.
			children.stream().forEach(
					(child)-> child.getChildrenBy(list, condition)
			);
		}
		// Check if this element should be added to the collection.
		if (condition.test(this)) {
			list.add(this);
		}
	}
	
	/**
	 * Returns the first LiquibaseElement matching the given condition.
	 * If no such element is found, null is returned. 
	 */
	public LiquibaseElement getFirstWith(Predicate<LiquibaseElement> condition) {
		if (condition.test(this)) {
			return this;
		}
		if (children != null) {
			for (LiquibaseElement child : children) {
				LiquibaseElement hit = child.getFirstWith(condition);
				if (hit != null)
					return hit;
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		StringBuilder toString = new StringBuilder("Tag: ");
		toString.append(tag);
		
		if (children != null) {
			children.stream().forEach(
				(child)-> child.toString(toString, 1)
			);
		}
		
		return toString.toString();
	}
	
	public StringBuilder toString(StringBuilder toString, int indent) {
		toString.append(System.lineSeparator());
		for (int i=0; i<indent; i++) {
			toString.append("\t");
		}
		toString.append("Tag: ");
		toString.append(tag);
		if (languageCode != null) {
			toString.append(", languageCode: ");
			toString.append(languageCode);
		}
		if (value != null) {
			toString.append(", value: ");
			toString.append(value);
		}
		if (children != null) {
			children.stream().forEach(
				(child)-> child.toString(toString, indent + 1)
			);
		}
		return toString;
	}

	public LiquibaseElement getParent() {
		return parent;
	}

	public void setParent(LiquibaseElement parent) {
		this.parent = parent;
	}

	public String getTag() {
		return tag;
	}

	public String getValue() {
		return value;
	}

	public Integer getLineTagStart() {
		return lineTagStart;
	}

	public void setLineTagStart(Integer lineTagStart) {
		this.lineTagStart = lineTagStart;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getLanguageCode() {
		return languageCode;
	}

	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}

	public List<LiquibaseElement> getChildren() {
		return children;
	}
}
