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

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.drive.Drive;

/**
 * Represents a GDrive endpoint.
 * 
 * @author Jorge Davison (jdavisonc)
 */
public class GDriveEndpoint extends DefaultEndpoint {
	
	private Drive gDriveClient;
	
	private GDriveConfiguration configuration;

    public GDriveEndpoint() {
    }

    public GDriveEndpoint(String uri, GDriveComponent component) {
        super(uri, component);
    }

    public GDriveEndpoint(String endpointUri) {
        super(endpointUri);
    }

    public GDriveEndpoint(String uri, GDriveComponent comp, GDriveConfiguration configuration) {
    	super(uri, comp);
        this.configuration = configuration;
	}

	@Override
	public Producer createProducer() throws Exception {
        return new GDriveProducer(this);
    }

    @Override
	public Consumer createConsumer(Processor processor) throws Exception {
        return new GDriveConsumer(this, processor);
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
	    HttpTransport httpTransport = new NetHttpTransport();
	    JsonFactory jsonFactory = new JacksonFactory();
		
		GoogleTokenResponse response = new GoogleTokenResponse();
		
	    GoogleCredential credential = new GoogleCredential().setFromTokenResponse(response);
	    
	    //Create a new authorized API client
	    return new Drive.Builder(httpTransport, jsonFactory, credential).build();
	}
    
}
