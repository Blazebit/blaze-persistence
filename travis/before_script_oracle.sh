#!/bin/bash
#
# Sets up environment for Blaze-Persistence backend MSSQL at travis-ci.org
#

docker run --shm-size=1536m --name oracle -d -p 1521:1521 alexeiled/docker-oracle-xe-11g

docker cp oracle:/u01/app/oracle/product/11.2.0/xe/jdbc/lib/ojdbc6.jar ojdbc.jar
mvn install:install-file -Dfile=ojdbc.jar -DgroupId=com.oracle -DartifactId=ojdbc14 -Dversion=10.2.0.4.0 -Dpackaging=jar -DgeneratePom=true
