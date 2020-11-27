#! /bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

java -version

if [ "$NATIVE" = "true" ]; then
  sudo add-apt-repository -y ppa:jonathonf/python-3.6
  sudo apt-get update
  sudo apt-get install -y python3-pip
  yes | sudo pip3 install git+https://github.com/naftulikay/travis-pls
  source setup-graalvm.sh
fi

if [ "$RDBMS" == 'mysql' ]; then
  sudo service mysql stop
  bash $DIR/../docker_db.sh mysql_5_7
elif [ "$RDBMS" == 'mysql8' ]; then
  sudo service mysql stop
  bash $DIR/../docker_db.sh mysql_8_0
elif [ "$RDBMS" == 'postgresql' ]; then
  sudo service postgres stop
  bash $DIR/../docker_db.sh postgresql_9_5
elif [ "$RDBMS" == 'db2' ]; then
  bash $DIR/../docker_db.sh db2
elif [ "$RDBMS" == 'oracle' ]; then
  bash $DIR/../docker_db.sh oracle
elif [ "$RDBMS" == 'mssql' ]; then
  bash $DIR/../docker_db.sh mssql
fi

exec bash $DIR/../build.sh