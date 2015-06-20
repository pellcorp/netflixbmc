package com.pellcorp.android.netflixbmc.jsonrpc;

import java.util.Map;

public interface JsonClient {
	boolean send(String method, Map<String, Object> params) throws JsonClientException;
}
