package msgrsc.request;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import msgrsc.imp.MrImporter;
import msgrsc.utils.Fallible;
import msgrsc.utils.Language;

/**
 * Writes a {@link TranslationRequest} to a file that can be used to easily add
 * the requested messages to the table in Confluence.
 */
public class TranslationRequestFileWriter implements Fallible {

	private TranslationRequest translationRequest;
	
	private boolean includeColumnHeaders;
	
	public TranslationRequestFileWriter(TranslationRequest translationRequest) {
		this.translationRequest = translationRequest;
	}
	
	/**
	 * Please respect this inner class. All the other classes already make fun of it.
	 */
	private class CsvStringBuilder {
		
		private StringBuilder builder;
		
		CsvStringBuilder(String firstValue) {
			builder = new StringBuilder(firstValue);
		}
		
//		void circumventCsvAppend(String text) {
//			builder.append(text);
//		}
		
		void append(String text) {
			builder.append(";");
			builder.append(text);
		}
		
		@Override
		public String toString() {
			return builder.toString();
		}
	}
	
	/**
	 * Writes the {@link #translationRequest} to a CSV file.
	 */
	public boolean writeToCsvFile() {
		
		if (translationRequest.getCountRequestedTranslations() <= 0) {
			// No messages have a 'toBeTranslated'-tag, so no translations
			// have to be requested.
			informer.informUser("No requested translations found for bug "
					+ "number: " + translationRequest.getBugNumber());
			return true;
		}
		
		String bugNumber = translationRequest.getBugNumber();
		// Construct the path for the request file.
		String directoryName = MrImporter.IMPORT_DIRECTORY
				+ File.separator + bugNumber;
		Path directory = Paths.get(directoryName);
		
		if (!Files.isDirectory(directory)) {
			try {
				Files.createDirectories(directory);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		
		String fileName = "requestedTranslationsFor" + bugNumber + ".csv";		
		Path file = directory.resolve(fileName);
		
		// Use ISO_8859_1 encoding, so Excel can interpret the un-escaped special characters. 
		try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.ISO_8859_1)) {
			if (includeColumnHeaders) {
				// We should include column headers in the output file.
				String firstLine = composeFirstLine();
				// Write the first line to file.
				writer.write(firstLine);
				writer.newLine();
			}
			
			for (String messageKey : translationRequest.getRequestedKeys()) {
				String line = composeLine(messageKey);
				writer.write(line);
				// Don't forget that line break!
				writer.newLine();
			}
		} catch (IOException ioex) {
			ioex.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	
	private String composeFirstLine() {
		StringBuilder lineBuilder = new StringBuilder();
		lineBuilder.append("QSD number");
		lineBuilder.append(";Message resource key");
		lineBuilder.append(";Original text EN");
		lineBuilder.append(";Original text NL");
		
		for (Language language : Language.foreignLanguages()) {
			lineBuilder.append(";New text ");
			lineBuilder.append(language.code);
		}
		
		return lineBuilder.toString();
	}
	
	
	private String composeLine(String messageKey) {
		// First column contains the bug number.
		CsvStringBuilder lineBuilder = new CsvStringBuilder(translationRequest.getBugNumber());
		// Second column contains the message key.
		lineBuilder.append(messageKey);
		// Third column contains the English message.
		String englishMessage = determineOriginalMessage(translationRequest.getEnglishMessage(messageKey));
		lineBuilder.append(englishMessage);
		// Fourth column contains the Dutch message.
		String dutchMessage = determineOriginalMessage(translationRequest.getDutchMessage(messageKey));
		lineBuilder.append(dutchMessage);
		
		boolean[] required = translationRequest.getRequestedTranlations(messageKey);
		// Other columns are for translations per language. Each is filled with either:
		for (int i=0; i<required.length; i++) {
			if (required[i]) {
				// 1) an empty String to indicate the translation is required;
				lineBuilder.append("");
			} else {
				// 2) an 'x' to indicate the translation is not needed.
				lineBuilder.append("x");
			}
		}
		
		return lineBuilder.toString();
	}
	
	private String determineOriginalMessage(String originalMessageFromFile) {
		if (originalMessageFromFile == null || originalMessageFromFile.equals("")) {
			return "!!!orignal text missing !!!";
		}
		return originalMessageFromFile;
	}
	
	public void setIncludeColumnHeaders(boolean includeColumnHeaders) {
		this.includeColumnHeaders = includeColumnHeaders;
	}
}
