package msgrsc.filewalk;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import msgrsc.dao.MsgRscDir;
import msgrsc.dao.MsgRscFile;

public class MsgRscFileFinder extends AxonFileWalker {
	
	protected final static Pattern INF_AND_MSG_FILE_PATTERN = Pattern.compile(".*MessageResources_.{2,3}\\.properties");
	
	protected final static String MSG_FILE_PATTERN = "MessageResources_.{2,3}\\.properties";
	
	protected final static String INFO_FILE_PATTERN = "InformationMessageResources_.{2,3}\\.properties";
	
	protected List<MsgRscFile> msgRscFilesCurrentDir;
	
	protected List<MsgRscFile> infoFilesCurrentDir;
	
	protected List<MsgRscDir> msgRscDirectories;
	
	public MsgRscFileFinder() {
		msgRscDirectories = new ArrayList<>();
		directoriesToSkip.add("xsd");
		directoriesToSkip.add("Reference");
		directoriesToSkip.add("test");
		directoriesToSkip.add("Scripts");
	}
	
	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
		
		if (file.getFileName().toString().matches(MSG_FILE_PATTERN)) {
			// Found a message resource file.
			MsgRscFile mrFile = new MsgRscFile();
			mrFile.setFullPath(file.toString());
			
			if (!processMRFile(mrFile)) {
				// Skip this file.
				return FileVisitResult.CONTINUE;
			}
			
			if (mrFile.getLanguage() != null) {
				// The file contains translations for one of the supported languages.
				if (msgRscFilesCurrentDir == null) {
					msgRscFilesCurrentDir = new ArrayList<>();
				}
				msgRscFilesCurrentDir.add(mrFile);
			}
		}
		
		if (file.getFileName().toString().matches(INFO_FILE_PATTERN)) {
			MsgRscFile mrFile = new MsgRscFile();
			mrFile.setFullPath(file.toString());
			
			if (!processMRFile(mrFile)) {
				return FileVisitResult.CONTINUE;
			}
						
			if (mrFile.getLanguage() != null) {
				// The file contains translations for one of the supported languages.
				if (infoFilesCurrentDir == null) {
					infoFilesCurrentDir = new ArrayList<>();
				}
				infoFilesCurrentDir.add(mrFile);
			}
		}
		
		return FileVisitResult.CONTINUE;
	}
	
	/**
	 * Hook method to allow for processing of a {@link MsgRscFile}. By returning {@code false}, the 
	 * file will be exempt from generic processing in {@link #visitFile(Path, BasicFileAttributes)}.
	 * Therefore, if any logging is desired, the implementing subclass should provide it. 
	 */
	protected boolean processMRFile(MsgRscFile mrFile) {
		return true;
	}
	
	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException ioex) {
		MsgRscDir directory = null;
		
		if (msgRscFilesCurrentDir != null) {
			directory = new MsgRscDir();
			// The 'dir' parameter might be a directory that is a child of the directory holding
			// message resources. Therefore, take the parent of one of the message resource files
			// and set it as the parent directory.
			Path dirPath = Paths.get(msgRscFilesCurrentDir.get(0).getFullPath()).getParent();
			directory.setPath(dirPath.toString());
			directory.addMsgRscFiles(msgRscFilesCurrentDir);
		}
		
		if (infoFilesCurrentDir != null) {
			if (directory == null) {
				directory = new MsgRscDir();
				// The 'dir' parameter might be a directory that is a child of the directory holding
				// message resources. Therefore, take the parent of one of the message resource files
				// and set it as the parent directory.
				Path dirPath = Paths.get(infoFilesCurrentDir.get(0).getFullPath()).getParent();
				directory.setPath(dirPath.toString());
			}
			directory.addInfoFiles(infoFilesCurrentDir);
		}
		
		if (directory != null) {
			msgRscDirectories.add(directory);
			msgRscFilesCurrentDir = null;
			infoFilesCurrentDir = null;
		}
		
		return FileVisitResult.CONTINUE;
	}

	public List<MsgRscDir> getMsgRscDirectories() {
		return msgRscDirectories;
	}
}
