package com.pellcorp.android.netflixbmc.jsonrpc;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JsonClientImplTest extends Assert {
	private JsonClient client;
	
	@Before
	public void setup() throws Exception {
		client = new JsonClientImpl("http://localhost:8080", "test", "test");
	}
	
	@Test
	public void testItemSearch() throws Exception {
		MovieIdSender itemSearch = new MovieIdSender(client);
		//http://www.netflix.com/watch/70259443?trackId=13462050&tctx=1%2C0%2C48d00020-b7c9-46ea-ae58-219011a2ed29-16193513
		itemSearch.sendMovie("http://www.netflix.com/watch/70259443?trackId=13462050&tctx=1%2C0%2C48d00020-b7c9-46ea-ae58-219011a2ed29-16193513");
		//System.out.println(results);
	}
}
