package msgrsc.fixes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import msgrsc.dao.MsgRscFile;
import msgrsc.io.LineRewriter;
import msgrsc.utils.Fallible;
import msgrsc.utils.StringUtil;

public class MRSpecialCharacterRewriter implements Fallible {

	private MRSpecialCharacterFinder finder;
	
	public MRSpecialCharacterRewriter() {
		finder = new MRSpecialCharacterFinder();
	}
	
	/**
	 * 
	 * @param start - the directory to start the search in. Typically, this would
	 * be a branch name (e.g. QSD-12345).   
	 */
	public boolean findAndRewriteSpecialCharacters(String start, boolean replaceOriginal) {
		
		Path startPath = Paths.get(start);
		try {
			// Use the finder to find all files that contain special characters.
			Files.walkFileTree(startPath, finder);
		} catch (IOException ioex) {
			ioex.printStackTrace();
			return false;
		}
		
		// Rewrite the files that contain special characters.
		LineRewriter rewriter = new LineRewriter();
		for (MsgRscFile fileWithSpecialChars : finder.getFilesContainingSpecialCharacters()) {
			String fileWithSpecialCharsPath = fileWithSpecialChars.getFullPath();
			// Try to replace all special characters with their unicode equivalent.
			if (!rewriter.bufferedRewrite(fileWithSpecialCharsPath, 
					(text) -> StringUtil.replaceAllSpecialChars(text), 
					replaceOriginal)) {
				logger.log("findAndRewriteSpecialCharacters - failed while rewriting the file!");
				return false;
			}
			logger.log("Successfully replaced all special characters in file: " + fileWithSpecialCharsPath);
		}
		
		return true;
	}
}
