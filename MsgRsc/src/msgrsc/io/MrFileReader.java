package msgrsc.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper class for {@link BufferedReader}, specified in reading a message
 * resource file.
 */
public class MrFileReader {

	private List<String> messageResources;
	
	private int cursor;
	
	/**
	 * Moves the cursor to the next message resource. Since the cursor starts at
	 * -1 (like {@link ResultSet}), it is advisable to call this method before
	 * attempting to call one of the getters.
	 * 
	 * @return {@code false} if no more message resources remain, {@code true}
	 * otherwise. 
	 * @throws IllegalStateException - if called before {@link #readFile(String)}
	 * has been called.
	 */
	public boolean next() {
		if (messageResources == null) {
			throw new IllegalStateException("Call readFile() first!");
		}
		
		if (cursor + 1 >= messageResources.size()) {
			// The last message resource has already been passed.
			return false;
		}
		// There are more message resources available.
		cursor++;
		return true;
	}
	
	/**
	 * Returns the key-message pair that the cursor is currently pointing at,
	 * or null if the cursor is out of bounds.
	 */
	public String getMesRes() {
		if (cursorOutOfBounds()) {
			return null;
		}
		return messageResources.get(cursor);
	}
	
	/**
	 * Returns the key of the message resource that the cursor is currently
	 * pointing at, or null if the cursor is out of bounds.
	 */
	public String getKey() {
		if (cursorOutOfBounds()) {
			return null;
		}
		String nextMessageResource = messageResources.get(cursor);
		int indexOfEquals = nextMessageResource.indexOf("=");
		if (indexOfEquals < 0)
			return null;
		return nextMessageResource.substring(0,
				// Select everything to the left of the equals sign.
				nextMessageResource.indexOf("=")).trim();
	}
	
	/**
	 * Returns the message that the cursor is currently pointing to,
	 * or null if the cursor is out of bounds. 
	 */
	public String getMessage() {
		if (cursorOutOfBounds()) {
			return null;
		}
		String nextMessageResource = messageResources.get(cursor);
		return nextMessageResource.substring(
				// Select everything to the right of the equals sign.
				nextMessageResource.indexOf("=") + 1).trim();
	}
	
	/**
	 * Tests if the cursor is out of bounds.
	 */
	private boolean cursorOutOfBounds() {
		return cursor < 0 || cursor >= messageResources.size();
	}
	
	/**
	 * Reads in all message resources the given message resource file and populates
	 * an internal list of strings with it.
	 *  
	 * @param mrFile - the full path of the file to read.
	 * @return {@code boolean} value indicating success.
	 */
	public boolean readFile(String mrFile) {
		// Reset the instance variables.
		messageResources = new ArrayList<>();
		cursor = -1;
		
		try (BufferedReader reader = new BufferedReader(new FileReader(mrFile))) {
			String line;
			// Variable that can be used to concatenate messages that are spread over
			// multiple lines.
			String multiLine = null;
			while ((line = reader.readLine()) != null) {
				
				// Skip out-commented and empty lines.
				if (line.startsWith("#") || line.trim().length() == 0) {
					continue;
				}
			
				// Check if this is a message that encompasses multiple lines.
				if (line.trim().endsWith("\\")) {
					if (multiLine == null) {
						multiLine = line;
						continue;
					} else {
						multiLine += line;
						continue;
					}
				}
				// Check if this is the last line of a multi-line message. 
				if (multiLine != null) {
					// It is. Concatenate this last line to the multi-line message.
					line = multiLine + line;
					// Reset the multiLine variable.
					multiLine = null;
				}
				// Add the single or aggregated multi-line to the list of lines.
				messageResources.add(line);
			}
		} catch (IOException ioex) {
			ioex.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/**
	 * For now, this does nothing. It might be interesting to be able to jump to the
	 * first message key that starts with 'E' for instance, especially if the MR file
	 * is alphabetically sorted.
	 */
	public void jumpTo(char letter) {
		
	}
	
	/**
	 * Resets the cursor to its initial position (i.e. -1). 
	 */
	public void resetCursor() {
		cursor = -1;
	}
}
