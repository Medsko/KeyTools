package msgrsc.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Wrapper for a {@link BufferedWriter}. Upon creation, it checks whether the
 * last line of the file that it is going to write to is a newline character.
 * If not, a new line is appended before any other output is appended. 
 */
public class AppendingFileWriter implements AutoCloseable {

	private BufferedWriter writer;
	
	public AppendingFileWriter(Path filePath) throws IOException {
		writer = Files.newBufferedWriter(filePath, StandardOpenOption.APPEND);
		if (!endsWithNewLine(filePath.toString())) {
			writer.newLine();
		}
	}
	
	public AppendingFileWriter(String filePath) throws IOException {
		this(Paths.get(filePath));
	}
	
	/**
	 * Writes the given line to file. Follows it up with a newline character. 
	 */
	public void writeLine(String line) throws IOException {
		writer.write(line);
		writer.newLine();
	}
	
	private boolean endsWithNewLine(String fileName) throws IOException {
		// This is completely stolen, no original thought was wasted in
		// the creation of this method :).
	    try (RandomAccessFile raf = new RandomAccessFile(fileName, "r")) {
	        long pos = raf.length() - 2;
	        if (pos < 0) {
	        	// Empty file.
	        	return true; 
	        }
	        raf.seek(pos);
	        return raf.read() == '\r' && raf.read() == '\n';
	    }
	}
	
	@Override
	public void close() throws IOException {
		writer.close();
	}

}
