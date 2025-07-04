#!/bin/sh
#
# Copyright 2020 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

##############################################################################
##
##  ${applicationName} start up script for UN*X
##
##############################################################################

set -e

FIPS_CLASSPATH=""
if [ "\$FIPS_ENABLED" = "true" ]; then
  FIPS_CLASSPATH="/usr/share/java/bc-fips/*"
  echo "Adding the additional FIPS libs to the classpath"
  if [ -z "\$FIPS_JAVA_OPTS" ]; then
    FIPS_JAVA_OPTS="--add-exports=java.base/sun.security.internal.spec=ALL-UNNAMED \
      --add-exports=java.base/sun.security.provider=ALL-UNNAMED \
      -Djava.security.properties==\$JAVA_HOME/conf/security/java.security.fips \
      -Djavax.net.ssl.trustStore=\$JAVA_HOME/lib/security/cacerts-bcfks  \
      -Djavax.net.ssl.trustStoreType=BCFKS \
      -Djavax.net.ssl.trustStorePassword=changeit \
      -Djavax.net.ssl.trustStoreProvider=BCFIPS"
    echo "Using default FIPS_JAVA_OPTS"
  fi
fi

exec java ${defaultJvmOpts.substring(1, defaultJvmOpts.length()-1)} \$JAVA_OPTS \$FIPS_JAVA_OPTS -classpath "\${FIPS_CLASSPATH}:/app/resources:/app/classes:/app/localLibs/*:/app/orgLibs/*:/app/externalLibs/*" ${mainClassName} \$@
