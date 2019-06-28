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

public class Test {

	public static void main(String[] args) {
//		testMissingDefinitionFinder();
		
//		testVariableDefinitionsUpdater();
		
//		testAppendingFileWriter();
		
		testVariableDefinitionWriter();
	}
	
	private static void testVariableDefinitionWriter() {
		String aggregateDir = "C:\\src\\QSD-55251";
		Map<Language, List<MessageResource>> newVariableDefinitions = findMissingMesRes(aggregateDir);
		VariableDefinitionWriter writer = new VariableDefinitionWriter(aggregateDir, newVariableDefinitions);
		// Puntje om nog te overdenken: de Map<Language, List<MessageResource>> als argument aan deze methode meegeven?
		if (!writer.write()) {
			p("Fail!");
		}
	}
	
	private static void testAppendingFileWriter() {
		Path filePath = Paths.get("C:\\src\\QSD-55251");
		filePath = filePath.resolve(Paths.get(IOUtils.RELATIVE_FITNESSE_LANG_PATH));
		filePath = filePath.resolve("VariableDefinitionsVls.wiki");

		try (AppendingFileWriter writer = new AppendingFileWriter(filePath)) {
			
		} catch (IOException e) {
			e.printStackTrace();
			p("Fail!");
		}
	}
	
	private static void testVariableDefinitionsUpdater() {
		String aggregateDir = "C:\\src\\QSD-55251";
		Map<Language, List<MessageResource>> missingMesRes = findMissingMesRes(aggregateDir);
		for (Language lang : missingMesRes.keySet()) {
			p("Missing variables for language " + lang.getPrettyName());
			for (MessageResource missingVar : missingMesRes.get(lang)) {
				p(missingVar.toString());
			}
			p("");
		}
	}
	
	private static Map<Language, List<MessageResource>> findMissingMesRes(String aggregateDir) {
		MissingDefinitionFinder missDefFinder = new MissingDefinitionFinder(aggregateDir);
		if (!missDefFinder.findMissingMesRes()) {
			p("Failed to find the missing variable definitions!");
			return null;
		}

		VariableDefinitionsFinder updater = new VariableDefinitionsFinder(aggregateDir);

		if (!updater.findMessageResourceDefinitions(missDefFinder.getMissingResources())) {
			p("Fail!");
			return null;
		}
		return updater.getVariablesPerLanguage();
	}
	
	private static void testMissingDefinitionFinder() {
		String aggregateDir = "C:\\src\\QSD-55251";
		MissingDefinitionFinder finder = new MissingDefinitionFinder(aggregateDir);
		if (!finder.findMissingMesRes()) {
			p("Fail!");
			return;
		}

		for (Language lang : finder.getMissingResources().keySet()) {
			p("Missing variables for language " + lang.getPrettyName());
			for (MessageResource missingVar : finder.getMissingResources().get(lang)) {
				p(missingVar.getKey());
			}
			p("");
		}
		
	}
	
	
	private static void p(Object obj) {
		System.out.println(obj);
	}
}
