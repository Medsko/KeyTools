package msgrsc.find;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import msgrsc.craplog.Logger;
import msgrsc.craplog.LoggerFactory;

public class AxonFileWalker extends SimpleFileVisitor<Path> {

	protected Logger logger = LoggerFactory.getLogger();
	
	protected List<String> directoriesToSkip;
	
	public AxonFileWalker() {
		directoriesToSkip = new ArrayList<>();
		// Always skip the directory that contains the compiled sources.
		directoriesToSkip.add("target");
	}
	
	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attr) {
		
		for (String skipDir : directoriesToSkip) {
			if (dir.getFileName().toString().contains(skipDir)) {
				return FileVisitResult.SKIP_SUBTREE;
			}
		}
		return FileVisitResult.CONTINUE;
	}
	
	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
		
		return FileVisitResult.CONTINUE;
	}
	
	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException ioex) {
		
		return FileVisitResult.CONTINUE;
	}
	
	@Override
	public FileVisitResult visitFileFailed(Path dir, IOException ioex) {
		
		return FileVisitResult.CONTINUE;
	}
	
	
}
