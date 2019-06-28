package msgrsc.fitnesse;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import msgrsc.craplog.Fallible;
import msgrsc.dao.MessageResource;
import msgrsc.utils.IOUtils;
import msgrsc.utils.Language;

/**
 * Determines what variables are declared in VariableNamesInUse.wiki, but not in
 * the VariableDefinitions[Language].wiki files. The results are stored in a
 * {@link Map}, with a list of missing language resources per language.
 */
public class MissingDefinitionFinder implements Fallible {

	private Map<Language, List<MessageResource>> missingResources;
	
	private String aggregateDir;
	
	private List<String> variableNamesInUse;
	
	private List<String> variableDefinitions;
	
	public MissingDefinitionFinder(String aggregateDir) {
		this.aggregateDir = aggregateDir;
		missingResources = new HashMap<>();
	}
	
	public boolean findMissingMesRes() {
		// Scan the VariableNamesInUse.wiki, collecting all message keys.
		Path filePath = Paths.get(aggregateDir);
		filePath = filePath.resolve(Paths.get(IOUtils.RELATIVE_FITNESSE_LANG_PATH));
		filePath = filePath.resolve("VariableNamesInUse.wiki");
		String file = filePath.toString();
		
		// Select all lines that start with "mr_" from the VariableNamesInUse.wiki file.
		variableNamesInUse = IOUtils.collectLines(file, line -> line.startsWith("mr_"));
		if (variableNamesInUse == null) {
			logger.error("Failed to read variable names in use from file: " + file);
			return false;
		}
		
		for (Language language : Language.values()) {
			// For some reason, no variable definitions have been included for Norsk Bokmal yet.
			if (language == Language.NORWEGIAN) {
				continue;
			}
			findMissingMesRes(language);
		}
		
		return true;
	}
	
	private boolean findMissingMesRes(Language language) {
		// Collect all lines containing message resource definitions from the
		// file for this language.
		String fileName = "VariableDefinitions" + language.getPrettyCode() + ".wiki";
		String file = determineFilePath(fileName);
		variableDefinitions = IOUtils.collectLines(file, line -> line.contains("mr_"));
		if (variableDefinitions == null) {
			logger.error("Failed to read variable definitions from file: " + file);
			return false;
		}

		List<MessageResource> missingVariables = new ArrayList<>();
		// Now try to find a match for each Variable name in use.
		for (String variableNameInUse : variableNamesInUse) {
			variableNameInUse = variableNameInUse.trim();
			if (!isDefined(variableNameInUse)) {
				// This variable name is missing. Add it to the list.
				String key = variableNameInUse.replace("mr_", "");
				MessageResource mesRes = new MessageResource(key);
				missingVariables.add(mesRes);
			}
		}
		// If we found missing variables, add them to the map.
		// TODO: replace this with MessageResourceTable when it is available.
		if (missingVariables.size() > 0) {
			missingResources.put(language, missingVariables);
		}
		
		return true;
	}
	
	/**
	 * Checks whether the given variable name is present in the VariableDefinitions
	 * file for the language we are currently scanning for.
	 *  
	 * @param variableNameInUse - the name of the variable to search for.
	 * @return {@code true} if the given variable is defined for this language.
	 */
	private boolean isDefined(String variableNameInUse) {
		for (String variableDefinition : variableDefinitions) {
			if (variableDefinition.contains(variableNameInUse)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Determines the complete file path for the wiki file with the specified file name. 
	 */
	private String determineFilePath(String fileName) {
		Path filePath = Paths.get(aggregateDir);
		filePath = filePath.resolve(Paths.get(IOUtils.RELATIVE_FITNESSE_LANG_PATH));
		filePath = filePath.resolve(fileName);
		return filePath.toString();
	}

	public Map<Language, List<MessageResource>> getMissingResources() {
		return missingResources;
	}
}
