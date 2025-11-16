#!/bin/bash
set -e
set -x

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

# Disable the downloading/downloaded message: https://stackoverflow.com/questions/21638697/disable-maven-download-progress-indication
export MAVEN_OPTS="$MAVEN_OPTS -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn"
# Increase retry count and reduce TTL for connections: https://github.com/actions/virtual-environments/issues/1499#issuecomment-718396233
export MAVEN_OPTS="$MAVEN_OPTS -Dmaven.wagon.httpconnectionManager.ttlSeconds=25 -Dmaven.wagon.http.retryHandler.count=3"
if [[ "$JDK" != "" ]]; then
  export MAVEN_OPTS="-Xmx1536m -XX:MaxMetaspaceSize=512m $MAVEN_OPTS"
else
  export MAVEN_OPTS="-Xmx1536m -XX:MaxPermSize=512m $MAVEN_OPTS"
fi

$DIR/mvnw -version

PROPERTIES="$PROPERTIES -Duser.country=US -Duser.language=en -Dmaven.javadoc.skip"

if [[ "$BUILD_JDK" != "" ]]; then
  if [[ "$BUILD_JDK" == *-ea ]]; then
    export BUILD_JDK=${BUILD_JDK::-3}
  fi
  PROPERTIES="$PROPERTIES -Dmain.java.version=$BUILD_JDK -Dtest.java.version=$BUILD_JDK"
  if [[ "$BUILD_JDK" == "21" ]] || [[ "$BUILD_JDK" == "25" ]] || [[ "$BUILD_JDK" == "26" ]]; then
    # Until Deltaspike releases a version with ASM 9.5, we have to exclude these parts from the build
    PROPERTIES="-pl !integration/deltaspike-data/testsuite $PROPERTIES"
  fi
fi

if [[ "$NATIVE" == "true" ]]; then
  exec $DIR/mvnw -B -P ${JPAPROVIDER},${RDBMS},${SPRING_DATA:-spring-data-3.5.x},${DELTASPIKE:-deltaspike-2.0},native${ADDITIONAL_PROFILES} clean install -am -V $PROPERTIES
else
  exec $DIR/mvnw -B -P ${JPAPROVIDER},${RDBMS},${SPRING_DATA:-spring-data-3.5.x},${DELTASPIKE:-deltaspike-2.0}${ADDITIONAL_PROFILES} clean install -am -V $PROPERTIES
fi