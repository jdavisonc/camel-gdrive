Camel Google Drive Component
============

The Google Drive component supports storing objetcs to Google Drive service.
    
## URI Format

    gdrive://mydrive[?options]

You can append query options to the URI in the following format, ?options=value&option2=value&...

## URI Options

| Name            | Default Value | Context | Description                                                         |
| ----------------|:------------- |:--------|:-------------------------------------------------------------------:|
| gDriveClient    | null          | Shared  | Reference to a com.google.api.services.drive.Drive in the Registry. |
| accessToken     | null          | Shared  | Google Drive access token                                           |
| refreshToken    | null          | Shared  | Google Drive refresh token                                          |
| clientId        | null          | Shared  | Application id to connect to Google Drive                           |
| clientSecret    | null          | Shared  | Application secret key to connect to Google Drive                   |
| deleteAfterRead | true          | Shared  | Delete files from google drive after read                           |

## Authentication

This component use Offline OAuth 2.0 for Web Server Applications, you should follow the following instructions to get an authentication code: https://developers.google.com/accounts/docs/OAuth2WebServer.
The component doesn't provide the authentication process, it assumes that the token given is valid.

In case a clientId and clientSecret were configured, a refresh token request is made by the component. Enabling a expired token request still alive and sucess without throwing an unauthorized exception.

## How to use with DSL

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
			public void configure() {
                from("file://local/path")
                  .process(MyProcessor())
                  .to("gdrive://mydrive");
            }
        };
    }

## How to use with XML

    <route>
      <from uri="file://local/path" />
      <process ref="myProcessor" />
      <to uri="gdrive://mydrive" />
    </route>


## Message Headers

| Header                    | Description                                               |
| ------------------------- |:---------------------------------------------------------:|
| CamelGDriveContentType    | Content type of the file                                  |
| CamelGDriveDescription    | Description for the file that will appear in Google Drive |
| CamelGDriveTitle          | Title of the file in Google Drive (the name)              |
| CamelGDriveETag           | ETag for file                                             |
| CamelGDriveFileId         | File identifier given by Google Drive                     |
| CamelGDriveAccessToken    | Access token for Google Drive                             |
| CamelGDriveRefreshToken   | Refresh token for Google Drive                            |
| CamelGDriveMD5            | Content MD5 checksum                                      |
| CamelGDriveContentLength  | Content length of the file                                |
| CamelGDriveLastModified   | File last modified date                                   |

## Dependencies

Maven users will need to add the following dependency to their pom.xml.

    <dependency>
        <groupId>com.github.jdavisonc</groupId>
        <artifactId>camel-gdrive</artifactId>
        <version>${gdrive-version}</version>
    </dependency>

where ${gdriveversion} must be replaced by the actual version (1.0-SNAPSHOT or higher).

## Links

* [Google API Java Client](https://code.google.com/p/google-api-java-client/wiki)
* [Google Drive API](https://developers.google.com/drive)

## Author

* Jorge Davison - [@jdavisonc](http://twitter.com/jdavisonc)

## License

Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
