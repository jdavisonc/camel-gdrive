/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jdavisonc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Ignore;
import org.junit.Test;

import com.github.jdavisonc.camel.gdrive.GDriveConstants;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.common.io.Files;

/**
 *
 * @author Jorge Davison (jdavisonc)
 *
 */
@Ignore
public class GDriveComponentTest extends CamelTestSupport {

	private static String CLIENT_ID = "?";
	private static String CLIENT_SECRET = "?";

	private static String REDIRECT_URI = "?";

	private String token = "?";

    @EndpointInject(uri = "direct:input")
    private ProducerTemplate template;

    @EndpointInject(uri = "mock:result")
    private MockEndpoint mockEndpoint;

	@Override
	public void setUp() throws Exception {
		HttpTransport httpTransport = new NetHttpTransport();
		JsonFactory jsonFactory = new JacksonFactory();

	    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
	            httpTransport, jsonFactory, CLIENT_ID, CLIENT_SECRET, Arrays.asList(DriveScopes.DRIVE))
	            .setAccessType("offline")
	            .setApprovalPrompt("auto").build();

        String url = flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URI).build();
        System.out.println("Please open the following URL in your browser then type the authorization code:");
        System.out.println("  " + url);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String code = br.readLine();

        GoogleTokenResponse response = flow.newTokenRequest(code).setRedirectUri(REDIRECT_URI).execute();

		System.out.println("Response " + response);
		System.out.println("Token " + response.getAccessToken());
		token = response.getAccessToken();
		super.setUp();
	}

	@Test
	public void testGDrive() throws Exception {
		mockEndpoint.expectedMinimumMessageCount(1);

		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put(GDriveConstants.ACCESS_TOKEN, token);
		headers.put(GDriveConstants.TITLE, "Title");
		headers.put(GDriveConstants.DESCRIPTION, "test");
		headers.put(GDriveConstants.CONTENT_TYPE, "text/plain");

		File tmpFile = File.createTempFile("temp-file", ".tmp");
		Files.write("This is a body test", tmpFile, Charset.defaultCharset());

		template.sendBodyAndHeaders(new FileInputStream(tmpFile), headers);

		assertMockEndpointsSatisfied();
	}

	@Override
	protected RouteBuilder createRouteBuilder() throws Exception {
		return new RouteBuilder() {
			@Override
			public void configure() {
				from("direct:input").to(
						"gdrive://mydrive?accessToken=&refreshToken=").to(
						"mock:result");
			}
		};
	}
}
