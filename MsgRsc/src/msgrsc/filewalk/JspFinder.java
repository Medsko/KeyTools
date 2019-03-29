package msgrsc.filewalk;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class JspFinder extends AxonFileWalker {

	private final static Pattern JSP_FILE_PATTERN = Pattern.compile(".*\\.jspf?");
	
	private final static String JSP_FILE_PATTERN_STRING = ".*\\.jspf?";
	
	private long nrOfJsps;
	
	private List<String> currentDirJsps;
	
	public JspFinder() {
		directoriesToSkip.add("xsd");
		directoriesToSkip.add("Reference");
		directoriesToSkip.add("test");
		directoriesToSkip.add("Scripts");
		nrOfJsps = 0;
	}
	
	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
		
		
		if (file.getFileName().toString().matches(JSP_FILE_PATTERN_STRING)) {
			nrOfJsps++;
			if (currentDirJsps == null) {
				currentDirJsps = new ArrayList<>();
			}
			currentDirJsps.add(file.getFileName().toString());
		}
		
		return FileVisitResult.CONTINUE;
	}
	
	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException ioex) {
		
		if (currentDirJsps != null) {
			logger.log("Jsps found in " + dir.toString() + ": ");
			for (String file : currentDirJsps) {
				logger.log(file);
			}
			logger.log("----------------------------------------------");
			currentDirJsps = null;
		}
		
		return FileVisitResult.CONTINUE;
	}


	public long getNrOfJsps() {
		return nrOfJsps;
	}
}
