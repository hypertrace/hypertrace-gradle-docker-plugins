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

# Remove trailing colon if present
FIPS_CLASSPATH="\${FIPS_CLASSPATH%:}"
# If FIPS_CLASSPATH is not empty, prepend it to classpath with colon as delim
CLASSPATH="\${FIPS_CLASSPATH:+\${FIPS_CLASSPATH}:}/app/resources:/app/classes:/app/localLibs/*:/app/orgLibs/*:/app/externalLibs/*"
echo "Using the classpath \$CLASSPATH"
exec java ${defaultJvmOpts.substring(1, defaultJvmOpts.length()-1)} \$JAVA_OPTS \$FIPS_JAVA_OPTS -classpath \${CLASSPATH} ${mainClassName} \$@
