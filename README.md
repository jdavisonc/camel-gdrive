Camel Google Drive Component
============

Camel component for integrate with Google Drive

To build this project use

    mvn install

For more help see the Apache Camel documentation:

    http://camel.apache.org/writing-components.html
    
## Authentication

This component use Offline OAuth 2.0 for Web Server Applications, you should follow the following instructions to get an authentication code: https://developers.google.com/accounts/docs/OAuth2WebServer.
The component doesn't provide the authentication process, it assumes that the token given is valid.

## How to use

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
			public void configure() {
                from("file://local/path")
                  .process(MyProcessor())
                  .to("gdrive://");
            }
        };
    }

## Message Headers

| Header                    | Description           |
| ------------------------- |:---------------------:|
| CamelGDriveContentType    |                       |
| CamelGDriveDescription    |                       |
| CamelGDriveTitle          |                       |
| CamelGDriveETag           |                       |
| CamelGDriveFileId         |                       |
| CamelGDriveAccessToken    |                       |
| CamelGDriveRefreshToken   |                       |

## Links

* [Google Drive API](https://code.google.com/p/google-api-java-client/wiki/APIs#Drive_API)
* [Google Drive from Java](https://developers.google.com/drive/quickstart-java)

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