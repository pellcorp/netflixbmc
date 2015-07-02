package com.pellcorp.android.netflixbmc.jsonrpc;

import java.util.Map;

public interface JsonClient {
	JsonClientResponse send(String method, Map<String, Object> params);
}
