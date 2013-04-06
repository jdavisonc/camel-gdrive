Camel Google Drive Component
============

Camel component for integrate with Google Drive

This Project is a template of a Camel component.

When you create a component project, you need to move the META-INF/services/org/apache/camel/component/${name}
file to META-INF/services/org/apache/camel/component/foo where "foo" is the URI scheme for your component and any
related endpoints created on the fly.

To build this project use

    mvn install

For more help see the Apache Camel documentation:

    http://camel.apache.org/writing-components.html
    
Authentication
===

This component use Offline OAuth 2.0 for Web Server Applications, you should follow the following instructions to get an authentication code: https://developers.google.com/accounts/docs/OAuth2WebServer.
The component doesn't provide the authentication process, it assumes that the token given is valid.

How to use
===

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

Message Headers
===

| Header                    | Description           |
| ------------------------- |:---------------------:|
| CamelGDriveContentType    |                       |
| CamelGDriveDescription    |                       |
| CamelGDriveTitle          |                       |
| CamelGDriveETag           |                       |
| CamelGDriveFileId         |                       |
| CamelGDriveAccessToken    |                       |
| CamelGDriveRefreshToken   |                       |

Links
===

* [Google Drive API](https://code.google.com/p/google-api-java-client/wiki/APIs#Drive_API)
* [Google Drive from Java](https://developers.google.com/drive/quickstart-java)