FROM openjdk:10-jdk

COPY docker/web/tightblog-custom.properties tightblog-custom.properties

COPY docker/web/serverKeystore.jks serverKeystore.jks

COPY build/libs/tightblog-*.war app.war

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Dtightblog.custom.config=tightblog-custom.properties", "-jar","/app.war"]
