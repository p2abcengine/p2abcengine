@ECHO OFF
set MAVEN_OPTS=-Xmx1024m -Xms256m -XX:MaxPermSize=512m

call mvn %*
