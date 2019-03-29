package msgrsc;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import msgrsc.imp.MrImporter;
import msgrsc.io.LineRewriter;
import msgrsc.request.TranslationRequest;
import msgrsc.request.TranslationRequestBuilder;
import msgrsc.request.TranslationRequestFileWriter;
import msgrsc.utils.StringUtil;

public class Test {

	
	public static void main(String[] args) {
				
//		testLib();
			
		testReplaceSpecialChars();
		
//		testLineRewriter();
		
//		testWriteCsvTranslationRequest();
	}
	
	private static void testWriteCsvTranslationRequest() {
		TranslationRequestBuilder builder = new TranslationRequestBuilder();
		
		String directory = "C:/src/CURRENT_BUGFIX_CONTRACTS/QuinityFormsAdministration/source/com/quinity/qfa/applicationadministration";
		if (!builder.buildByBugNumber("QSD-50000", directory)) {
			System.out.println("Error!");
			return;
		}
		
		TranslationRequest request = builder.getTranslationRequest();
		
		TranslationRequestFileWriter writer = new TranslationRequestFileWriter(request);
		// ...not necessary to include these, of course.
		writer.setIncludeColumnHeaders(true);
		
		if (!writer.writeToCsvFile()) {
			System.out.println("Error!");
			return;
		}
	}
	
	
	private static void testLineRewriter() {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter complete path for file to rewrite: ");

		String input = scanner.nextLine().replace("\\", "/");
		
		LineRewriter rewriter = new LineRewriter();
		
		if (!rewriter.rewrite(input, s -> StringUtil.replaceAllSpecialChars(s))) {
			System.out.println("Error while rewriting the file!!!");
		}
	
		scanner.close();
	}
	
	
	private static void testReplaceSpecialChars() {
		
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter a line with special characters to convert: ");

		while (true) {
			String input = scanner.nextLine();
			
			if (input.equals("exit"))
				break;
			
			String output = StringUtil.replaceAllSpecialChars(input);
			System.out.println(output);
		}
		scanner.close();
	}

	// Get to know the POI library.
	private static void testLib() {
		
		try (Workbook workbook = WorkbookFactory.create(new File("C:/Users/mvries/Desktop/Copy of FR_QSD-50736_FRENCH.xlsx"))) {
			Sheet msgRscSheet;
			if ((msgRscSheet = workbook.getSheet("Message resources")) == null) {
				msgRscSheet = workbook.getSheetAt(0);
			}
			System.out.println("Physical number of rows: " + msgRscSheet.getPhysicalNumberOfRows());
			Row topRow = msgRscSheet.getRow(0);
			System.out.println("Physical number of columns top row: " + topRow.getPhysicalNumberOfCells());
			
			DataFormatter formatter = new DataFormatter();
			
			System.out.println("Contents of first row: ");
			
			for (int i=0; i<topRow.getPhysicalNumberOfCells(); i++) {
				System.out.println("Row " + i + ": " + formatter.formatCellValue(topRow.getCell(i)));
			}

		} catch (EncryptedDocumentException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
}
