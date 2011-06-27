package motionlog

//import edisDataClient.EdisDataService

class MotionsController {

		def edisDataService

    def index = { 
			def docs = edisDataService.findDocuments([documentType:"Motion"])    
    	return [ docs : docs ]
    }
}
