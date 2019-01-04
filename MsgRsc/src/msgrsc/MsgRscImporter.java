package msgrsc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class MsgRscImporter {

	private LanguageBundle german;
	
	private LanguageBundle french;

	private LanguageBundle flemish;

	private LanguageBundle norwegian;
	
	private List<LanguageBundle> languageBundles;
	
	public boolean importMessages(String importFileString, String msgRscDirString) {
				
		if (!readResources(importFileString, msgRscDirString)) {
			return false;
		}
		
		if (!processLanguageBundle(german)
				|| !processLanguageBundle(french)
				|| !processLanguageBundle(flemish)
				|| !processLanguageBundle(norwegian)) {
			return false;
		}
		
		return true;
	}
	
	private boolean processLanguageBundle(LanguageBundle bundle) {
		
		try (BufferedReader reader = Files.newBufferedReader(bundle.getMsgRscFile());
				BufferedWriter writer = Files.newBufferedWriter(bundle.getTempFile(), StandardOpenOption.CREATE)) {
			
			String line;
			
			while ((line = reader.readLine()) != null) {
				
				String lineKey = line.split("=")[0];
				
				if (lineKey != null) {
					for (String key : bundle.getKeySet()) {
						if (lineKey.equals(key)) {
							// This is a line for which we have a new message resource. Fill it and write it to temporary file.
							line = key + "=" + bundle.getMessage(key);
							break;
						}
					}
				}
				writer.write(line);
				writer.newLine();
			}
			
		} catch (IOException ioex) {
			ioex.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	
	private boolean readResources(String importFileString, String msgRscDirString) {
		
		german = new LanguageBundle("de", msgRscDirString);
		french = new LanguageBundle("fr", msgRscDirString);
		flemish = new LanguageBundle("vls", msgRscDirString);
		norwegian = new LanguageBundle("nb", msgRscDirString);

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(importFileString)))) {
			
			String line;
			
			while ((line = reader.readLine()) != null) {
				String[] fields = line.split(";");
				
				if (fields.length != 7) {
					System.out.println("Wrong number of columns!");
					continue;
				}
				
				String key = fields[0];
				String germanMsg = fields[3];
				String frenchMsg = fields[4];
				String flemishMsg = fields[5];
				String norwegianMsg = fields[6];
				
				german.addMessage(key, germanMsg);
				french.addMessage(key, frenchMsg);
				flemish.addMessage(key, flemishMsg);
				norwegian.addMessage(key, norwegianMsg);
			}
			
			
		} catch (IOException ioex) {
			ioex.printStackTrace();
			return false;
		}
		
		return true;
	}
	
}
