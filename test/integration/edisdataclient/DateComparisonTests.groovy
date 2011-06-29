package edisdataclient

import grails.test.*

class DateComparisonTests extends GroovyTestCase {
	EdisDataService edisDataService
	

	void testDateComparisonParamWorks() {
		def docs = edisDataService.findDocuments([officialReceivedDate:[comparisonType:"EXACT",date:"2011-06-27"]])
		docs.each {
			assertEquals("6/27/11", it.officialReceivedDate.getDateString())
		}
	}

	
}
