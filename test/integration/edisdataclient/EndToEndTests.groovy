package edisdataclient

import grails.test.*

class EndToEndTests extends GroovyTestCase {
	EdisDataService edisDataService
	
	
	void testEndToEnd() {
		def secretKey = "7c07ecc23fb8c1115e1b22c7a3a822fa5fa2b894"
		def username = "EDISDATACLIENT"
		def docId = 147025
		def docs = edisDataService.findDocuments([username:username, secretKey:secretKey, id:docId])
		def doc = docs[0]
		
		def atts = edisDataService.findAttachments([username:username, secretKey:secretKey, documentId:doc.id])
		def att = atts[0]
		
		InputStream stream = edisDataService.downloadAttachment([username:username, secretKey:secretKey, documentId:att.documentId, attachmentId:att.id])
		
		def os = new ByteArrayOutputStream();
		
		while (stream.available()) {	
			os.write stream.read()
		}
		os.close();
		
		assertEquals(att.fileSize, os.size())
	}

	
	void testAnonymousDocService() {
		def docs = edisDataService.findDocuments()
		
		assertEquals(100, docs.size)
    }
}
