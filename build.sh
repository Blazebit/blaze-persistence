#!/bin/bash
set -e

if [ "$JDK" = "9" ]; then
  MVN_BIN=/tmp/apache-maven/bin/mvn
  export MAVEN_OPTS="-Xmx1024m -XX:MaxMetaspaceSize=512m" # --add-modules=java.se.ee"
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

PROPERTIES="$PROPERTIES -Duser.country=US -Duser.language=en"

if [ "$TRAVIS_REPO_SLUG" == "Blazebit/blaze-persistence" ] && 
    [ "$TRAVIS_BRANCH" == "master" ] &&
    [ "$JPAPROVIDER" == "hibernate-5.2" ] &&
    [ "$RDBMS" == "h2" ]; then
  exec ${MVN_BIN} -P ${JPAPROVIDER},${RDBMS},${SPRING_DATA:-spring-data-1.11.x} install
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
  
  eval exec ${MVN_BIN} -P ${JPAPROVIDER},${RDBMS},${SPRING_DATA:-spring-data-1.11.x} install --projects "core/testsuite,entity-view/testsuite,jpa-criteria/testsuite" -am $PROPERTIES
fi
