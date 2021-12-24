#!/usr/bin/env bash

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
EOF

/usr/local/tomcat/bin/wait-for-it.sh ${DATABASE_HOST}
exec /usr/local/tomcat/bin/catalina.sh run
