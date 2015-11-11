package com.pellcorp.android.netflixbmc.jsonrpc;

import java.io.PrintWriter;
import java.io.StringWriter;

import net.minidev.json.JSONObject;

public class JsonClientResponse {
	private final Exception cause;
	private final boolean success;
	private final String error;
	private final JSONObject object;
	
	public JsonClientResponse(String error) {
		this.success = false;
		this.error = error;
		this.object = null;
		this.cause = null;
	}
	
	public JsonClientResponse(JSONObject object) {
		this.object = object;
		this.success = true;
		this.error = null;
		this.cause = null;
	}
	
	public JsonClientResponse(Exception cause) {
		this.success = false;
		this.cause = cause;
		this.object = null;
		this.error = cause.getMessage();
	}

	public JSONObject getResponse() {
		return object;
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

	public Throwable getCause() {
		return cause;
	}
}
