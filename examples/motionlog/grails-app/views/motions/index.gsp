<html>
    <head>
        <title>MotionLog</title>
    </head>
    <body>
    	Motions
    	<ul>
    		<g:each var="doc" in="${docs}">
	    		<li>${doc.id} (${doc.investigation.investigationNumber} / ${doc.investigation.investigationPhase}) ${doc.documentTitle}</li>
    		</g:each>
    	<ul>
    </body>
</html>
  
