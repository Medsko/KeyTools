package msgrsc.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BidirectionalFileReader {

	private List<String> lines;
	
	private int cursor;
	
	public String currentLine() {
		return lines.get(cursor);
	}
	
	public void next() {
		cursor++;
	}
	
	public void previous() {
		cursor--;
	}
	
	/**
	 * Finds the next occurrence of the given string.
	 * @return {@code true} if the target string was found. 
	 */
	public boolean findNext(String match) {
		int cursorBeforeSearch = cursor;
		match = match.toLowerCase();
		while (cursor < lines.size() - 1) {
			cursor++;
			String currentLine = lines.get(cursor);
			if (currentLine.toLowerCase().contains(match)) {
				// Found a line containing the search term.
				return true;
			}
		}
		// Reset the cursor to its initial position.
		cursor = cursorBeforeSearch;
		return false;
	}
	
	/**
	 * Finds the previous occurrence of the given string.
	 * @return {@code true} if the target string was found. 
	 */
	public boolean findPrevious(String match) {
		int cursorBeforeSearch = cursor;
		match = match.toLowerCase();
		while (cursor > 1) {
			cursor--;
			String currentLine = lines.get(cursor);
			if (currentLine.toLowerCase().contains(match)) {
				// Found a line containing the search term.
				return true;
			}
		}
		// Reset the cursor to its initial position.
		cursor = cursorBeforeSearch;
		return false;
	}
	
	public boolean readFile(String file) {
		lines = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
			
		} catch (IOException ioex) {
			ioex.printStackTrace();
			return false;
		}
		return true;
	}
}
