package msgrsc;

import msgrsc.request.TranslationRequest;
import msgrsc.request.TranslationRequestBuilder;
import msgrsc.request.TranslationRequestFileWriter;

public class Master {

	public static void main(String[] args) {
		
		
		
	}
	
	private static void writeCsvTranslationRequest(String bugNumber, String msgRscDir) {
		
		TranslationRequestBuilder builder = new TranslationRequestBuilder();		
		if (!builder.buildByBugNumber(bugNumber, msgRscDir)) {
			System.out.println("Error!");
			return;
		}
		TranslationRequest request = builder.getTranslationRequest();
	
		TranslationRequestFileWriter writer = new TranslationRequestFileWriter(request);
		// ...not necessary to include these, of course.
		writer.setIncludeColumnHeaders(true);
		if (!writer.writeToCsvFile()) {
			System.out.println("Error!");
		}
	}

}
