package com.pellcorp.android.flixbmc.jsonrpc;

import java.util.Map;

public interface JsonClient {
	JsonClientResponse send(String method, Map<String, Object> params);
}
