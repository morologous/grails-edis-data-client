package edisdataclient

import grails.test.*

class EndToEndTests extends GroovyTestCase {
	EdisDataService edisDataService
	
	
	void testEndToEnd() {
		def secretKey = "<not provided>"
		def username = "<not provided>"
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

    void testFindInvestigation() {
		def results = edisDataService.findInvestigations([investigationNumber:'337-420'])
		assertEquals 1, results.size()
		assertEquals '337-420', results[0].investigationNumber
		assertTrue results[0].investigationTitle.contains('Beer')
    }

    void testFindInvestigationWithParams() {
    	def results = edisDataService.findInvestigations([investigationNumber:'337-420', investigationType:'Sec 337'])
		assertEquals 1, results.size()
		assertEquals '337-420', results[0].investigationNumber
		assertTrue results[0].investigationTitle.contains('Beer')
    }
}
