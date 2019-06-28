package msgrsc.db;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import msgrsc.dao.LanguageTable;
import msgrsc.utils.IOUtils;
import msgrsc.utils.Language;
import msgrsc.utils.StringUtil;

public class Test {

	
	public static void main(String[] args) {
//		testLanguageFieldFinder();
//		testLanguageFieldFinderAllInclusive();
		
//		testDbTermFinder();
//		testDbTermFinderMatchup();
//		testDbTermFinderWriteToFile("aanvraag", Language.DUTCH, "policy request", Language.ENGLISH);
//		testDbTermFinderWriteToFile("aanvragen", Language.DUTCH, "policy request", Language.ENGLISH);
//		testDbTermFinderWriteToFile("groepscontract", Language.DUTCH, "collectiviteit", Language.FLEMISH);
		
//		testDbTermFinderWriteToFile("prolongatie", Language.DUTCH, "continuation", Language.ENGLISH);
	}
	
	private static void testDbTermFinderWriteToFile(String reference, Language referenceLang, String target, Language targetLang) {
		LanguageFieldFinder fieldFinder = new LanguageFieldFinder();
		fieldFinder.findLanguageTablesAllFields();
		
		DbTermFinder finder = new DbTermFinder(new DbTermQueryBuilder(referenceLang));
		finder.findTermInTables(reference, fieldFinder.getLanguageTables());

		// Now switch to target language and search all counterparts of occurrences of 
		// the reference term, collecting all entries that contain the faulty target term.
		DbTermFinder matcher = new DbTermFinder(new DbTermQueryBuilder(targetLang));
		matcher.findTermInTables(target, finder.getTablesWithHits());

		// Get ready to write results to file.
		Path outputFile = Paths.get(IOUtils.MR_PATH + "output" + StringUtil.capitalize(StringUtil.toCamelCase(target)) + ".txt");

		try (BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardOpenOption.APPEND, StandardOpenOption.CREATE)) {
			int counter = 0;
			for (LanguageTable result : matcher.getTablesWithHits()) {
				// Filter out hits with less than one language.
				result.filterHits((hit) -> {
					return hit.getContents().size() > 1;
				});
				writer.write(result.toStringWithHits());
				writer.newLine();
				p(result.toStringWithHits());
				counter++;
			}
			p("Total number of tables with incorrect term found: " + counter);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

	private static void testDbTermFinder() {

		LanguageFieldFinder fieldFinder = new LanguageFieldFinder();
		fieldFinder.findLanguageTablesAllFields();
		
		DbTermFinder finder = new DbTermFinder(new DbTermQueryBuilder(Language.FLEMISH));
		finder.findTermInTables("collectiviteit", fieldFinder.getLanguageTables());
		
		int counter = 0;
		for (LanguageTable result : finder.getTablesWithHits()) {
			p(result.toStringWithHits());
			counter++;
		}
		p("Total number of tables with hits found: " + counter);
	}
	
	private static void testLanguageFieldFinderAllInclusive() {
		// Find all language tables with field 'description'.
		LanguageFieldFinder finder = new LanguageFieldFinder();
		finder.findLanguageTablesAllFields();
		int counter = 0;
		
		for (LanguageTable result : finder.getLanguageTables()) {
			p(result);
			counter++;
		}
		p("Total number of language tables found: " + counter);
	}
	
	
	private static void testLanguageFieldFinder() {
		// Find all language tables with field 'description'.
		LanguageFieldFinder finder = new LanguageFieldFinder();
		finder.findLanguageTablesWithFields("description");
		int counter = 0;
		
		for (LanguageTable result : finder.getLanguageTables()) {
			p(result);
			counter++;
		}
		p("Total number of tables with field 'description' found: " + counter);
	}
	
	private static void p(Object obj) {
		System.out.println(obj);
	}
}
