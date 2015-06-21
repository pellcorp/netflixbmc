package com.pellcorp.android.netflixbmc.jsonrpc;

public class JsonClientResponse {
	private final boolean success;
	private final String error;
	
	public JsonClientResponse(String error) {
		this.success = false;
		this.error = error;
	}
	
	public JsonClientResponse() {
		this.success = true;
		this.error = null;
	}
	
	public JsonClientResponse(Exception cause) {
		this.success = false;
		this.error = JsonClientUtils.getStackTrace(cause);
	}

	public boolean isSuccess() {
		return success;
	}
	
	public boolean isError() {
		return error != null;
	}
	
	public String getErrorMessage() {
		return error;
	}
}
