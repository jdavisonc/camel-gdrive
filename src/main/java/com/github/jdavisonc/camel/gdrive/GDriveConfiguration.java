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

import com.google.api.services.drive.Drive;

/**
 * 
 * @author Jorge Davison (jdavisonc)
 */
public class GDriveConfiguration implements Cloneable {
	
	private Drive gDriveClient;
	
	private String accessToken;
	
	private String refreshToken;

	private String clientId;
	
	private String clientSecret;
	
    private boolean deleteAfterRead = true;
	
	public Drive getGDriveClient() {
		return gDriveClient;
	}

	public void setGDriveClient(Drive gDriveClient) {
		this.gDriveClient = gDriveClient;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public boolean isDeleteAfterRead() {
		return deleteAfterRead;
	}

	public void setDeleteAfterRead(boolean deleteAfterRead) {
		this.deleteAfterRead = deleteAfterRead;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	
}
