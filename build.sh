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
if [ "$JDK" != "" ]; then
  export MAVEN_OPTS="-Xmx1024m -XX:MaxMetaspaceSize=512m $MAVEN_OPTS"
else
  export MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=512m $MAVEN_OPTS"
fi

$DIR/mvnw -version

PROPERTIES="$PROPERTIES -Duser.country=US -Duser.language=en -Dmaven.javadoc.skip"

if [ "$BUILD_JDK" != "" ]; then
  if [[ "$BUILD_JDK" == *-ea ]]; then
    export BUILD_JDK=${BUILD_JDK::-3}
  fi
  PROPERTIES="$PROPERTIES -Djava.version=$BUILD_JDK -Dtest.java.version=$BUILD_JDK -Djdk8.home=$JDK8_HOME"
#  if [ "$BUILD_JDK" == "16" ]; then
#    # Until Deltaspike releases a version with ASM 9, we have to exclude these parts from the build
#    PROPERTIES="-pl !integration/deltaspike-data/testsuite,!examples/deltaspike-data-rest,!integration/quarkus/deployment,!integration/quarkus/runtime,!examples/quarkus/testsuite/base,!examples/quarkus/base $PROPERTIES"
#  else
    # Unfortunately we have to exclude quarkus tests with Java 9+ due to the MR-JAR issue: https://github.com/quarkusio/quarkus/issues/13892
  #PROPERTIES="-pl !integration/quarkus/deployment,!integration/quarkus/runtime,!examples/quarkus/testsuite/base,!examples/quarkus/base $PROPERTIES"
#  fi
#else
  #if [ "$JPAPROVIDER" == "hibernate-5.2" ] && [ "$JDK" != "8" ]; then
    # Unfortunately we have to exclude quarkus tests with Java 9+ due to the MR-JAR issue: https://github.com/quarkusio/quarkus/issues/13892
    #PROPERTIES="-pl !integration/quarkus/deployment,!integration/quarkus/runtime,!examples/quarkus/testsuite/base,!examples/quarkus/base $PROPERTIES"
  #fi
fi

if [ "$JDK" != "" ]; then
  if [[ "$JDK" == *-ea ]]; then
    export JDK=${JDK::-3}
  fi
  PROPERTIES="$PROPERTIES -Djdk8.home=$JDK8_HOME"
fi

#if { [ "$JPAPROVIDER" == "hibernate-5.2" ] || [ "$JPAPROVIDER" == "hibernate-apt" ]; } &&
#    [ "$RDBMS" == "h2" ]; then
#  exec $DIR/mvnw -B -P ${JPAPROVIDER},${RDBMS},${SPRING_DATA:-spring-data-1.11.x},${DELTASPIKE:-deltaspike-1.7} clean install -V $PROPERTIES
#else
#  PROJECT_LIST="core/testsuite,entity-view/testsuite,jpa-criteria/testsuite,integration/deltaspike-data/testsuite,integration/jaxrs,integration/spring-data/testsuite/webflux,integration/spring-data/testsuite/webmvc,examples/spring-data-webmvc,examples/spring-data-webflux,examples/showcase/runner/spring,examples/showcase/runner/cdi,integration/querydsl/testsuite,integration/spring-hateoas/webmvc"
#  if [ "$JPAPROVIDER" == "hibernate-6.0" ] &&
#    [ "$RDBMS" == "h2" ]; then
#    # Just in case we want to run against a specific version
##    cd ..
##    git clone --depth=1 --branch="bp-integration" https://github.com/beikov/hibernate-orm.git hibernate6
##    cd hibernate6
##    ./gradlew -Dmaven.repo.local=$HOME/.m2/repository build publishToMavenLocal -x :documentation:buildDocs -x :hibernate-core:javadoc -x :hibernate-jpamodelgen:javadoc -x :documentation:aggregateJavadocs -x test -x javadoc -x checkStyleMain -x checkStyleTest
##    cd -
##    cd ../blaze-persistence
#    PROJECT_LIST="$PROJECT_LIST,core/testsuite-jakarta-runner,entity-view/testsuite-jakarta-runner,jpa-criteria/testsuite-jakarta-runner"
#    exec $DIR/mvnw -B -P ${JPAPROVIDER},${RDBMS},${SPRING_DATA:-spring-data-1.11.x},${DELTASPIKE:-deltaspike-1.7} clean install --projects $PROJECT_LIST -am -V $PROPERTIES
#  elif [ "$JPAPROVIDER" == "hibernate-5.4" ]; then
#    PROJECT_LIST="$PROJECT_LIST,integration/quarkus/deployment,examples/quarkus/testsuite/base"
#    if [ "$NATIVE" == "true" ]; then
#      if [ "$RDBMS" == "mysql8" ]; then
#        PROJECT_LIST="$PROJECT_LIST,examples/quarkus/testsuite/native/mysql"
#      else
#        PROJECT_LIST="$PROJECT_LIST,examples/quarkus/testsuite/native/$RDBMS"
#      fi
#      exec $DIR/mvnw -B -P ${JPAPROVIDER},${RDBMS},${SPRING_DATA:-spring-data-1.11.x},${DELTASPIKE:-deltaspike-1.7},native clean install --projects $PROJECT_LIST -am -V $PROPERTIES
#    else
#      exec $DIR/mvnw -B -P ${JPAPROVIDER},${RDBMS},${SPRING_DATA:-spring-data-1.11.x},${DELTASPIKE:-deltaspike-1.7} clean install --projects $PROJECT_LIST -am -V $PROPERTIES
#    fi
#  else
#    exec $DIR/mvnw -B -P ${JPAPROVIDER},${RDBMS},${SPRING_DATA:-spring-data-1.11.x},${DELTASPIKE:-deltaspike-1.7} clean install --projects $PROJECT_LIST -am -V $PROPERTIES
#  fi
#fi

if [ "$NATIVE" == "true" ]; then
  exec $DIR/mvnw -B -P ${JPAPROVIDER},${RDBMS},${SPRING_DATA:-spring-data-1.11.x},${DELTASPIKE:-deltaspike-1.7},native clean install -am -V $PROPERTIES
else
  exec $DIR/mvnw -B -P ${JPAPROVIDER},${RDBMS},${SPRING_DATA:-spring-data-1.11.x},${DELTASPIKE:-deltaspike-1.7} clean install -am -V $PROPERTIES
fi