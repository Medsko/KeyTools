package msgrsc;

import java.util.List;
import java.util.Map;

import msgrsc.dao.MessageResource;
import msgrsc.fitnesse.MissingDefinitionFinder;
import msgrsc.fitnesse.VariableDefinitionWriter;
import msgrsc.fitnesse.VariableDefinitionsFinder;
import msgrsc.utils.Language;

/**
 * Scans the given aggregate directory for variables in the VariableNamesInUse.wiki
 * file that are not present in (one of the) VariableDefinitions files for different
 * languages. The message resource files are then scanned for these missing 
 * resources, which are then added to the VariableDefinitions files.
 * <p>
 * Arguments for this executable:
 * <ol>
 * 	<li>1. aggregate directory - the aggregate directory of the project that 
 * 		will be scanned for missing message resources (e.g. 'C:\src\BUGFIX').
 * 		Hint: if path contains spaces, wrap this argument in quotes.
 * 	</li>
 * </ol>
 */
public class FindFitnesseVariables {

	public static void main(String[] args) {
		if (args.length < 1) {
			p("Please run with aggregate directory as argument!");
			return;
		}
		
		String aggregateDir = args[0];
		Map<Language, List<MessageResource>> newVariableDefinitions = findMissingMesRes(aggregateDir);
		
		VariableDefinitionWriter writer = new VariableDefinitionWriter(aggregateDir, newVariableDefinitions);
		// Puntje om nog te overdenken: de Map<Language, List<MessageResource>> als argument aan deze methode meegeven?
		if (!writer.write()) {
			p("Failed while adding missing variable definitions!");
		} else {
			p("Successfully added missing variable definitions!");
			p("Number of variables found: " + writer.getNrOfVariablesDefined());
		}
	}
	
	private static Map<Language, List<MessageResource>> findMissingMesRes(String aggregateDir) {
		// 1) Find missing variable definitions.
		MissingDefinitionFinder missDefFinder = new MissingDefinitionFinder(aggregateDir);
		if (!missDefFinder.findMissingMesRes()) {
			p("Failed to find the missing variable definitions!");
			return null;
		}
		// 2) Find the messages for the keys found in 1). 
		VariableDefinitionsFinder updater = new VariableDefinitionsFinder(aggregateDir);
		if (!updater.findMessageResourceDefinitions(missDefFinder.getMissingResources())) {
			p("Fail!");
			return null;
		}
		return updater.getVariablesPerLanguage();
	}
	
	private static void p(Object obj) {
		System.out.println(obj);
	}
}
