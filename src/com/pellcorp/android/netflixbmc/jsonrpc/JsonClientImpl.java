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

	public JsonClientImpl(String url) {
		try {
			this.session = new JSONRPC2Session(new URL(url + "/jsonrpc"));
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid URL: " + url);
		}
	}

	public boolean send(String method, Map<String, Object> params)
			throws JsonClientException {
		String requestId = requestIdGen.incrementAndGet() + "";
		JSONRPC2Request request = new JSONRPC2Request(method, requestId);
		request.setNamedParams(params);
		
		System.out.println(request);
		
		JSONRPC2SessionOptions options = new JSONRPC2SessionOptions();
		options.setAllowedResponseContentTypes(new String[] {"application/json"});
		options.setConnectTimeout(10000);
		options.setReadTimeout(30000);
		
		session.setOptions(options);
		try {
			JSONRPC2Response response = session.send(request);
			return response.indicatesSuccess();
		} catch (JSONRPC2SessionException e) {
			logger.error("Failed to send", e);
			throw new JsonClientException(e);
		}
	}
}
