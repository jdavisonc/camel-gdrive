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

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultProducer;
import org.apache.camel.util.URISupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

/**
 * The GDrive producer.
 *
 * @author Jorge Davison (jdavisonc)
 */
public class GDriveProducer extends DefaultProducer {

    private static final transient Logger LOG = LoggerFactory.getLogger(GDriveProducer.class);

    public GDriveProducer(GDriveEndpoint endpoint) {
        super(endpoint);
    }

    @Override
	public void process(Exchange exchange) throws Exception {

    	InputStream is = exchange.getIn().getMandatoryBody(InputStream.class);
    	String contentType = exchange.getIn().getHeader(GDriveConstants.CONTENT_TYPE, String.class);
    	String description = exchange.getIn().getHeader(GDriveConstants.DESCRIPTION, String.class);
    	String title = exchange.getIn().getHeader(GDriveConstants.TITLE, String.class);
    	if (title == null) {
    		title = exchange.getIn().getHeader(Exchange.FILE_NAME_ONLY, String.class);
    	}
    	String accessToken = exchange.getIn().getHeader(GDriveConstants.ACCESS_TOKEN, String.class);

    	Drive gDriveClient = null;
    	if (accessToken != null) {
    		gDriveClient = getGDriveClient(accessToken);
    	} else {
    		throw new UnsupportedOperationException("Any AccessToken set");
    	}

    	//Insert a file
        File body = new File();
        body.setTitle(title);
        body.setDescription(description);
        body.setMimeType(contentType);
    	InputStreamContent mediaContent = new InputStreamContent(contentType, is);

    	try {
    		LOG.trace("Put file [{}] from exchange [{}]...", body, exchange);
    		uploadFile(exchange, gDriveClient, body, mediaContent);
		} catch (HttpResponseException e) {
			if (e.getStatusCode() == 401) {
				// Attempt to refresh token in case of failed request
				String newToken = refreshToken(exchange);
				if (newToken != null) {
					uploadFile(exchange, getGDriveClient(newToken), body, mediaContent);		
				} else {
					throw e;					
				}
			}
		}
    }

	private String refreshToken(Exchange exchange) {
		try {
			String refreshToken = exchange.getIn().getHeader(GDriveConstants.REFRESH_TOKEN, String.class);
			GoogleTokenResponse newToken = getEndpoint().refreshToken(refreshToken);
			return (newToken != null) ? newToken.getAccessToken() : null;
		} catch (IOException e) {
			LOG.trace("Refresh token request fails", e);
			return null;
		}
	}

	private void uploadFile(Exchange exchange, Drive gDriveClient, File body,
			InputStreamContent mediaContent) throws IOException {
		File file = gDriveClient.files().insert(body, mediaContent).execute();

		LOG.trace("Received result [{}]", file);

		Message message = getMessageForResponse(exchange);
		message.setHeader(GDriveConstants.E_TAG, file.getEtag());
		message.setHeader(GDriveConstants.FILE_ID, file.getId());
	}

    private Message getMessageForResponse(final Exchange exchange) {
        if (exchange.getPattern().isOutCapable()) {
            Message out = exchange.getOut();
            out.copyFrom(exchange.getIn());
            return out;
        }

        return exchange.getIn();
    }

    protected GDriveConfiguration getConfiguration() {
        return getEndpoint().getConfiguration();
    }
    
    protected Drive getGDriveClient(String accessToken) {
        return getEndpoint().getGDriveClient(accessToken);
    }

    @Override
    public String toString() {
        return "GDriveProducer[" + URISupport.sanitizeUri(getEndpoint().getEndpointUri()) + "]";
    }

    @Override
    public GDriveEndpoint getEndpoint() {
        return (GDriveEndpoint) super.getEndpoint();
    }
}
