#!/bin/bash -e

sudo bash -c 'echo "deb http://archive.canonical.com/ubuntu precise partner" >> /etc/apt/sources.list'
sudo apt-get update -qq
sudo apt-get install -y db2exc

echo "Running db2profile and db2rmln"
sudo /bin/sh -c '. ~db2inst1/sqllib/db2profile ; $DB2DIR/cfg/db2rmln'

echo "Setting up db2 users"
sudo usermod --password $(echo "db2inst1" | openssl passwd -1 -stdin) db2inst1
sudo usermod --password $(echo "db2fenc1" | openssl passwd -1 -stdin) db2fenc1
sudo usermod --password $(echo "dasusr1" | openssl passwd -1 -stdin) dasusr1

echo "Enable MySQL compatibility"
sudo -u db2inst1 -i /opt/ibm/db2/V9.7/adm/db2set DB2_COMPATIBILITY_VECTOR=MYS
sudo chmod +x /opt/ibm/db2/V9.7/adm/db2stop
sudo -u db2inst1 -i /opt/ibm/db2/V9.7/adm/db2stop
sudo chmod +x /opt/ibm/db2/V9.7/adm/db2start
sudo -u db2inst1 -i /opt/ibm/db2/V9.7/adm/db2start


mvn -q install:install-file -Dfile=/opt/ibm/db2/V9.7/java/db2jcc4.jar -DgroupId=com.ibm.db2 -DartifactId=db2jcc4 -Dversion=9.7 -Dpackaging=jar -DgeneratePom=true 
mvn -q install:install-file -Dfile=/opt/ibm/db2/V9.7/java/db2jcc_license_cu.jar -DgroupId=com.ibm.db2 -DartifactId=db2jcc_license_cu -Dversion=9.7 -Dpackaging=jar -DgeneratePom=true
