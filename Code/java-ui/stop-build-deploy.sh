#!/bin/sh
# Use the following export line in case maven reports too little memory during execution
# export MAVEN_OPTS='-Xmx1024m -Xms256m -XX:MaxPermSize=512m'
echo After startup, the user GUI will be accessible at http://localhost:9093/user-ui
echo To test the GUI for a presentation transaction, go to http://localhost:9093/user-ui?mode=presentation
echo To test the GUI for an issuance transaction, go to http://localhost:9093/user-ui?mode=issuance
mvn -pl eu.abc4trust.ri.ui.user.product jetty:stop
mvn clean install
mvn -pl eu.abc4trust.ri.ui.user.product jetty:deploy-war
