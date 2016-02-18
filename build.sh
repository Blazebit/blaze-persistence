#!/bin/bash
set -e

if [ "$JDK" = "9" ]; then
  MVN_BIN=/tmp/apache-maven/bin/mvn
  export MAVEN_OPTS="-Xmx1024m -XX:MaxMetaspaceSize=512m"
elif [ "$LATEST_MAVEN" = "true" ]; then
  MVN_BIN=/tmp/apache-maven/bin/mvn
  export MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=512m"
else
  MVN_BIN=mvn
  export MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=512m"
fi

if [ "$JDK" = "9" ]; then
  export JAVA_HOME="/usr/lib/jvm/java-9-oracle/"
fi

${MVN_BIN} -version
exec ${MVN_BIN} -P ${JPAPROVIDER},${RDBMS} -X install
