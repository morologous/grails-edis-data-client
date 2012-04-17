package edisdataclient

import grails.test.*

class SecretKeyTests extends GroovyTestCase {
	EdisDataService edisDataService

	void testSecretKey() {
		def result = edisDataService.secretKey([username:'EDISDATACLIENT',password:'<not provided>'])
		assertEquals("e70cd3eae9f06e2cda5f9864853a29ac9ebe8ccd",  result)
	}
	
}
