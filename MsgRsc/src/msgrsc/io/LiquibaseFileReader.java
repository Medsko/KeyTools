package msgrsc.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import msgrsc.craplog.Fallible;
import msgrsc.dao.LiquibaseElement;

/**
 * Reads a Liquibase file and converts it to a {@link LiquibaseElement} containing
 * {@link LiquibaseElement}s etc. (Composite). Typically, the resulting superelement
 * will be of kind 'databaseChangeLog'.
 * <p>
 * Sorta works for now.
 */
public class LiquibaseFileReader implements Fallible {

	private LiquibaseElement databaseChangeLog;
	
	private LiquibaseElement currentElement;
	
	private int lineNumber;
	
	private int lineTagStart;
	
	public boolean readFile(String file) {
		databaseChangeLog = null;
		currentElement = null;
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			
			lineNumber = 0;
			String line;
			String multiLineTag = null;
			boolean isMultiLine = false;
			while ((line = reader.readLine()) != null) {
				
				lineNumber++;

				if (isMultiLine) {
					
					multiLineTag += System.lineSeparator() + line;
					
					if (line.contains(">")) {
						// End of a multiline tag.
						isMultiLine = false;
						if (!evaluate(multiLineTag)) {
							logger.log("readFile - failed to parse tag: " + multiLineTag);
						}
					}
					continue;
				}
				
				lineTagStart = lineNumber;
				
				if (line.contains("<") && !line.contains(">")) {
					// Start of a tag that stretches over several lines.
					isMultiLine = true;
					multiLineTag = line;
					continue;
				}
				
				if (!evaluate(line)) {
					logger.log("readFile - failed to parse line: " + line);
				}	
			}
			
		} catch (IOException ioex) {
			logger.log("readFile - failed to read the liquibase file!");
			return false;
		}
		
		return true;
	}
	
	private boolean evaluate(String tag) {
		boolean lineRead = false;
		tag = tag.trim();
		if (tag.length() == 0)
			return true;
		// If the line contains the start tag '<', but is not a closing tag '</',
		// this is the start of a new element.
		if (tag.contains("<") && !tag.contains("</")) {
			// Start of a new element.
			// Skip the XML declaration.
			if (tag.contains("?xml")) {
				return true;
			}
			
			LiquibaseElement newElement = parseTag(tag);
			newElement.setLineTagStart(lineNumber);
			
			if (databaseChangeLog == null)
				databaseChangeLog = newElement;
			if (currentElement != null) {
				currentElement.addChild(newElement);
				newElement.setParent(currentElement);
			}
			currentElement = newElement;
			lineRead = true;
		}
		// This line contains either a closing tag or a self-closing tag.
		if (tag.contains("/>") || tag.contains("</")) {
			// End of current element. Return to the parent.
			if (currentElement == databaseChangeLog) {
				// If the current element is the super element, there is no 
				// parent to return to and processing is finished.
				return true;
			}
			currentElement = currentElement.getParent();
			lineRead = true;
		}
		
		return lineRead;
	}

	private LiquibaseElement parseTag(String line) {
		int startOfTag = line.indexOf("<") + 1;
		int endOfTag = line.indexOf(" ");
		
		if (endOfTag < 0) {
			// This tag does not contain a space. The end of tag should be the end of this line.
			// Exclude the tag closing '>'.
			endOfTag = line.length() - 1;
		}
		String tag = line.substring(startOfTag, endOfTag);
		LiquibaseElement element = new LiquibaseElement(tag);
		element.setLineTagStart(lineTagStart);
		String attributesOnLine = line.substring(endOfTag);
		
		if (attributesOnLine == null)
			// This line only contains the tag name.
			return element;
		
		// Now process the rest of the element.		
		String[] attributes = attributesOnLine.split("\"[ />]");
		
		// TODO: create constants for tags of interest.
		if (tag.equals("ext:translation")) {
			for (String attribute : attributes) {
				if (attribute.contains("languageCode")) {
					String languageCode = readAttributeValue(attribute);
					element.setLanguageCode(languageCode);
				}
				// TODO: this could be done way more generic, for lots of tag types.
				if (attribute.contains("value")) {
					String value = readAttributeValue(attribute);
					element.setValue(value);
				}
			}
		} else if (tag.equals("ext:insertTranslations") 
				|| tag.equals("ext:updateTranslations")) {
			for (String attribute : attributes) {
				if (attribute.contains("tableName")) {
					String tableName = readAttributeValue(attribute);
					element.setTableName(tableName);
				}
			}
		} else if (tag.equals("ext:column")) {
			for (String attribute : attributes) {
				if (attribute.contains("name")) {
					String columnName = readAttributeValue(attribute);
					element.setColumnName(columnName);
				}
			}
		}

		return element;
	}
	
	private String readAttributeValue(String attribute) {
		return attribute.substring(
				attribute.indexOf("\"") + 1);
	}
	
	public LiquibaseElement getDatabaseChangeLog() {
		return databaseChangeLog;
	}
}
