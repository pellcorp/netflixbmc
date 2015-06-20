package com.pellcorp.android.netflixbmc.jsonrpc;

import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionException;

public class JsonClientException extends RuntimeException {
	private static final long serialVersionUID = -8504987330420192297L;

	public JsonClientException(JSONRPC2SessionException e) {
		super(e);
	}
}
