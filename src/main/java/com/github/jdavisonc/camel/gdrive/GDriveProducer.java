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

import java.io.InputStream;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultProducer;
import org.apache.camel.util.URISupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    		gDriveClient = getEndpoint().getGDriveClient(accessToken);
    	} else {
    		gDriveClient = getEndpoint().getGDriveClient();
    	}
    	
    	//Insert a file  
        File body = new File();
        body.setTitle(title);
        body.setDescription(description);
        body.setMimeType(contentType);
    	InputStreamContent mediaContent = new InputStreamContent(contentType, is);
    	
    	LOG.trace("Put file [{}] from exchange [{}]...", body, exchange);
    	
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

    @Override
    public String toString() {
        return "S3Producer[" + URISupport.sanitizeUri(getEndpoint().getEndpointUri()) + "]";
    }

    @Override
    public GDriveEndpoint getEndpoint() {
        return (GDriveEndpoint) super.getEndpoint();
    }
}
