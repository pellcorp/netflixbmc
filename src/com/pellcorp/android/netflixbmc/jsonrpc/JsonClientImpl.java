package com.pellcorp.android.netflixbmc.jsonrpc;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionException;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionOptions;

public class JsonClientImpl implements JsonClient {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final AtomicLong requestIdGen = new AtomicLong();
	private final JSONRPC2Session session;

	public JsonClientImpl(URL url) {
		this.session = new JSONRPC2Session(url);
	}

	public JsonClientResponse send(String method, Map<String, Object> params) {
		try {
			String requestId = requestIdGen.incrementAndGet() + "";
			JSONRPC2Request request = new JSONRPC2Request(method, requestId);
			request.setNamedParams(params);
			
			logger.debug(request.toString());
			
			JSONRPC2SessionOptions options = new JSONRPC2SessionOptions();
			options.setAllowedResponseContentTypes(new String[] {"application/json"});
			options.setConnectTimeout(10000);
			options.setReadTimeout(30000);
			
			session.setOptions(options);
		
			JSONRPC2Response response = session.send(request);
			if (response.indicatesSuccess()) {
				return new JsonClientResponse();
			} else {
				return new JsonClientResponse(response.getError().toString());
			}
		} catch (JSONRPC2SessionException e) {
			logger.error("Failed to send", e);
			return new JsonClientResponse(e); 
		}
	}
}
