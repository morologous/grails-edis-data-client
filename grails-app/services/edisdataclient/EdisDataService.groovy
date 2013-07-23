package edisdataclient

import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient

import java.text.SimpleDateFormat

import org.codehaus.groovy.grails.plugins.codecs.Base64Codec

/**
 * 
 * EDIS data webservice interface
 * 
 * @author jyankus
 *
 */
class EdisDataService {

	// TODO: convert this to a "Category" http://www.packtpub.com/article/metaprogramming-and-groovy-mop
	{
		Convert.from(String).to(Date).using({ value -> new java.text.SimpleDateFormat(determineDateFormat(value)).parse(value) })
	}
	
	static transactional = false
	
	static def convertStringToDate = { str -> 
		if (str) {
			def divinedFormat = determineDateFormat(str)
			if (divinedFormat) {
				new SimpleDateFormat(divinedFormat).parse(str)
			}
		} else {
			null
		}
	}
	
	private def createRESTClient(params=[:]) {
		def webserviceURL = "https://edis.usitc.gov/data/"
		if (params.baseURL && params.baseURL.length() > 0) {
			webserviceURL = params.baseURL
		}
		return new RESTClient(webserviceURL);
	}
	
	/**
	 * Generate a secret key for later use
	 * 
	 * Required Arguments:
	 * username - the user name 
	 * password - the password
	 * 
	 * @param params A map of parameters.
	 * @return the secretKey
	 */
	def secretKey(params=[:]) {
		validateParams(params, ["username","password"])
		def rest = createRESTClient(params)
		def path = 'secretKey/' + params.username
		def resp = rest.post(path: path, body: [password:params.password], requestContentType : ContentType.URLENC)
		return resp.data.secretKey.text()
	}

	/**
	 * find investigations.
	 * 
	 * Valid parameters:
	 * 
	 * investigationNumber - the investigation number
	 * investigationPhase - the name of the investigation phase
	 * investigationType - the name of the investigation type
	 * investigationStatus - the name of the investigation status
	 * 
	 * investigationNumber must be provided if investigationPhase is provided
	 * 
	 * @param params the parameters, in map Êform
	 * @return
	 */
	def findInvestigations(params=[:]) {
		def rest = createRESTClient(params)
		
		def query = [:]
		if (params.investigationType) {
			query << [investigationType:params.investigationType]
		}
		if (params.investigationStatus) {
			query << [investigationStatus:params.investigationStatus]
		}
		if (params.investigationId) {
			query << [investigationId:params.investigationId]
		}
		def path = "investigation/"
		if (params.investigationNumber) {
			path += params.investigationNumber 
			if (params.investigationPhase) {
				path += "/" + params.investigationPhase
			}
		}
		if (params.pageNumber) {
			query << [pageNumber:params.pageNumber]
		}

		def invs = []
		rest.get(contentType:ContentType.XML, path:path, query:query) {
			resp, xml ->
			xml.investigations.investigation.each {
				invs << buildInv(it)
			}
		}
		return invs
	}
	
	/**
	 * find documents
	 * 
	 * Valid parameters:
	 * 
	 * securityLevel - the security level name
	 * investigationNumber - the investigation number
	 * investigationPhase - the investigation phase
	 * documentType - the document type
	 * firmOrg - the firm that filed the doc
	 * 
	 * @return a list of document maps
	 */
    def findDocuments(params=[:]) {		
		def rest = createRESTClient(params)
		
		def headers = [:]
		headers << applySecurity(params)

		def query = [:]
		if (params.securityLevel) {
			query << [securityLevel:params.securityLevel]
		}
		if (params.investigationNumber) {
			query << [investigationNumber:params.investigationNumber]
		}
		if (params.investigationPhase) {
			query << [investigationPhase:params.investigationPhase]
		}
		if (params.documentType) {
			query << [documentType:params.documentType]
		}
		if (params.firmOrg) {
			query << [firmOrg:params.firmOrg]
		}
		if (params.pageNumber) {
			query << [pageNumber:params.pageNumber]
		}
		if (params.officialReceivedDate) {
			query << [officialReceivedDate:decodeDateParam(params.officialReceivedDate)]
		}
		if (params.modifiedDate) {
			query << [modifiedDate:decodeDateParam(params.modifiedDate)]
		}

		def path = "document/"
		if (params.id) {
			path = path + params.id
		} 
		
		def docs = []
		def resp = rest.get(contentType: ContentType.XML, path:path, query:query, headers:headers) {
			resp, xml ->
			xml.documents.document.each {
				docs << buildDoc(it)
			}
		}

		return docs
    }
	
	def findAttachments(params = [:]) {
		validateParams(params, ["documentId"])
		
		def rest = createRESTClient(params)
		
		def headers = [:]
		headers << applySecurity(params)
		
		def atts = []
		rest.get(contentType:ContentType.XML, path:"attachment/" +params.documentId, headers:headers) {
			resp, xml ->
			xml.attachments.attachment.each {
				atts << buildAtt(it)
			}
		}
		return atts
	}
	
	def downloadAttachment(params=[:]) {
		validateParams(params, ["documentId","attachmentId","username","secretKey"])
		def rest = createRESTClient(params)
		
		def headers = [:]
		headers << applySecurity(params)
		
		def path = "download/" + params.documentId + "/" + params.attachmentId
		def resp = rest.get(contentType:ContentType.BINARY, path:path, headers:headers)
		return resp.data
	}
 
	private def decodeDateParam(params=[:]) {
		switch(params.comparisonType) {
			case "BETWEEN": 
				 return params.comparisonType + ":" + params.toDate + ":" + params.fromDate
			case "BEFORE":
			case "AFTER":
			case "EXACT":
				return params.comparisonType+ ":" + params.date
			default:
				throw new IllegalArgumentException("Date Parameter values incorrect: $params.comparisonType not a valid comparisonType")
		}
			
	}
	
	private def validateParams(params=[:], requiredParams=[]) {
		def missingParams = []
		requiredParams.each {
			if (!params.get(it)) {
				missingParams << it
			}
		}
		if (missingParams.size > 0) {
			throw new IllegalArgumentException("Method call missing required parameters $missingParams")
		}
	}
	
	private def applySecurity(params=[:]) {
		def auth = [:]
		if (params.username && params.secretKey) {
			auth << ["Authorization":"Basic " + new Base64Codec().encode(params.username + ":" + params.secretKey)]
		} 
		return auth
	}
	
	private def buildDoc (xml) {
		def doc = [:]
		doc << [id: xml.id.text() as Long]
		doc << [documentTitle: xml.documentTitle.text()]
		doc << [documentType: xml.documentType.text()]
		doc << [securityLevel: xml.securityLevel.text()]
		doc << [firmOrganization: xml.firmOrganization.text()]
		doc << [filedBy: xml.filedBy.text()]
		doc << [onBehalfOf: xml.onBehalfOf.text()]
		doc << [documentDate: convertStringToDate(xml.documentDate.text()) as Date]
		doc << [officialReceivedDate: convertStringToDate(xml.officialReceivedDate.text()) as Date]
		doc << [modifiedDate: convertStringToDate(xml.modifiedDate.text()) as Date]
		doc << [investigation: buildInv(xml)]
		return doc

	}
	
	private def buildInv (xml) {
		def inv = [:]
		inv << [investigationNumber: xml.investigationNumber.text()]
		inv << [investigationPhase: xml.investigationPhase.text()]
		inv << [investigationStatus: xml.investigationStatus.text()]
		inv << [investigationTitle: xml.investigationTitle.text()]
		inv << [investigationType: xml.investigationType.text()]
		if (xml.investigationId.text()) {
			inv << [investigationId: xml.investigationId.text() as Long]
		}
		return inv
	}
		
	private def buildAtt (xml) {
		def att = [:]
		att << [id: xml.id.text()]
		att << [documentId: xml.documentId.text()]
		att << [title: xml.title.text()]
		if (xml.fileSize.text()) {
			att << [fileSize: xml.fileSize.text() as Long]
		}
		att << [originalFileName: xml.originalFileName.text()]
		if (xml.pageCount.text()) {
			att << [pageCount: xml.pageCount.text() as Long]
		}
		att << [createDate: convertStringToDate(xml.createDate.text()) as Date]
		if (xml.lastModifiedDate.text()) {
			att << [lastModifiedDate: convertStringToDate(xml.lastModifiedDate.text()) as Date]
		}
		return att
	}
	
	private static def determineDateFormat (dateString) {
		
		def dtFrmtRegX = [:]
		dtFrmtRegX << ["^\\d{4}-\\d{2}-\\d{2}\\s\\d{1,2}:\\d{2}:\\d{2}\\.\\d{1,}\$" : 'yyyy-MM-dd HH:mm:ss.S'] // 2011-08-17 14:19:45.0
		dtFrmtRegX << ["^[a-z]{3}\\s[a-z]{3}\\s\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}\\s[a-z]{3}\\s\\d{4}\$" : 'EEE MMM dd hh:mm:ss zzz yyyy']  // Fri Aug 05 00:00:00 EDT 2011
		dtFrmtRegX << ["^\\d{4}-\\d{2}-\\d{2}\\s\\d{1,2}:\\d{2}:\\d{2}\$" : 'yyyy-MM-dd HH:mm:ss'] // 2011-08-17 14:19:45.0
		dtFrmtRegX << ["^\\d{4}\\/\\d{2}\\/\\d{2}\\s\\d{1,2}:\\d{2}:\\d{2}\$" : 'yyyy/MM/dd HH:mm:ss'] // 2012/06/05 09:38:48
		
		
		for (String regexp : dtFrmtRegX.keySet()) {
			if (dateString.toLowerCase().matches(regexp)) {
				return dtFrmtRegX.get(regexp)
			}
		}
		return null // Unknown format.
	}
	
}
