#!/usr/bin/env bash

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

cat > /usr/local/tomcat/lib/roller-custom.properties << EOF
installation.type=auto
mediafiles.storage.dir=${STORAGE_ROOT}/roller_mediafiles
search.index.dir=${STORAGE_ROOT}/roller_searchindex
log4j.appender.roller.File=/usr/local/tomcat/logs/roller.log
database.configurationType=jdbc
database.jdbc.driverClass=${DATABASE_JDBC_DRIVERCLASS}
database.jdbc.connectionURL=${DATABASE_JDBC_CONNECTIONURL}
database.jdbc.username=${DATABASE_JDBC_USERNAME}
database.jdbc.password=${DATABASE_JDBC_PASSWORD}
authentication.method=${AUTHENTICATION_METHOD:-db}
EOF

# Register an OIDC provider only when one is configured. The issuer must be
# reachable under the same URL from both this container and the browser,
# because the browser is redirected to the endpoints Roller discovers here.
if [ -n "${OIDC_ISSUER_URI}" ]; then
cat >> /usr/local/tomcat/lib/roller-custom.properties << EOF
oidc.${OIDC_PROVIDER_ID:-keycloak}.client-id=${OIDC_CLIENT_ID}
oidc.${OIDC_PROVIDER_ID:-keycloak}.client-secret=${OIDC_CLIENT_SECRET}
oidc.${OIDC_PROVIDER_ID:-keycloak}.issuer-uri=${OIDC_ISSUER_URI}
oidc.${OIDC_PROVIDER_ID:-keycloak}.client-name=${OIDC_CLIENT_NAME:-Keycloak}
users.oidc.autoProvision.enabled=${OIDC_AUTO_PROVISION:-true}
EOF
fi

# Turn off the JVM's hardware-accelerated Base64 decoding, which has an
# out-of-bounds write on x86-64 processors with AVX-512 in JDKs older than the
# November 2026 updates (CVE-2026-114938). It only costs Base64 speed; an
# image built on a patched JDK can drop it. It goes before any CATALINA_OPTS
# passed to the container, so those options add to it.
export CATALINA_OPTS="-XX:-UseBASE64Intrinsics ${CATALINA_OPTS}"

/usr/local/tomcat/bin/wait-for-it.sh ${DATABASE_HOST}
exec /usr/local/tomcat/bin/catalina.sh run
