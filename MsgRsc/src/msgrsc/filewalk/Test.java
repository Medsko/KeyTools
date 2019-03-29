package msgrsc.filewalk;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import msgrsc.dao.MsgRscDir;
import msgrsc.dao.MsgRscFile;
import msgrsc.fixes.MRSpecialCharacterFinder;
import msgrsc.fixes.MRSpecialCharacterRewriter;
import msgrsc.utils.TranslationToolHelper;

public class Test {

	public static void main(String[] args) throws IOException {
		
//		testMsgRscFinder();
			
//		testJspFinder();
		
//		testSpecialCharacterFinder();
		
//		testSpecialCharacterRewriter();
		
//		System.out.println(Charset.defaultCharset());
//		testOpenFileCharset();
	}
	
	private static void testOpenFileCharset() {
		String msgFile = "C:/src/QSD-50062/QISDamageClaimAdministration/source/com/quinity/qfa/damageclaimadministration/addclaimviaform/MessageResources_fr.properties";
		Path mrPath = Paths.get(msgFile);
		Path rewritePath = Paths.get("C:/MsgRscImport/rewrite.properties");
		BufferedWriter writer = null;
		try {
			List<String> allLines = Files.readAllLines(mrPath, StandardCharsets.ISO_8859_1);
			
			for (int i=50; i<60; i++) {
				System.out.println("ISO Latin Alphabet No encoding: " + allLines.get(i));
			}
			
			System.out.println("Successfully read all lines in the source file!");
			
			TranslationToolHelper helper = new TranslationToolHelper();
			
			writer = Files.newBufferedWriter(rewritePath);			
			for (String line : allLines) {
				writer.write(helper.replaceSpecialCharactersByCodes(line));
				writer.newLine();
			}
			System.out.println("Successfully written all lines to target file!");

			allLines = Files.readAllLines(rewritePath, StandardCharsets.UTF_8);
			
			System.out.println("Successfully read all lines from rewritten file!");
			
			for (int i=50; i<60; i++) {
				System.out.println("Read line with UTF-8 encoding after rewrite: " + allLines.get(i));
			}

			
		} catch (IOException ioex) {
			ioex.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private static void testSpecialCharacterRewriter() {
		MRSpecialCharacterRewriter rewriter = new MRSpecialCharacterRewriter();
		String start = "C:/src/QSD-50535";
		rewriter.findAndRewriteSpecialCharacters(start, true);
	}
	
	
	private static void testSpecialCharacterFinder() throws IOException {
		MRSpecialCharacterFinder finder = new MRSpecialCharacterFinder();
//		Path start = Paths.get("C:/src/QSD-50535/QuinityFormsAdministration");
		// HEAVY LOAD ALERT: full search of Axon.
		Path start = Paths.get("C:/src/QSD-50062");
		
		Files.walkFileTree(start, finder);
		
		for (MsgRscFile file : finder.getFilesContainingSpecialCharacters()) {
			System.out.println("Messages with special characters in file " + file.getFullPath() + ":");
			for (String messageKey : file.getMessagesWithSpecialChars()) {
				System.out.println("Message key: " + messageKey);
			}
			System.out.println("-------------------------------------------");
		}
	}
	
	private static void testJspFinder() throws IOException {
		JspFinder finder = new JspFinder();
		Path start = Paths.get("C:/src/CURRENT_BUGFIX_CONTRACTS");

		Files.walkFileTree(start, finder);
		
		System.out.println("Number of jsp(f) files: " + finder.getNrOfJsps());

	}
	
	private static void testMsgRscFinder() throws IOException {
		MsgRscFileFinder finder = new MsgRscFileFinder();
		
		Path start = Paths.get("C:/src/QSD-49141_aka_BEL-CTR-P150/CDSView/source/com/keylane/cd/dunningprocess/view");
		// C:\src\QSD-49141_aka_BEL-CTR-P150\CDSView\source\com\keylane\cd\dunningprocess\view\MessageResources_en.properties
		Files.walkFileTree(start, finder);
		
		for (MsgRscDir dir : finder.getMsgRscDirectories()) { 
			System.out.println(dir);
			System.out.println("-------------------------------------------------");
		}
		System.out.println("Total number of directories found: " + finder.getMsgRscDirectories().size());
	}
	
}
