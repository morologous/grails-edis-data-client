package edisdataclient

import grails.test.*

class EdisDataServiceTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

	void testRequiredParamsSecretKey() {
		try {
			new EdisDataService().secretKey()
			fail("Expected required argument validation")
		} catch (ex) {
			assertTrue(ex instanceof IllegalArgumentException)
			assertEquals("Method call missing required parameters [username, password]", ex.getMessage())
		}		
	}

	void testRequiredParamsFindAttachment() {
		try {
			new EdisDataService().findAttachments()
			fail("Expected required argument validation")
		} catch (ex) {
			assertTrue(ex instanceof IllegalArgumentException)
			assertEquals("Method call missing required parameters [documentId]", ex.getMessage())
		}
	}

	
	void testRequiredParamsDownloadAttachment() {
		try {
			new EdisDataService().downloadAttachment()
			fail("Expected required argument validation")
		} catch (ex) {
			assertTrue(ex instanceof IllegalArgumentException)
			assertEquals("Method call missing required parameters [documentId, attachmentId, username, secretKey]", ex.getMessage())
		}
	}

	
	void testSecretKey() {
		EdisDataService svc = new EdisDataService()
		def result = svc.secretKey([username:'EDISDATACLIENT',password:'3d1sD4t4C1!3nt'])
		assertEquals("7c07ecc23fb8c1115e1b22c7a3a822fa5fa2b894",  result)
	}
	
	
	void testEndToEnd() {
		def secretKey = "7c07ecc23fb8c1115e1b22c7a3a822fa5fa2b894"
		def username = "EDISDATACLIENT"
		def docId = 147025
		EdisDataService svc = new EdisDataService()
		def docs = svc.findDocuments([username:username, secretKey:secretKey, id:docId])
		def doc = docs[0]
		
		def atts = svc.findAttachments([username:username, secretKey:secretKey, documentId:doc.id])
		def att = atts[0]
		
		InputStream stream = svc.downloadAttachment([username:username, secretKey:secretKey, documentId:att.documentId, attachmentId:att.id])
		
		def os = new ByteArrayOutputStream();
		
		while (stream.available()) {	
			os.write stream.read()
		}
		os.close();
		
		assertEquals(att.fileSize, os.size())
	}

	
	void testAnonymousDocService() {
		EdisDataService svc = new EdisDataService()
		def docs = svc.findDocuments()
		
		assertEquals(100, docs.size)
    }
}
