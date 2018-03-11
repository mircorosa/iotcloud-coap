package it.mr.fetcher;

import it.mr.MyPrefs;
import it.mr.types.com.RequestDescriptor;
import it.mr.types.com.ResponseDescriptor;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import it.mr.types.internal.FetcherLogFormatter;

public class FetcherHTTPServer {

	private static final Logger LOG = Logger.getLogger("HTTP");


	
	private FetcherLauncher launcher;
	private HttpServer server;

	public FetcherHTTPServer(FetcherLauncher launcher) throws IOException {
		setupLogger(LOG);

		this.launcher=launcher;

		server = HttpServer.create(new InetSocketAddress(10500), 0);
		server.createContext(MyPrefs.HTTPSERVER_URI, new MyHandler(this.launcher));
		server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(5)); //Concurrent HTTP requests
		server.start();
		LOG.info("Server started @ "+server.getAddress());
	}

	//The handler generates a new RequestDescriptor and sends it to the fetcher, 
	//then waits for response and sends it back
	static class MyHandler implements HttpHandler {
		private FetcherLauncher launcher;
		public MyHandler(FetcherLauncher launcher) {
			this.launcher=launcher;
		}

		public void handle(HttpExchange t) throws IOException {
			switch (t.getRequestMethod()) {
			case "GET":
				RequestDescriptor getReqDescriptor = new RequestDescriptor(t.getRequestURI().toString());
				StringBuilder logString = new StringBuilder("GET request to "+getReqDescriptor.getServer()+"/"+getReqDescriptor.getUriString()+", Observing:"+getReqDescriptor.isObserving());
				if(getReqDescriptor.getToken()!=null) {
					logString.append(", Token:").append(new String(getReqDescriptor.getToken()));
				}
				LOG.info(logString.toString());
				Headers getResponseHeaders=t.getResponseHeaders();
				getResponseHeaders.set("Access-Control-Allow-Origin","*");
				t.sendResponseHeaders(200, 0);
				OutputStream getOs = t.getResponseBody();
				ResponseDescriptor payload = launcher.handleHTTPGet(getReqDescriptor);
				logString = new StringBuilder("Sending back GET from "+payload.getServer()+"/"+payload.getResource()+", value:"+payload.getValue());
				if(payload.getToken()!=null) {
					logString.append(", Token:").append(new String(payload.getToken()));
				}
				LOG.info(logString.toString());
				getOs.write(new Gson().toJson(payload).getBytes());
				getOs.close();
				break;
			case "POST":
				RequestDescriptor postReqDescriptor = new RequestDescriptor(t.getRequestURI().toString());
				InputStream postIs = t.getRequestBody();
				BufferedReader reader = new BufferedReader(new InputStreamReader(postIs));
				postReqDescriptor.setValue(reader.readLine());
				reader.close();
				postIs.close();
				LOG.info("POST request to "+postReqDescriptor.getServer()+"/"+postReqDescriptor.getUriString()+" ("+postReqDescriptor.getValue()+")");
				Headers postResponseHeaders=t.getResponseHeaders();
				postResponseHeaders.set("Access-Control-Allow-Origin","*");
				t.sendResponseHeaders(200, 0);
				OutputStream postOs = t.getResponseBody();
				boolean result = launcher.handleHTTPPost(postReqDescriptor);
				LOG.info("Sending back POST confirmation "+postReqDescriptor.getServer()+"/"+postReqDescriptor.getUriString()+" ("+postReqDescriptor.getValue()+")");
				postOs.write(new Gson().toJson(result).getBytes());
				postOs.close();
				break;
			case "PUT":
				break;
			case "DELETE":
				break;

			default:
				break;
			}
		}
	}

	private void setupLogger(Logger logger) {
		logger.setUseParentHandlers(false);
		FetcherLogFormatter formatter = new FetcherLogFormatter();
		ConsoleHandler handler = new ConsoleHandler();
		handler.setFormatter(formatter);
		logger.addHandler(handler);
	}
}
