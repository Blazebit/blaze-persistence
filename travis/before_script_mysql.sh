#!/bin/bash
#
# Sets up environment for Blaze-Persistence backend MySQL8 at travis-ci.org
#

if [[ -z "${MYSQL_VERSION}" ]]; then
  mysql -e 'create database IF NOT EXISTS test DEFAULT CHARACTER SET utf8mb4 DEFAULT COLLATE utf8mb4_bin;'
else
  sudo service mysql stop
  docker run -d --name=mysqld -p 3306:3306 -e "MYSQL_DATABASE=test" -e "MYSQL_ALLOW_EMPTY_PASSWORD=yes" mysql:$MYSQL_VERSION  --character-set-server=utf8mb4 --collation-server=utf8mb4_bin
fi
