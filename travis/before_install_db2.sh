#!/bin/bash -e

sudo bash -c 'echo "deb http://archive.canonical.com/ubuntu precise partner" >> /etc/apt/sources.list'
sudo apt-get update -qq
sudo apt-get install -y db2exc

echo "Running db2profile and db2rmln"
sudo /bin/sh -c '. ~db2inst1/sqllib/db2profile ; $DB2DIR/cfg/db2rmln'

echo "Setting up db2 users"
echo -e "db2admin\ndb2admin" | sudo passwd db2admin

mvn -q install:install-file -Dfile=/opt/ibm/db2/V9.7/java/db2jcc4.jar -DgroupId=com.ibm.db2 -DartifactId=db2jcc4 -Dversion=9.7 -Dpackaging=jar -DgeneratePom=true 
mvn -q install:install-file -Dfile=/opt/ibm/db2/V9.7/java/db2jcc_license_cu.jar -DgroupId=com.ibm.db2 -DartifactId=db2jcc_license_cu -Dversion=9.7 -Dpackaging=jar -DgeneratePom=true
