#!/bin/bash
set -e
set -x

if [ "$TRAVIS_REPO_SLUG" == "Blazebit/blaze-persistence" ] && 
    [ "$TRAVIS_BRANCH" == "master" ] &&
    [ "$TRAVIS_PULL_REQUEST" == "false" ] &&
    [ "$SNAPSHOT_PUBLISH" == "true" ]; then

  echo "Starting snapshot deployment..."
  export MAVEN_OPTS="$MAVEN_OPTS -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn"
  exec mvn -P blazebit-release -s .travis-settings.xml -DperformRelease -DskipTests -DskipITs -Dgpg.skip=true -Dquiet=true -Djdk8.home=/usr/lib/jvm/java-8-oracle clean deploy
else
  echo "Skipping snapshot deployment..."
fi
