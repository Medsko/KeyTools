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

public class Test {

	
	public static void main(String[] args) {
		
		
//		testMsgRscImport();
		
//		testLib();
		
		testRewrite();
		
	}
	
	private static void testRewrite() {
		MsgRscImporter rewriter = new MsgRscImporter();
		rewriter.convertExcelToCsv("QSD-50790", "C:/MsgRscImport");
	}
	
	private static void testMsgRscImport() {
		Scanner scanner = new Scanner(System.in);
		
//		System.out.print("Enter file to import: ");
//		String importFileString = scanner.nextLine();
//		System.out.print("Enter directory containing message resources to update: ");
//		String msgRscDirString = scanner.nextLine();
		
		String importFileString = "C:/Users/mvries/Desktop/combinedMsgRsc.csv";
		String msgRscDirString = "C:/src/QSD-45945/QuinityFormsAdministration/source/com/quinity/qfa/policyadministration/report/view";
		
		MsgRscImporter importer = new MsgRscImporter();
		importer.importMessages(importFileString, msgRscDirString);
		
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
