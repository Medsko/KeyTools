package msgrsc.request;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import msgrsc.craplog.Fallible;
import msgrsc.dao.DbDir;
import msgrsc.dao.DbFile;
import msgrsc.utils.StringUtil;

/**
 * Scans for requested translations in Liquibase XML files.
 * Only the files for the given number of months in the past are scanned. 
 */
public class DbScanner implements Fallible {
	
	private String bareBugNumber;
	
	private String baseDbFilePath;
	
	private List<DbDir> directoriesToScan;
	
	public DbScanner(String bugNumber) {
		bareBugNumber = StringUtil.toBareBugNumber(bugNumber);
	}
	
	public boolean scan(String aggregateDir, int nrOfMonthsInThePast) {
		
		baseDbFilePath = aggregateDir + File.separator 
				+ "QISDatabase" + File.separator + "changelogs";
		directoriesToScan = new ArrayList<>();
		determineDirectoriesToScan(nrOfMonthsInThePast);
		
		if (directoriesToScan.size() == 0) {
			// No files to scan. Something probably went wrong. 
			logger.log("No liquibase script files were found to scan!");
			return false;
		}
		
		return true;
	}
		
	private void determineDirectoriesToScan(int nrOfMonthsInThePast) {
		// Scan the directories for this month, as many months ahead as there
		// are files present and the given number of months in the past.
		LocalDate today = LocalDate.now();
		LocalDate iterDate = today.minusMonths(nrOfMonthsInThePast);

		Path dbDirectory;
		while ((dbDirectory = determineDirectoryFromYearAndMonth(iterDate)) != null) {
			
			List<DbFile> filesToScan = determineFilesToScan(dbDirectory);
			
			if (filesToScan.size() > 0) {
				// There are files in this directory that we might like to scan.
				DbDir dir = new DbDir();
				dir.setFullPath(dbDirectory.toString());
				dir.addFiles(filesToScan);
				directoriesToScan.add(dir);
			}
			// Update the date iterator.
			iterDate = iterDate.plusMonths(1);
		}
	}
	
	private Path determineDirectoryFromYearAndMonth(LocalDate date) {
		Path dir = Paths.get(baseDbFilePath);
		dir = dir.resolve(Paths.get(Integer.toString(date.getYear())));
		dir = dir.resolve(Paths.get("M" + Integer.toString(date.getMonthValue())));
		
		if ((date.getYear() == 2019 && date.getMonthValue() >= 5) 
				|| date.getYear() > 2019) {
			// This means we have two child directories in this directory, AxonMain
			// and AxonRisk. For now, we are only interested in AxonMain.
			dir = dir.resolve(Paths.get("AxonMain"));
		}
		
		if (Files.isDirectory(dir))
			return dir;
		else
			return null;
	}
	
	private List<DbFile> determineFilesToScan(Path directory) {
		List<DbFile> filesToScan = new ArrayList<>();
		try (DirectoryStream<Path> ds = Files.newDirectoryStream(directory)) {
			for (Path file : ds) {
				if (file.getFileName().toString().equals("changelog.xml")) {
					// Skip the governing change log files.
					continue;
				}
				
				if (containsBugNumber(file.toString())) {
					DbFile dbFile = new DbFile();
					dbFile.setFullPath(file.toString());
					filesToScan.add(dbFile);
				}
			}
			
		} catch (IOException ioex) {
			ioex.printStackTrace();
			return null;
		}
		
		return filesToScan;
	}
	
	private boolean containsBugNumber(String file) {
		List<String> lines = null;
		try {
			lines = Files.readAllLines(Paths.get(file));				
		} catch (IOException ioex) {
			ioex.printStackTrace();
			return false;
		}

		return lines != null && lines.stream()
				.anyMatch((line)-> line.contains(bareBugNumber));
	}

	/**
	 * Default implementation of {@link #scan()}, which scans for three 
	 * months in the past. 
	 */
	public boolean scan(String aggregateDir) {
		return scan(aggregateDir, 3);
	}

	public List<DbDir> getDirectoriesToScan() {
		return directoriesToScan;
	}
}
