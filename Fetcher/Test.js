var page = require('webpage').create();
var server = require('webserver').create();

page.onConsoleMessage = function(msg) {
  console.log('CONSOLE: ' + msg);
};

page.open("http://iot220999.appspot.com/", function(status) {	//DOMAINS: http://iot220999.appspot.com/  http://localhost:8080/
  	//Do something
});

var service = server.listen(11000, function (request, response) {
    var reqtype = request.method;
    console.log(reqtype);
    
   switch (reqtype) {
  		case "POST":
		  	console.log('POST received from HTTPServer at '+new Date()+' with JSON '+request.post);
			var postBody = request.post;
			var requestJSCode = addslashes(postBody);
			page.evaluateJavaScript('function(){gapi.client.messaging.messagingEndpoint.sendUpdate({\'value\': \''+requestJSCode+'\'}).execute();}');
			response.statusCode = 200;
			response.headers = {'Cache': 'no-cache','Content-Type': 'text/plain;charset=utf-8'};
			response.write('Resource values sent successfully.');
			response.close();
    		break;
    		
		case "PUT":
			console.log('[PUT] '+new Date()+' @ '+decodeURIComponent(request.url.substring(1))+' with payload '+request.post);
			var putBody = request.post;
			var putUri = decodeURIComponent(request.url.substring(1));
			var jsonObject = addslashes(putBody);
			var uri = addslashes(putUri);
			if(uri==="")	uri = " ";
			var script = "function(){gapi.client.datastore.datastoreEndpoint.handlePUT({\"uri\": \""+uri+"\",\"json\": \""+jsonObject+"\"}).execute();}";
			console.log(script);
			page.evaluateJavaScript(script);
			response.statusCode = 200;
			response.headers = {'Cache': 'no-cache','Content-Type': 'text/plain;charset=utf-8'};
			response.write('PUT request sent successfully to the cloud.');
			response.close();
    		break;
    		
  		case "DELETE":	
			console.log('DELETE received from HTTPServer at '+new Date()+' with JSON '+decodeURIComponent(request.url.substring(1)));
			var putUri = decodeURIComponent(request.url.substring(1));
			var uri = addslashes(putUri);
			var script = "function(){gapi.client.datastore.datastoreEndpoint.handleDELETE({\"uri\": \""+uri+"\"}).execute();}";
			page.evaluateJavaScript(script);
			response.statusCode = 200;
			response.headers = {'Cache': 'no-cache','Content-Type': 'text/plain;charset=utf-8'};
			response.write('DELETE request sent successfully to the cloud.');
			response.close();
    		break;
    		
  		case "GET":
    		break;
    		
 		default:
    		console.log("Unknown request type.");
	}


});

//String escaping method
function addslashes( str ) {
    return (str + '').replace(/[\\"']/g, '\\$&').replace(/\u0000/g, '\\0');
}
