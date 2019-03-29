package msgrsc.fixes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import msgrsc.dao.MsgRscDir;
import msgrsc.dao.MsgRscFile;
import msgrsc.filewalk.MsgRscFileFinder;
import msgrsc.utils.StringUtil;
import msgrsc.utils.TranslationToolHelper;

public class MRSpecialCharacterFinder extends MsgRscFileFinder {

	private TranslationToolHelper specialHelper;
	
	public MRSpecialCharacterFinder() {
		specialHelper = new TranslationToolHelper();
	}

	protected boolean processMRFile(MsgRscFile mrFile) {
		
		try (BufferedReader reader = new BufferedReader(new FileReader(mrFile.getFullPath()))) {
			
			String line;
			while ((line = reader.readLine()) != null) {
				
				if (StringUtil.isEmpty(line)) {
					continue;
				}
				
				String message = line.substring(line.indexOf("=") + 1);
				if (specialHelper.containsSpecialCharacters(message)) {
					String messageKey = line.substring(0, line.indexOf("="));
					mrFile.addMessageWithSpecialChars(messageKey);
				}
			}
			
		} catch (IOException ioex) {
			ioex.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public List<MsgRscFile> getFilesContainingSpecialCharacters() {
		List<MsgRscFile> filesWithSpecialChars = new ArrayList<>();
		for (MsgRscDir dir : msgRscDirectories) {
			for (MsgRscFile file : dir.getMsgRscFiles()) {
				if (file.containsMessagesWithSpecialChars()) {
					filesWithSpecialChars.add(file);
				}
			}
		}
		return filesWithSpecialChars;
	}
	
}
