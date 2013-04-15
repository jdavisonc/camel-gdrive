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
package com.github.jdavisonc.camel.gdrive;

import java.io.IOException;
import java.io.InputStream;

import org.apache.camel.Consumer;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.ScheduledPollEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

/**
 * Represents a GDrive endpoint.
 * 
 * @author Jorge Davison (jdavisonc)
 */
public class GDriveEndpoint extends ScheduledPollEndpoint {
	
	private static final Logger LOG = LoggerFactory.getLogger(GDriveEndpoint.class);
	
	private Drive gDriveClient;
	
	private int maxMessagesPerPoll = 10;
	
	private GDriveConfiguration configuration;
	
	private final HttpTransport httpTransport;
	
	private final JsonFactory jsonFactory;

    public GDriveEndpoint(String uri, GDriveComponent component) {
        super(uri, component);
	    httpTransport = new NetHttpTransport();
	    jsonFactory = new JacksonFactory();
    }

    public GDriveEndpoint(String uri, GDriveComponent comp, GDriveConfiguration configuration) {
    	super(uri, comp);
        this.configuration = configuration;
	    httpTransport = new NetHttpTransport();
	    jsonFactory = new JacksonFactory();
	}

	@Override
	public Producer createProducer() throws Exception {
        return new GDriveProducer(this);
    }

    @Override
	public Consumer createConsumer(Processor processor) throws Exception {
        GDriveConsumer consumer = new GDriveConsumer(this, processor);
        configureConsumer(consumer);
        consumer.setMaxMessagesPerPoll(maxMessagesPerPoll);
        return consumer;
    }

    @Override
	public boolean isSingleton() {
        return true;
    }

	public Drive getGDriveClient() {
        if (gDriveClient == null) {
        	gDriveClient = configuration.getGDriveClient() != null
                ? configuration.getGDriveClient() : createGDriveClient();
        }
        return gDriveClient;
	}
    
	public Drive getGDriveClient(String accessToken) {
        if (gDriveClient == null) {
        	gDriveClient = configuration.getGDriveClient() != null
                ? configuration.getGDriveClient() : createGDriveClient(accessToken);
        }
        return gDriveClient;
	}

	public void setGDriveClient(Drive gDriveClient) {
		this.gDriveClient = gDriveClient;
	}

	public GDriveConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(GDriveConfiguration configuration) {
		this.configuration = configuration;
	}
	
	private Drive createGDriveClient() {
	    String accessToken = getConfiguration().getAccessToken();
	    if (accessToken == null) {
	    	throw new IllegalArgumentException("Any AccessToken set");
	    }
	    return createGDriveClient(accessToken);
	}
	
	private Drive createGDriveClient(String accessToken) {
	    GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
	    return new Drive.Builder(httpTransport, jsonFactory, credential).build();
	}
	
	public GoogleTokenResponse refreshToken(String refreshToken) throws IOException {
		String clientId = getConfiguration().getClientId();
		String clientSecret = getConfiguration().getClientSecret();
		if (clientId == null || clientSecret == null) {
			return null;
		}
		
		if (refreshToken == null) {
			refreshToken = getConfiguration().getRefreshToken();
		}
		
		GoogleRefreshTokenRequest refresh = new GoogleRefreshTokenRequest(httpTransport, jsonFactory, 
				refreshToken, clientId, clientSecret);
		return refresh.execute();
	}


    public Exchange createExchange(File file) throws IOException {
        return createExchange(getExchangePattern(), file);
    }
	
    public Exchange createExchange(ExchangePattern pattern, File file) throws IOException {
        LOG.trace("Getting object with id [{}]...", file.getId());
        
        Exchange exchange = new DefaultExchange(this, pattern);
        Message message = exchange.getIn();
        message.setBody(downloadFile(getGDriveClient(), file));
        message.setHeader(GDriveConstants.E_TAG, file.getEtag());
        message.setHeader(GDriveConstants.CONTENT_TYPE, file.getMimeType());
        message.setHeader(GDriveConstants.DESCRIPTION, file.getDescription());
        message.setHeader(GDriveConstants.FILE_ID, file.getId());
        message.setHeader(GDriveConstants.TITLE, file.getTitle());
        
        message.setHeader(GDriveConstants.MD5_CHECKSUM, file.getMd5Checksum());
        message.setHeader(GDriveConstants.CONTENT_LENGTH, file.getFileSize());
        message.setHeader(GDriveConstants.LAST_MODIFIED, file.getModifiedDate());
        
        return exchange;
    }
    
    /**
     * Download a file's content.
     * 
     * @param service Drive API service instance.
     * @param file Drive File instance.
     * @return InputStream containing the file's content if successful,
     *         {@code null} otherwise.
     * @throws IOException 
     */
	protected static InputStream downloadFile(Drive service, File file) throws IOException {
		if (file.getDownloadUrl() != null && file.getDownloadUrl().length() > 0) {
			HttpResponse resp = service.getRequestFactory()
					.buildGetRequest(new GenericUrl(file.getDownloadUrl()))
					.execute();
			return resp.getContent();
		} else {
			// The file doesn't have any content stored on Drive.
			return null;
		}
	}
	
    public int getMaxMessagesPerPoll() {
        return maxMessagesPerPoll;
    }

    public void setMaxMessagesPerPoll(int maxMessagesPerPoll) {
        this.maxMessagesPerPoll = maxMessagesPerPoll;
    }
    
}
