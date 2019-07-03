package msgrsc.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import msgrsc.dao.MsgRscDir;
import msgrsc.find.MsgRscFileFinder;

public class IOUtils {

	public final static String MR_PATH = "C:\\MsgRsc\\";
	
	public final static String MR_IMPORT_PATH = "C:\\MsgRsc\\";
	
	public final static String RELATIVE_FITNESSE_LANG_PATH = "KeylaneFitNesse\\wiki\\FitNesseRoot\\Keylane\\Lang";
	
	/**
	 * Collects all lines for which the specified condition evaluates to {@code true} from
	 * the given file.
	 * @return a list of all lines that pass the given condition, or null if something went wrong. 
	 */
	public static List<String> collectLines(String file, Predicate<String> condition) {
		
		List<String> lines = new ArrayList<>();
		
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (condition.test(line)) {
					// This line tests positive for whatever affliction the 
					// caller has specified. Collect it.
					lines.add(line);
				}
			}
		} catch (IOException ioex) {
			ioex.printStackTrace();
			return null;
		}
		
		return lines;
	}
	
	/**
	 * Scans the given aggregate directory for message resource files and returns
	 * a list containing a {@link MsgRscDir} object for each directory. 
	 */
	public static List<MsgRscDir> findMesResDirs(String aggregateDir) {
		// Find all directories containing message resource files.
		Path start = Paths.get(aggregateDir);
		MsgRscFileFinder finder = new MsgRscFileFinder();
		try {
			Files.walkFileTree(start, finder);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return finder.getMsgRscDirectories();
	}

	
}
