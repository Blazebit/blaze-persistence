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
if [ "$JDK" != "" ]; then
  export MAVEN_OPTS="-Xmx1024m -XX:MaxMetaspaceSize=512m $MAVEN_OPTS"
else
  export MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=512m $MAVEN_OPTS"
fi

if [ "$JDK" = "9" ]; then
  export JAVA_HOME="/usr/lib/jvm/java-9-oracle/"
fi

mvn -version

PROPERTIES="$PROPERTIES -Duser.country=US -Duser.language=en"

if [ "$BUILD_JDK" != "" ]; then
  PROPERTIES="$PROPERTIES -Djava.version=$BUILD_JDK -Dtest.java.version=$BUILD_JDK"
fi

if [ "$JDK" != "" ]; then
  PROPERTIES="$PROPERTIES -Djdk8.home=/usr/lib/jvm/java-8-oracle"
fi

if [ [ "$JPAPROVIDER" == "hibernate-5.2" ] || [ "$JPAPROVIDER" == "hibernate-apt" ] ] &&
    [ "$RDBMS" == "h2" ]; then
  exec mvn -B -P ${JPAPROVIDER},${RDBMS},${SPRING_DATA:-spring-data-1.11.x},${DELTASPIKE:-deltaspike-1.7} clean install -V $PROPERTIES
else
  PROJECT_LIST="core/testsuite,entity-view/testsuite,jpa-criteria/testsuite,integration/deltaspike-data/testsuite,integration/jaxrs,integration/spring-data/testsuite/webflux,integration/spring-data/testsuite/webmvc,examples/spring-data-webmvc,examples/spring-data-webflux,examples/showcase/runner/spring,examples/showcase/runner/cdi,integration/querydsl/blaze-persistence-querydsl-testsuite"
  if [ "$JPAPROVIDER" == "hibernate-6.0" ] &&
    [ "$RDBMS" == "h2" ]; then
    # Just in case we want to run against a specific version
    #git clone --depth=1 --branch="wip/6.0" https://github.com/sebersole/hibernate-core.git hibernate6
    #cd hibernate6
    #./gradlew clean build publishToMavenLocal -x :documentation:buildDocs -x :documentation:aggregateJavadocs -x test -x findbugsMain -x findbugsTest -x checkStyleMain -x checkStyleTest
    : # do nothing right now
  elif [ "$JPAPROVIDER" == "hibernate-5.4" ]; then
    PROJECT_LIST="$PROJECT_LIST,integration/quarkus/deployment,examples/quarkus/testsuite/base"
    if [ "$NATIVE" == "true" ]; then
      if [ "$RDBMS" == "mysql8" ]; then
        PROJECT_LIST="$PROJECT_LIST,examples/quarkus/testsuite/native/mysql"
      else
        PROJECT_LIST="$PROJECT_LIST,examples/quarkus/testsuite/native/$RDBMS"
      fi
      exec mvn -B -P ${JPAPROVIDER},${RDBMS},${SPRING_DATA:-spring-data-1.11.x},${DELTASPIKE:-deltaspike-1.7},native clean install --projects $PROJECT_LIST -am -V $PROPERTIES
    else
      exec mvn -B -P ${JPAPROVIDER},${RDBMS},${SPRING_DATA:-spring-data-1.11.x},${DELTASPIKE:-deltaspike-1.7} clean install --projects $PROJECT_LIST -am -V $PROPERTIES
    fi
  else
    exec mvn -B -P ${JPAPROVIDER},${RDBMS},${SPRING_DATA:-spring-data-1.11.x},${DELTASPIKE:-deltaspike-1.7} clean install --projects $PROJECT_LIST -am -V $PROPERTIES
  fi
fi
