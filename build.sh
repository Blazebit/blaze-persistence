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

if [ "$JDK" != "" ]; then
  export MAVEN_OPTS="-Xmx1024m -XX:MaxMetaspaceSize=512m" # --add-modules=java.se.ee"
else
  export MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=512m"
fi

if [ "$JDK" = "9" ]; then
  export JAVA_HOME="/usr/lib/jvm/java-9-oracle/"
fi

mvn -version

PROPERTIES="$PROPERTIES -Duser.country=US -Duser.language=en"

if [ "$BUILD_JDK" != "" ]; then
  PROPERTIES="$PROPERTIES -Djava.version=$BUILD_JDK"
fi

if [ "$JDK" != "" ]; then
  PROPERTIES="$PROPERTIES -Djdk8.home=/usr/lib/jvm/java-8-oracle"
fi

if [ "$TRAVIS_REPO_SLUG" == "Blazebit/blaze-persistence" ] && 
    [ "$TRAVIS_BRANCH" == "master" ] &&
    [ "$JPAPROVIDER" == "hibernate-5.2" ] &&
    [ "$RDBMS" == "h2" ]; then
  exec mvn -P ${JPAPROVIDER},${RDBMS},${SPRING_DATA:-spring-data-1.11.x},${DELTASPIKE:-deltaspike-1.7} clean install -V $PROPERTIES
else
  if [ "$TRAVIS_REPO_SLUG" == "Blazebit/blaze-persistence" ] &&
    [ "$TRAVIS_BRANCH" == "master" ] &&
    [ "$TRAVIS_PULL_REQUEST" == "false" ] &&
    [ "$JPAPROVIDER" == "hibernate-6.0" ] &&
    [ "$RDBMS" == "h2" ]; then
    # Just in case we want to run against a specific version
    #git clone --depth=1 --branch="wip/6.0" https://github.com/sebersole/hibernate-core.git hibernate6
    #cd hibernate6
    #./gradlew clean build publishToMavenLocal -x :documentation:buildDocs -x :documentation:aggregateJavadocs -x test -x findbugsMain -x findbugsTest -x checkStyleMain -x checkStyleTest
    : # do nothing right now
  fi
  
  eval exec mvn -P ${JPAPROVIDER},${RDBMS},${SPRING_DATA:-spring-data-1.11.x},${DELTASPIKE:-deltaspike-1.7} clean install --projects "core/testsuite,entity-view/testsuite,jpa-criteria/testsuite,integration/deltaspike-data/testsuite,integration/spring-data/testsuite,examples/spring-data-rest,examples/showcase/runner/spring,examples/showcase/runner/cdi" -am -V $PROPERTIES
fi
