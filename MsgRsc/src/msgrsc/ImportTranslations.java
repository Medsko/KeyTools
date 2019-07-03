package msgrsc;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import msgrsc.craplog.Fallible;
import msgrsc.imp.DbTranslationImporter;
import msgrsc.imp.MrImporter;
import msgrsc.imp.MsgRscExcelReader;
import msgrsc.utils.IOUtils;
import msgrsc.utils.StringUtil;

/**
 * Imports the translations provided by translation bureaus or faithful
 * colleagues (lobi Rune). 
 * <p>
 * ATTENTION: before running this, make sure you put the Excel files with the
 * requested translations in a folder marked with the QSD number of the
 * translation sub-task in the C:\MsgRsc directory (e.g. in 'C:\MsgRsc\QSD-12345').
 * Also, make sure placeholders with this bug number were placed, either by
 * yourself or by running {@link RequestTranslations}.
 * <p>
 * Arguments for this executable:
 * <ol>
 * 	<li>1. bug number - the QSD of the translation sub-task (e.g. 'QSD-12345').
 * 	</li>
 * 	<li>2. aggregate directory - the aggregate directory of the project that 
 * 		will be scanned for missing message resources (e.g. 'C:\src\BUGFIX').
 * 		Hint: if path contains spaces, wrap this argument in quotes.
 * 	</li>
 * </ol>
 */
public class ImportTranslations implements Fallible {

	public static void main(String[] args) {
		// Check and process input.
		if (args.length < 2) {
			log.error("Please run with arguments 1) bug number and 2) aggregate directory");
			return;
		}
		
		String bugNumber = args[0];
		String aggregateDir = args[1];

		// Import message resources.
		MrImporter importer = new MrImporter(false);
		
		if (!importer.importMR(bugNumber, aggregateDir)) {
			System.out.println("Failed to import message resources!");
		} else {
			System.out.println("Successfully imported message resources!");
		}
		
		// Import database translations.
		MsgRscExcelReader excelReader = new MsgRscExcelReader(StringUtil.toBareBugNumber(bugNumber));
		try {
			Path dir = Paths.get(IOUtils.MR_IMPORT_PATH).resolve(bugNumber);
			DirectoryStream<Path> ds = Files.newDirectoryStream(dir, "*.xlsx");
			for (Path excelFile : ds) {
				if (!excelReader.buildFromFile(excelFile.toString())) {
					log.error("excelReader failed!");
					return;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		} 
		
		DbTranslationImporter dbImporter = new DbTranslationImporter(bugNumber, excelReader.getDbTranslations());
		
		if (!dbImporter.importDbTranslations(aggregateDir)) {
			log.error("DbTranslationImporter failed!");
		} else {
			log.debug("Successfully imported database translations! "
					+ "(but check the Liquibase changelog for your RFC/bug "
					+ "just to make sure...)");
		}
	}
	
}
