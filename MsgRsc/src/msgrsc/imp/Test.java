package msgrsc.imp;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import msgrsc.utils.StringUtil;

public class Test {

	
	public static void main(String[] args) {
		
//		repeatedTestMrImporter();
		
//		testMrImporter();
		
		testDbTranslationsImporter();
	}
	
	private static void testDbTranslationsImporter() {
		String bugNumber = "QSD-52754";
		String aggregateDir = "C:\\src\\QSD-49141_aka_BEL-CTR-P150"; 
		MsgRscExcelReader excelReader = new MsgRscExcelReader(StringUtil.toBareBugNumber(bugNumber));
		try {
			Path dir = Paths.get("C:\\MsgRscImport").resolve(bugNumber);
			DirectoryStream<Path> ds = Files.newDirectoryStream(dir, "*.xlsx");
			for (Path excelFile : ds) {
				if (!excelReader.buildFromFile(excelFile.toString())) {
					p("excelReader failed!");
					return;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		} 
		DbTranslationImporter importer = new DbTranslationImporter(bugNumber, excelReader.getDbTranslations());
		
		if (!importer.importDbTranslations(aggregateDir)) {
			p("DbTranslationImporter failed!");
		}
	}
	
	private static void repeatedTestMrImporter() {
		// Debug-mode-deactivated importer.
		MrImporter importer = new MrImporter(false);
		
		String bugNumber = "QSD-52754";
		String aggregateDir = "C:\\src\\QSD-49141_aka_BEL-CTR-P150";
		
		if (!importer.importMR(bugNumber, aggregateDir)) {
			System.out.println("Fatal error!");
		} else {
			System.out.println("Fatal success!");
		}
	}
	
	private static void testMrImporter() {
		
		Scanner scanner = new Scanner(System.in);
		
		p("Welcome! For what bug number are we going to import translations? "
				+ "(please use full format, e.g. 'QSD-12345')");
		String bugNumber = scanner.nextLine();
		
		p("Please enter the full path for the aggregate directory of the "
				+ "project you want to import into: ");
		
		String aggregateDir = scanner.nextLine();
		
		
		p("Have you been a good boy/girl? I.e. have you set placeholders with "
				+ "the given bug number for all requested translations?");
		String placeholdersSet = scanner.nextLine();
		
		p("Thanks! One last question: do you trust me?");
		
		String answer = scanner.nextLine();
		scanner.close();
		
		boolean isDebug = true;
		if (answer.equalsIgnoreCase("yes")
				|| answer.equalsIgnoreCase("fo shizzle")) {
			isDebug = false;
		}
		
		// Debug-mode-activated importer.
		MrImporter importer = new MrImporter(isDebug);
		
//		String bugNumber = "QSD-52224";
//		String aggregateDir = "C:\\src\\QSD-36385";
		
		if (!placeholdersSet.startsWith("y") && !placeholdersSet.equals("fo shizzle")) {
			importer.setFindByMessageKey(true);
		}
		
		if (!importer.importMR(bugNumber, aggregateDir)) {
			System.out.println("Fatal error!");
		} else {
			System.out.println("Fatal success!");
		}
		
	}
	
	private static void p(Object obj) {
		System.out.println(obj);
	}
}
