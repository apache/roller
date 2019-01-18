# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  The ASF licenses this file to You
# under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.  For additional information regarding
# copyright in this work, please see the NOTICE file in the top level
# directory of this distribution.

FROM tomcat:8.0-jre8-alpine

ARG STORAGE_ROOT=/var/lib/roller/data
ARG DATABASE_JDBC_DRIVERCLASS=com.mysql.jdbc.Driver
ARG DATABASE_JDBC_CONNECTIONURL=mysql://localhost:3306/rollerdb?autoReconnect=true&useUnicode=true&characterEncoding=utf-8&mysqlEncoding=utf8
ARG DATABASE_JDBC_USERNAME=scott
ARG DATABASE_JDBC_PASSWORD=tiger


# get Roller from Apache mirror

WORKDIR /usr/local/roller
RUN wget -O roller.tar.gz http://www-us.apache.org/dist/roller/roller-5.1/v5.1.2/roller-release-5.1.2-standard.tar.gz
RUN tar -xf roller.tar.gz
RUN ln -s roller-release-*/ release

# install Roller WAR as ROOT.war, create data dirs

WORKDIR /usr/local/roller
RUN rm -rf /usr/local/tomcat/webapps/*
RUN cp release/webapp/roller.war /usr/local/tomcat/webapps/ROOT.war
RUN mkdir -p data/mediafiles data/searchindex

# download PostgreSQL and MySQL drivers plus Mail and Activation JARs

WORKDIR /usr/local/tomcat/lib
RUN wget -O mysql.jar http://repo2.maven.org/maven2/mysql/mysql-connector-java/5.1.43/mysql-connector-java-5.1.43.jar
RUN wget http://repo2.maven.org/maven2/javax/mail/mail/1.4.1/mail-1.4.1.jar
RUN wget http://repo2.maven.org/maven2/javax/activation/activation/1.1.1/activation-1.1.1.jar

# start Tomcat

ADD entry-point.sh /usr/local/tomcat/bin
RUN chgrp -R 0 /usr/local/tomcat
RUN chmod -R g+rw /usr/local/tomcat

WORKDIR /usr/local/tomcat
ENTRYPOINT /usr/local/tomcat/bin/entry-point.sh
