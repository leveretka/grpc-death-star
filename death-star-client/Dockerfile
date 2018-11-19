FROM tomcat:9.0.5-jre8
RUN rm -rf /usr/local/tomcat/webapps/*
COPY build/libs/death-star-client-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war
VOLUME /usr/local/tomcat/logs