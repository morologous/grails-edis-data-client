package edisdataclient

import org.codehaus.groovy.grails.plugins.codecs.Base64Codec;

import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.RESTClient;

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
		Convert.from(String).to(Date).using({ value -> new java.text.SimpleDateFormat('yyyy-MM-dd hh:mm:ss').parse(value) })
	}
	
    static transactional = false

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
		def rest = new RESTClient('https://edis.usitc.gov/data/')
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
		def rest = new RESTClient('https://edis.usitc.gov/data/')
		
		def query = [:]
		if (params.investigationType) {
			query << [investigationType:investigationType]
		}
		if (params.investigationStatus) {
			query << [investigationStatus:investigationStatus]
		}
		path = "investigation/"
		if (params.investigationNumber) {
			path += params.investigationNumber 
			if (params.investigationPhase) {
				path += "/" + params.investigationPhase
			}
		}
		
		def invs = []
		rest.get(contentType.XML, path:"investigation") {
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
		def rest = new RESTClient('https://edis.usitc.gov/data/')
		
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
		
		def path = "document/"
		if (params.id) {
			path = path + params.id
		} 
		
		def resp = rest.get(contentType: ContentType.XML, path:path, query:query, headers:headers)	

		def docs = []
		resp.data.documents.document.each {
			docs << buildDoc(it)
		}
		return docs
    }
	
	def findAttachments(params = [:]) {
		validateParams(params, ["documentId"])
		
		def rest = new RESTClient('https://edis.usitc.gov/data/')
		
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
		def rest = new RESTClient('https://edis.usitc.gov/data/')
		
		def headers = [:]
		headers << applySecurity(params)
		
		def path = "download/" + params.documentId + "/" + params.attachmentId
		return rest.get(contentType:ContentType.BINARY, path:path, headers:headers).data
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
		doc << [documentDate: xml.documentDate.text() as Date]
		doc << [officialReceivedDate: xml.officialReceivedDate.text() as Date]
		doc << [modifiedDate: xml.modifiedDate.text() as Date]
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
		att << [createDate: xml.createDate.text() as Date]
		if (xml.lastModifiedDate.text()) {
			att << [lastModifiedDate: xml.lastModifiedDate.text() as Date]
		}
		return att
	}
	
	
}
