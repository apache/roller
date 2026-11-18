
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.


# Example Dockerfile for containerizing Roller


# STAGE 1 - BUILD ------------------------------------------------

FROM maven:3.9.16-eclipse-temurin-21 AS builder

COPY . /project
WORKDIR /project
RUN mvn -Duser.home=/builder/home -DskipTests=true -B clean install


# STAGE 2 - PACKAGE ------------------------------------------------

FROM tomcat:11.0.29-jdk21-temurin

# Remove existing Tomcat webapps

RUN rm -rf /usr/local/tomcat/webapps/*

# Add Roller configuration to environment

ARG STORAGE_ROOT=/usr/local/tomcat/data
ARG DATABASE_JDBC_DRIVERCLASS=org.postgresql.Driver
ARG DATABASE_JDBC_CONNECTIONURL=jdbc:postgresql://postgresql/rollerdb
ARG DATABASE_JDBC_USERNAME=scott
ARG DATABASE_JDBC_PASSWORD=tiger
ARG DATABASE_HOST=postgresql:5432

ENV STORAGE_ROOT ${STORAGE_ROOT}
ENV DATABASE_JDBC_DRIVERCLASS ${DATABASE_JDBC_DRIVERCLASS}
ENV DATABASE_JDBC_CONNECTIONURL ${DATABASE_JDBC_CONNECTIONURL}
ENV DATABASE_JDBC_USERNAME ${DATABASE_JDBC_USERNAME}
ENV DATABASE_JDBC_PASSWORD ${DATABASE_JDBC_PASSWORD}
ENV DATABASE_HOST ${DATABASE_HOST}

# install Roller WAR as ROOT.war, create data dirs

WORKDIR /usr/local/roller
COPY --from=builder /project/app/target/roller.war /usr/local/tomcat/webapps/ROOT.war
RUN mkdir -p data/mediafiles data/searchindex

# download PostgreSQL and MySQL drivers plus Mail and Activation JARs

WORKDIR /usr/local/tomcat/lib
RUN apt-get update && apt-get install -y wget
RUN wget -O postgresql.jar https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.13/postgresql-42.7.13.jar
RUN wget https://repo1.maven.org/maven2/org/eclipse/angus/angus-mail/2.0.5/angus-mail-2.0.5.jar
RUN wget https://repo1.maven.org/maven2/jakarta/mail/jakarta.mail-api/2.1.5/jakarta.mail-api-2.1.5.jar
RUN wget https://repo1.maven.org/maven2/jakarta/activation/jakarta.activation-api/2.1.4/jakarta.activation-api-2.1.4.jar
RUN wget https://repo1.maven.org/maven2/org/eclipse/angus/angus-activation/2.0.3/angus-activation-2.0.3.jar

# Turn off the JVM's hardware-accelerated Base64 decoding, which has an
# out-of-bounds write on x86-64 processors with AVX-512 in JDKs older than the
# November 2026 updates (CVE-2026-114938). It only costs Base64 speed; an
# image built on a patched JDK can drop it.
ENV CATALINA_OPTS="-XX:-UseBASE64Intrinsics"

# Add Roller entry-point and go!

COPY docker/entry-point.sh /usr/local/tomcat/bin
COPY docker/wait-for-it.sh /usr/local/tomcat/bin
RUN chgrp -R 0 /usr/local/tomcat
RUN chmod -R g+rw /usr/local/tomcat

WORKDIR /usr/local/tomcat
CMD [ "/usr/local/tomcat/bin/entry-point.sh" ]
