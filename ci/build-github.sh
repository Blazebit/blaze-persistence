#! /bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

java -version

if [ "$RDBMS" == 'mysql' ]; then
  bash $DIR/../docker_db.sh mysql
elif [ "$RDBMS" == 'postgresql' ]; then
  bash $DIR/../docker_db.sh postgresql
elif [ "$RDBMS" == 'db2' ]; then
  bash $DIR/../docker_db.sh db2
elif [ "$RDBMS" == 'oracle' ]; then
  bash $DIR/../docker_db.sh oracle
elif [ "$RDBMS" == 'mssql' ]; then
  bash $DIR/../docker_db.sh mssql
fi

exec bash $DIR/../build.sh