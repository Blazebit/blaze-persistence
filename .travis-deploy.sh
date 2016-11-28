#!/bin/bash

if [ "$TRAVIS_REPO_SLUG" == "Blazebit/blaze-persistence" ] && 
    [ "$TRAVIS_BRANCH" == "master" ] &&
    [ "$TRAVIS_PULL_REQUEST" == "false" ] &&
    [ "$JPAPROVIDER" == "hibernate" ] &&
    [ "$RDBMS" == "h2" ]; then

  echo "Starting snapshot deployment..."
  mvn -s .travis-settings.xml -DperformRelease -DskipTests -Dgpg.skip=true -Dquiet=true deploy
  echo "Snapshots deployed!"

else
  echo "Skipping snapshot deployment..."
fi
