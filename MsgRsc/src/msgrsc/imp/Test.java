package msgrsc.imp;

import java.util.Scanner;

public class Test {

	
	public static void main(String[] args) {
		testMrImporter();
	}

	
	private static void testMrImporter() {
		
		Scanner scanner = new Scanner(System.in);
		
		p("Welcome! For what bug number are we going to import translations? "
				+ "(please use full format, e.g. 'QSD-12345')");
		String bugNumber = scanner.nextLine();
		
		p("Please enter the full path for the aggregate directory of the "
				+ "project you want to import into: ");
		
		String aggregateDir = scanner.nextLine();
		
		p("Thanks! One last question: do you trust me?");
		
		String answer = scanner.nextLine();
		scanner.close();
		
		boolean isDebug = false;
		if (answer.equalsIgnoreCase("yes")
				|| answer.equalsIgnoreCase("fo shizzle")) {
			isDebug = true;
		}
		
		// Debug-mode-activated importer.
		MrImporter importer = new MrImporter(isDebug);
//		String bugNumber = "QSD-52224";
//		String aggregateDir = "C:\\src\\QSD-36385";

		
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
