#!/bin/bash

if [ "$TRAVIS_REPO_SLUG" == "Blazebit/blaze-persistence" ] && 
    [ "$TRAVIS_BRANCH" == "master" ] &&
    [ "$TRAVIS_PULL_REQUEST" == "false" ] &&
    [ "$JPAPROVIDER" == "hibernate-5.2" ] &&
    [ "$RDBMS" == "h2" ] &&
    [ "$JDK" == "9" ] &&
    [ "$SNAPSHOT_PUBLISH" == "true" ]; then

  echo "Starting snapshot deployment..."
  export MAVEN_OPTS="$MAVEN_OPTS -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn"
  mvn -B -P blazebit-release -s .travis-settings.xml -DperformRelease -DskipTests -Dgpg.skip=true -Dquiet=true clean deploy
  echo "Snapshots deployed!"

else
  echo "Skipping snapshot deployment..."
fi
