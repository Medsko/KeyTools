package msgrsc.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import msgrsc.dao.DbDir;
import msgrsc.dao.DbFile;
import msgrsc.dao.DbTranslation;
import msgrsc.dao.LiquibaseElement;
import msgrsc.request.DbScanner;
import msgrsc.request.DbTranslationBuilder;

public class Test {

	public static void main(String[] args) {
//		testLiquibaseFileReader();
		
//		testDbFileScan();
		
		testDbTranslationBuilder();
		
	}
	
	private static void testDbTranslationBuilder() {
		String testFile = "C:/src/QSD-49141_aka_BEL-CTR-P150/QISDatabase/changelogs/2019/M5/AxonMain/QSD-52659.xml";
		String testBugNumber = "QSD-52754";

		LiquibaseFileReader reader = new LiquibaseFileReader();
		if (!reader.readFile(testFile)) {
			p("scanDbDirectory - failed to read file: " 
					+ testFile);
			return;
		}
		// Get a list of the insert-/updateTranslations elements.
		List<LiquibaseElement> translations = new ArrayList<>();
		reader.getDatabaseChangeLog().getChildrenBy(translations, 
				(element)-> 
					element.getTag().equals("ext:insertTranslations")
					|| element.getTag().equals("ext:updateTranslations"));
		
		List<DbTranslation> results = new ArrayList<>();
		
		// For each insertTranslations, create a DbTranslation and add it to the list.
		DbTranslationBuilder builder = new DbTranslationBuilder(testBugNumber);
		for (LiquibaseElement insTrans : translations) {
			List<DbTranslation> dbTranslations = builder.build(insTrans);
			if (dbTranslations == null) {
				p("scanDbDirectory - failed to build translation from "
						+ "insertTranslations element!");
				return;
			}
			results.addAll(dbTranslations);
		}
		
		for (DbTranslation translation : results) {
			p(translation.getTable() + ", " + translation.getTextDutch() + ", " 
					+ translation.getTextEnglish() + ", " + Arrays.toString(translation.getRequestedFor()));
		}
	}
	
	private static void testDbFileScan() {
		String testBugNumber = "QSD-52754";
		String aggregateDir = "C:/src/QSD-49141_aka_BEL-CTR-P150";
		DbScanner scanner = new DbScanner(testBugNumber);
		if (!scanner.scan(aggregateDir)) {
			p("Oh no! Something went wrong!");
			return;
		}
		List<DbDir> dirsToScan = scanner.getDirectoriesToScan();
		
		for (DbDir dir : dirsToScan) {
			for (DbFile file : dir.getFiles()) {
				p(file.getFullPath());
			}
		}
	}
	
	private static void testLiquibaseFileReader() {
		String testFile = "C:/src/QSD-49141_aka_BEL-CTR-P150/QISDatabase/changelogs/2019/M5/AxonMain/QSD-52659.xml";
//		String testBugNumber = "QSD-52754";
		
		LiquibaseFileReader reader = new LiquibaseFileReader();
		
		if (!reader.readFile(testFile))
			p("Oh no! Something went wrong!");
		
		LiquibaseElement changeLog = reader.getDatabaseChangeLog();
		List<LiquibaseElement> translations = new ArrayList<>();
		changeLog.getChildrenBy(translations, (element)-> element.getTag().equals("ext:translation"));

		for (LiquibaseElement translation : translations) {
			StringBuilder result = new StringBuilder();
			p(translation.toString(result, 0));
		}
	}
	
	
	
	private static void p(Object obj) {
		System.out.println(obj);
	}
}
