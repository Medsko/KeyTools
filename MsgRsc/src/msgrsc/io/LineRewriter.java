package msgrsc.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.function.Predicate;

public class LineRewriter {

	private StringModifier modifier;
	
	private Charset readCharset;
	
	private Charset writeCharset;
	
	/**
	 * Constructor for a {@link LineRewriter} with default ISO_8859_1 encoding
	 * for both reading and writing. 
	 */
	public LineRewriter() {
		this(StandardCharsets.ISO_8859_1, StandardCharsets.ISO_8859_1);
	}

	public LineRewriter(Charset readCharset, Charset writeCharset) {
		this.readCharset = readCharset;
		this.writeCharset = writeCharset;
	}
	
	public boolean rewrite(String file, boolean replaceOriginal) {
		return rewrite(file, modifier, replaceOriginal);
	}
	
	public boolean bufferedRewrite(String file, StringModifier modifier, boolean replaceOriginal) {
		
		Path original = Paths.get(file);
		String rewriteFileName = original.getFileName().toString();
		rewriteFileName = rewriteFileName.replace(".", "rewrite.");
		Path dir = original.getParent();
		Path rewrite = dir.resolve(Paths.get(rewriteFileName));
		
		try (BufferedReader reader = Files.newBufferedReader(original, readCharset);
				BufferedWriter writer = Files.newBufferedWriter(rewrite, writeCharset)) {
			
			String line;
			while ((line = reader.readLine()) != null) {
				String rewrittenLine = modifier.modify(line);
				writer.write(rewrittenLine);
				writer.newLine();
			}
						
		} catch (IOException ioex) {
			ioex.printStackTrace();
			return false;
		}
		
		if (replaceOriginal) {
			try {
				Files.move(rewrite, original, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		} 
		
		return true;
	}
	
	/**
	 * Rewrite without replace. 
	 */
	public boolean rewrite(String file, StringModifier modifier) {
		return rewrite(file, modifier, false);
	}
	
	public boolean rewrite(String file, StringModifier modifier, boolean replaceOriginal) {
		Path original = Paths.get(file);
		// Determine the file name for the resulting rewritten file.
		String rewriteFileName = original.getFileName().toString();
		rewriteFileName = rewriteFileName.replace(".", "temp.");
		Path dir = original.getParent();
		Path rewrite = dir.resolve(Paths.get(rewriteFileName));
		
		List<String> allLinesInFile;
		
		try {
			allLinesInFile = Files.readAllLines(original, readCharset);
		} catch (IOException ioex) {
			ioex.printStackTrace();
			return false;
		}
		
		try (BufferedWriter writer = Files.newBufferedWriter(rewrite, writeCharset)) {
			
			for (String line : allLinesInFile) {
				// Edit the line as specified in the given Action. 
				String rewrittenLine = modifier.modify(line);
				// Write the line to file.
				writer.write(rewrittenLine);
				writer.newLine();
			}
			
			if (replaceOriginal) {
				try {
					Files.move(rewrite, original, StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}
			}
			
		} catch (IOException ioex) {
			ioex.printStackTrace();
			return false;
		}

		return true;
	}
	
	/**
	 * Like {@link #rewrite(String, Action)}, rewrites the file at the given location.
	 * With this method, it is also possible to specify a condition for whether the line
	 * should be rewritten.
	 */
	public boolean rewriteIf(String file, StringModifier modifier, Predicate<String> condition) {
		Path original = Paths.get(file);
		// Determine the file name for the resulting rewritten file.
		String rewriteFileName = original.getFileName().toString();
		rewriteFileName = rewriteFileName.replace(".", "rewrite.");
		Path dir = original.getParent();
		Path rewrite = dir.resolve(Paths.get(rewriteFileName));
		
		List<String> allLinesInFile;
		
		try {
			allLinesInFile = Files.readAllLines(original, readCharset);
		} catch (IOException ioex) {
			ioex.printStackTrace();
			return false;
		}
		
		try (BufferedWriter writer = Files.newBufferedWriter(rewrite, writeCharset)) {
			
			for (String line : allLinesInFile) {
				// Check whether this line should be rewritten.
				if (condition != null && condition.test(line)) { 
					// Edit the line as specified in the given Action.
					line = modifier.modify(line);
				}
				// Write the line to file.
				writer.write(line);
				writer.newLine();
			}
		} catch (IOException ioex) {
			ioex.printStackTrace();
			return false;
		}
		return true;
	}

	public void setModifier(StringModifier modifier) {
		this.modifier = modifier;
	}
}
