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