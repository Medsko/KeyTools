package msgrsc.fitnesse;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import msgrsc.dao.MessageResource;
import msgrsc.io.AppendingFileWriter;
import msgrsc.utils.IOUtils;
import msgrsc.utils.Language;

/**
 * Writes the variable definitions that were found by a {@link VariableDefinitionsFinder} 
 * to the VariableDefinitions files for all supported languages.
 */
public class VariableDefinitionWriter {

	private String aggregateDir;
	
	private Map<Language, List<MessageResource>> newVariableDefinitions;
	
	// Keeps track of the number of variables defined by this Writer.
	private int counter;
	
	public VariableDefinitionWriter(String aggregateDir, 
			Map<Language, List<MessageResource>> newVariableDefinitions) {
		this.aggregateDir = aggregateDir;
		this.newVariableDefinitions = newVariableDefinitions;
		counter = 0;
	}
	
	public boolean write() {
		// For each language in the map, append the found variable definitions.
		for (Language language : newVariableDefinitions.keySet()) {
			// Determine the file path.
			Path filePath = Paths.get(aggregateDir);
			filePath = filePath.resolve(Paths.get(IOUtils.RELATIVE_FITNESSE_LANG_PATH));
			filePath = filePath.resolve("VariableDefinitions" + language.getPrettyCode() 
				+ ".wiki");
			
			// Open the file to write to it.
			try (AppendingFileWriter writer = new AppendingFileWriter(filePath)) {
				// Append each newly determined variable definition to the file.
				for (MessageResource mesRes : newVariableDefinitions.get(language)) {
					String message = mesRes.getTranslation(language);
					if (message == null) {
						// TODO: hier zou je dus supermooi kunnen bijhouden welke variableNamesInUse helemaal niet meer bestaan in Axon mesres...
						continue;
					}
					String lineToAppend = "!define mr_" + mesRes.getKey();
					lineToAppend += " {!-" + message + "-!}";
					// Write the resulting line to file.
					writer.writeLine(lineToAppend);
					counter++;
				}
			} catch (IOException ioex) {
				ioex.printStackTrace();
				return false;
			}
		}
		
		return true;
	}

	public int getNrOfVariablesDefined() {
		return counter;
	}
	
}
