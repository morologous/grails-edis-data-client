package edisdataclient

import grails.test.*

class SecretKeyTests extends GroovyTestCase {
	EdisDataService edisDataService

	void testSecretKey() {
		def result = edisDataService.secretKey([username:'<not provided>',password:'<not provided>'])
		assertEquals("<not provided>",  result)
	}
	
}
