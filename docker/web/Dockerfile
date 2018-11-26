FROM openjdk:11-jdk

COPY docker/web/application-tbcustom.properties application-tbcustom.properties

COPY docker/web/serverKeystore.jks serverKeystore.jks

COPY build/libs/tightblog-*.war app.war

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Dspring.config.location=classpath:/application.properties,application-tbcustom.properties","-jar","/app.war"]
