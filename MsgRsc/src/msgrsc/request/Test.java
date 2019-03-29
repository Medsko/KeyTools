package msgrsc.request;

public class Test {

	public static void main(String[] args) {
		testTranslationRequestBuilder();

//		testFullScan();
	}
	
	private static void p(Object text) {
		System.out.println(text);
	}
	
	private static void testFullScan() {
		TranslationRequestBuilder builder = new TranslationRequestBuilder();
		builder.setShouldPerformFullScan(true);
		
		if (!builder.buildRequest("QSD-52754", "C:\\src\\trunk")) {
			p("Failed to build the request!");
			return;
		}
		
		TranslationRequestFileWriter writer = new TranslationRequestFileWriter(builder.getTranslationRequest());
		writer.setIncludeColumnHeaders(true);
		if (!writer.writeToCsvFile()) {
			p("Failed to write request to file!");
		}
	}
	
	private static void testTranslationRequestBuilder() {

		TranslationRequestBuilder builder = new TranslationRequestBuilder();
		
		if (!builder.buildRequest("QSD-52224", "C:\\src\\QSD-36385")) {
			p("Failed to build the request!");
			return;
		}
		
		TranslationRequestFileWriter writer = new TranslationRequestFileWriter(builder.getTranslationRequest());
		writer.setIncludeColumnHeaders(true);
		if (!writer.writeToCsvFile()) {
			p("Failed to write request to file!");
		} else {
			p("Great success for glorious nation of Kazachstan...I mean Keylane!");
		}
		
	}
	
}
