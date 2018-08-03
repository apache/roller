FROM tomcat:8.5-jre8

# Two TightBlog dependencies that are loaded into the Tomcat lib folder.
ADD http://central.maven.org/maven2/org/postgresql/postgresql/42.2.4/postgresql-42.2.4.jar \
    http://central.maven.org/maven2/javax/mail/javax.mail-api/1.6.1/javax.mail-api-1.6.1.jar /usr/local/tomcat/lib/
# Replacing image server.xml with one configured to use SSL. See https://hub.docker.com/r/unidata/tomcat-docker/,
# "HTTP over SSL" section (although this image does not use the Unidata image).  You'll need to provide
# the web/ssl.crt and web/ssl.key files, see that section for an OpenSSL command that can be used.
# Note self-signed certificates bring up browser warnings, getting a key from a certificate authority instead is preferred
COPY ./docker/web/server.xml ./docker/web/ssl.crt ./docker/web/ssl.key /usr/local/tomcat/conf/
COPY ./docker/web/tightblog-custom.properties /usr/local/tomcat/lib/
COPY ./build/libs/tightblog*.war /usr/local/tomcat/webapps/tightblog.war

CMD ["catalina.sh", "run"]
