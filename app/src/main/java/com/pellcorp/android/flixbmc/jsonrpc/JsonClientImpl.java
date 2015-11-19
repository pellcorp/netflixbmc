package com.pellcorp.android.flixbmc.jsonrpc;

import java.net.MalformedURLException;
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
import com.thetransactioncompany.jsonrpc2.client.RawResponse;
import com.thetransactioncompany.jsonrpc2.client.RawResponseInspector;

public class JsonClientImpl implements JsonClient {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final AtomicLong requestIdGen = new AtomicLong();
	private final JSONRPC2Session session;
	private final BasicAuthenticator authenticator;

	public JsonClientImpl(String url, String username, String password) {
		URL jsonRpcUrl = toJsonRpcUrl(url);
		String userInfo = jsonRpcUrl.getUserInfo();

        String urlWithoutUserInfo = null;
        if(userInfo != null) {
            String[] userInfoParts = userInfo.split(":", 2);
            username = userInfoParts[0];
            password= userInfoParts[1];

			urlWithoutUserInfo = jsonRpcUrl.getProtocol() + "://" + jsonRpcUrl.getHost() + ":" +
					jsonRpcUrl.getPort() + jsonRpcUrl.getPath();
        } else {
			urlWithoutUserInfo = jsonRpcUrl.toString();
        }

        if(username != null && password != null) {
		    authenticator = new BasicAuthenticator(username, password);
        } else {
            authenticator = null;
        }

		try {
			jsonRpcUrl = new URL(urlWithoutUserInfo);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e.getMessage());
		}

        logger.info("Kodi URL: {}", jsonRpcUrl);

		this.session = new JSONRPC2Session(jsonRpcUrl);
	}

    private URL toJsonRpcUrl(String url) {
        try {
            if (url.endsWith("/")) {
                url = url.substring(0, url.length() - 1);
            }
			if(!url.endsWith("/jsonrpc") )
				url += "/jsonrpc";

            return new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

	public JsonClientResponse send(String method, Map<String, Object> params) {
		try {
			String requestId = requestIdGen.incrementAndGet() + "";
			JSONRPC2Request request = new JSONRPC2Request(method, params, requestId);
			
			logger.debug(request.toString());
			
			JSONRPC2SessionOptions options = new JSONRPC2SessionOptions();
			options.setAllowedResponseContentTypes(new String[] {"application/json", "text/html"});
			options.setConnectTimeout(10000);
			options.setReadTimeout(30000);

            if(authenticator != null) {
                session.setConnectionConfigurator(authenticator);
            }

			session.setOptions(options);
			
			RawResponseInspector rawResponse = new RawResponseInspector() {
				@Override
				public void inspect(RawResponse response) {
					logger.debug(response.getContent());
				}
			};
			
			session.setRawResponseInspector(rawResponse);
			
			JSONRPC2Response response = session.send(request);
			if (response.indicatesSuccess()) {
				return new JsonClientResponse(response.toJSONObject());
			} else {
				return new JsonClientResponse(response.getError().toString());
			}
		} catch (JSONRPC2SessionException e) {
			logger.error("Failed to send", e);
			return new JsonClientResponse(e); 
		}
	}
}
