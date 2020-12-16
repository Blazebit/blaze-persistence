#!/bin/bash
set -e
set -x

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

echo "Starting snapshot deployment..."
export MAVEN_OPTS="$MAVEN_OPTS -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn"
exec $DIR/../mvnw -B -P "blazebit-release,spring-data-1.11.x,deltaspike-1.7,hibernate-5.2" -s $DIR/deploy-settings.xml -DperformRelease -DskipTests -DskipITs -Dgpg.skip=true -Dquiet=true -Djdk8.home=$JDK8_HOME clean deploy
