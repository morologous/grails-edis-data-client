package edisdataclient

import grails.test.*

class EdisDataServiceTests extends GrailsUnitTestCase {
	EdisDataService edisDataService = new EdisDataService();
	
	void testRequiredParamsSecretKey() {
		try {
			edisDataService.secretKey()
			fail("Expected required argument validation")
		} catch (ex) {
			assertTrue(ex instanceof IllegalArgumentException)
			assertEquals("Method call missing required parameters [username, password]", ex.getMessage())
		}		
	}

	void testRequiredParamsFindAttachment() {
		try {
			edisDataService.findAttachments()
			fail("Expected required argument validation")
		} catch (ex) {
			assertTrue(ex instanceof IllegalArgumentException)
			assertEquals("Method call missing required parameters [documentId]", ex.getMessage())
		}
	}

	
	void testRequiredParamsDownloadAttachment() {
		try {
			edisDataService.downloadAttachment()
			fail("Expected required argument validation")
		} catch (ex) {
			assertTrue(ex instanceof IllegalArgumentException)
			assertEquals("Method call missing required parameters [documentId, attachmentId, username, secretKey]", ex.getMessage())
		}
	}
	
	
}
