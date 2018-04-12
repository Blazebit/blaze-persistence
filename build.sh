#!/bin/bash
set -e

if [ "$JDK" != "" ]; then
  MVN_BIN=/tmp/apache-maven/bin/mvn
  export MAVEN_OPTS="-Xmx1024m -XX:MaxMetaspaceSize=512m" # --add-modules=java.se.ee"
elif [ "$LATEST_MAVEN" = "true" ]; then
  MVN_BIN=/tmp/apache-maven/bin/mvn
  export MAVEN_OPTS="-Xmx1024m -XX:MaxMetaspaceSize=512m" # --add-modules=java.se.ee"
else
  MVN_BIN=mvn
  export MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=512m"
fi

if [ "$JDK" = "9" ]; then
  export JAVA_HOME="/usr/lib/jvm/java-9-oracle/"
fi

${MVN_BIN} -version

PROPERTIES="$PROPERTIES -Duser.country=US -Duser.language=en"

if [ "$BUILD_JDK" != "" ]; then
  PROPERTIES="$PROPERTIES -Dmaven.compiler.target=$BUILD_JDK -Dmaven.compiler.source=$BUILD_JDK"
fi

if [ "$TRAVIS_REPO_SLUG" == "Blazebit/blaze-persistence" ] && 
    [ "$TRAVIS_BRANCH" == "master" ] &&
    [ "$JPAPROVIDER" == "hibernate-5.2" ] &&
    [ "$RDBMS" == "h2" ]; then
  exec ${MVN_BIN} -P ${JPAPROVIDER},${RDBMS},${SPRING_DATA:-spring-data-1.11.x},${DELTASPIKE:-deltaspike-1.7} install
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
  
  eval exec ${MVN_BIN} -P ${JPAPROVIDER},${RDBMS},${SPRING_DATA:-spring-data-1.11.x},${DELTASPIKE:-deltaspike-1.7} install --projects "core/testsuite,entity-view/testsuite,jpa-criteria/testsuite,integration/deltaspike-data/testsuite,integration/spring-data/testsuite,examples/spring-data-rest,examples/showcase/runner/spring,examples/showcase/runner/cdi" -am $PROPERTIES
fi
