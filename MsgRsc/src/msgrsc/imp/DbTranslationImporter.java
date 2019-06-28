package msgrsc.imp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import msgrsc.craplog.Fallible;
import msgrsc.dao.DbDir;
import msgrsc.dao.DbFile;
import msgrsc.dao.DbTranslation;
import msgrsc.dao.LiquibaseElement;
import msgrsc.io.LiquibaseFileReader;
import msgrsc.request.DbScanner;
import msgrsc.utils.Language;
import msgrsc.utils.StringUtil;
import msgrsc.utils.XmlUtils;

public class DbTranslationImporter implements Fallible {
	
	private List<DbTranslation> translations;
	
	private String bugNumber;
	
	private LiquibaseFileReader reader;
	
	private List<String> successFullyUpdatedFiles;
		
	public DbTranslationImporter(String bugNumber, List<DbTranslation> translations) {
		this.translations = translations;
		this.bugNumber = bugNumber;
		reader = new LiquibaseFileReader();
	}
	
	public boolean importDbTranslations(String aggregateDir) {
		successFullyUpdatedFiles = new ArrayList<>();
		// Find all Liquibase scripts which mention the bug number. 
		DbScanner scanner = new DbScanner(bugNumber);
		scanner.scan(aggregateDir);
		List<DbDir> dirsContainingBugNumber = scanner.getDirectoriesToScan();
		// Try to rewrite all files found.
		for (DbDir dir : dirsContainingBugNumber) {
			for (DbFile file : dir.getFiles()) {
				// For each file:
				// 1) Construct a LiquibaseElement from the file contents.
				if (!reader.readFile(file.getFullPath())) {
					logger.log("importDbTranslations - failed to parse file " + file.getFullPath());
					return false;
				}
				// 2) find matches for provided translation by English text.
				List<LiquibaseElement> translatedTags = findTranslatedTags(reader.getDatabaseChangeLog());
				
				// 3) rewrite the file.
				if (!rewriteFile(file.getFullPath(), translatedTags)) {
					logger.log("Failed while rewriting file: " + file.getFullPath());
				}
			}
		}		
		// Replace original files with updated temporary files.
		for (String updatedFile : successFullyUpdatedFiles) {
			// Determine the full path of the temporary file.
			String tempFileName = StringUtil.determineTempFileName(updatedFile);
			Path tempFile = Paths.get(updatedFile).getParent().resolve(tempFileName);
			// Replace the original with the modified file.
			try {
				Files.move(tempFile, Paths.get(updatedFile), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException ioex) {
				ioex.printStackTrace();
				return false;
			}
		}
		
		return true;
	}
	
	// Returns a list of LiquibaseElements which translation value has been updated
	private List<LiquibaseElement> findTranslatedTags(LiquibaseElement changelog) {
		List<LiquibaseElement> translatedTags = new ArrayList<>();
		// For each DbTranslation:
		for (DbTranslation translation : translations) {
			// 1) find a column tag which value matches the Dutch or English translation.
			List<LiquibaseElement> matches = changelog.getChildrenBy((element)-> {
				return element.getTag().equals("ext:translation")
						&& translation.getTextEnglish().equals(element.getValue());
			});
			
			for (LiquibaseElement match : matches) {
				// 2) get the parent of that element.
				LiquibaseElement translationTag = match.getParent();
				for (Language language : Language.foreignLanguages()) {
					// 3) for each of the foreign languages, get the translation from the 
					// DbTranslation and set it on the corresponding LiquibaseElement.
					LiquibaseElement updatedTag = translationTag.getFirstWith((element)->
						element.getLanguageCode() != null
							&& language.code.equals(element.getLanguageCode().toUpperCase())
					);
					updatedTag.setValue(translation.getTranslation(language));
					// Add the updated element to the list.
					translatedTags.add(updatedTag);
				}
			}
		}
		
		// Order the list of LiquibaseElements by line number (of start of tag).
		translatedTags.sort((elm1, elm2)-> {
			return elm1.getLineTagStart().compareTo(elm2.getLineTagStart());
		});
		
		return translatedTags;
	}
		
	private boolean rewriteFile(String fullPath, List<LiquibaseElement> translatedTags) {
		// Rewrite the file to a temporary file, inserting the provided translations.
		// 1) get the next element from the list;
		// 2) copy lines to temporary file until line number of current element is reached;
		// 3) insert the translation into the line and write it to temporary file.
		String tempFileName = StringUtil.determineTempFileName(fullPath);
		Path tempFile = Paths.get(fullPath).getParent().resolve(tempFileName);
		Iterator<LiquibaseElement> it = translatedTags.iterator();
		
		if (!it.hasNext()) {
			// No matches found for this file. Something went wrong.
			logger.log("rewriteFile - empty list! No translations matched for file: " + fullPath);
			return false;
		}
		LiquibaseElement nextTranslatedTag = it.next();
		int lineNumber = 1;

		try (BufferedReader reader = new BufferedReader(new FileReader(fullPath));
				BufferedWriter writer = Files.newBufferedWriter(tempFile)) {
			
			String line;
			while ((line = reader.readLine()) != null) {
				// TODO: support for multi-line translations/tags. 
				if (nextTranslatedTag.getLineTagStart() == lineNumber) {
					// A translated tag is on this line. Update the line accordingly.
					line = XmlUtils.setAttribute(line, "value", nextTranslatedTag.getValue());
					// Get the next translated tag, if one is left.
					if (it.hasNext()) {
						nextTranslatedTag = it.next();
					}
				}
				writer.write(line);
				writer.newLine();
				lineNumber++;
			}
		} catch (IOException ioex) {
			ioex.printStackTrace();
			return false;
		}
		// Add the path of the successfully rewritten file to the list.
		successFullyUpdatedFiles.add(fullPath);
		
		return true;
	}
	
}
