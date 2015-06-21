package com.pellcorp.android.netflixbmc.jsonrpc;

import java.io.PrintWriter;
import java.io.StringWriter;

public class JsonClientUtils {
	public static String getStackTrace(Exception e) {
		StringWriter swriter = new StringWriter();
		PrintWriter writer = new PrintWriter(swriter);
		e.printStackTrace(writer);
		return swriter.toString();
	}
}
