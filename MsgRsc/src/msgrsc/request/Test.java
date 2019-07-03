package msgrsc.request;

public class Test {

	public static void main(String[] args) {
//		testTranslationRequestBuilder();
		
		testPlaceholderAppender();
	}
	
	private static void testPlaceholderAppender() {
		String bugNumber = "QSD-12345"; // Test bug number..
		String aggregateDir = "C:\\src\\QSD-54770";
		PlaceholderAppender appender = new PlaceholderAppender(bugNumber);
		
		if (!appender.appendPlaceHolders(aggregateDir)) {
			p("Fail!");
		}
	}
	
	private static void testTranslationRequestBuilder() {

		TranslationRequestBuilder builder = new TranslationRequestBuilder();
		
		if (!builder.buildRequest("QSD-52754", "C:\\src\\QSD-49141_aka_BEL-CTR-P150")) {
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
	
	// Deprecated: first use PlaceholderAppender, then build request based on bug number.
//	@Deprecated
//	private static void testFullScan() {
//		TranslationRequestBuilder builder = new TranslationRequestBuilder();
//		String aggregateDir = "C:\\src\\QSD-50664_AN-CTR-D010";
//		String specificStartDir = aggregateDir + "\\QuinityFormsAdministration\\source\\com\\quinity\\qfa\\policyadministration";
//		
//		if (!builder.buildRequest("QSD-55412", specificStartDir)) {
//			p("Failed to build the request!");
//			return;
//		}
//		
//		TranslationRequestFileWriter writer = new TranslationRequestFileWriter(builder.getTranslationRequest());
//		writer.setIncludeColumnHeaders(true);
//		if (!writer.writeToCsvFile()) {
//			p("Failed to write request to file!");
//		}
//	}
	
	private static void p(Object text) {
		System.out.println(text);
	}
	
}
