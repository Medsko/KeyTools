package msgrsc;

import java.util.Scanner;

public class Test {

	
	public static void main(String[] args) {
		
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
	
}
