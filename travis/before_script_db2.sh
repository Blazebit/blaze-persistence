#!/bin/bash -e

docker run --name db2 -e DB2INST1_PASSWORD=db2inst1-pwd -e LICENSE=accept -p50000:50000 -d ibmcom/db2express-c:10.5.0.5-3.10.0 db2start
# Give the container some time to start
sleep 5
docker exec -t db2 sudo -u db2inst1 /home/db2inst1/sqllib/bin/db2 create database test

docker cp db2:/home/db2inst1/sqllib/java/db2jcc4.jar db2jcc4.jar
docker cp db2:/home/db2inst1/sqllib/java/db2jcc_license_cu.jar db2jcc_license_cu.jar
mvn -q install:install-file -Dfile=db2jcc4.jar -DgroupId=com.ibm.db2 -DartifactId=db2jcc4 -Dversion=9.7 -Dpackaging=jar -DgeneratePom=true
mvn -q install:install-file -Dfile=db2jcc_license_cu.jar -DgroupId=com.ibm.db2 -DartifactId=db2jcc_license_cu -Dversion=9.7 -Dpackaging=jar -DgeneratePom=true
