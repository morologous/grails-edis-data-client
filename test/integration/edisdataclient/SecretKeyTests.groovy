package edisdataclient

import grails.test.*

class SecretKeyTests extends GroovyTestCase {
	EdisDataService edisDataService

	void testSecretKey() {
		def result = edisDataService.secretKey([username:'EDISDATACLIENT',password:'3d1sD4t4C1!3nt'])
		assertEquals("7c07ecc23fb8c1115e1b22c7a3a822fa5fa2b894",  result)
	}
	
}
