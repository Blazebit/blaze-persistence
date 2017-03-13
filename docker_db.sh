#! /bin/sh

mysql_5_6() {
    docker rm -f mysql || true
    docker run --name mysql -e MYSQL_ALLOW_EMPTY_PASSWORD=yes -e MYSQL_DATABASE=test -p3306:3306 -d mysql:5.6.25 --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
}

mysql_5_7() {
    docker rm -f mysql || true
    docker run --name mysql -e MYSQL_ALLOW_EMPTY_PASSWORD=yes -e MYSQL_DATABASE=test -p3306:3306 -d mysql:5.7 --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
}

db2() {
    docker rm -f db2 || true
    docker run --name db2 -e DB2INST1_PASSWORD=db2inst1-pwd -e LICENSE=accept -p50000:50000 -d ibmcom/db2express-c:10.5.0.5-3.10.0 db2start
    # Give the container some time to start
    sleep 5
    docker exec -t db2 sudo -u db2inst1 /home/db2inst1/sqllib/bin/db2 create database test
	
	#docker cp db2:/home/db2inst1/sqllib/java/db2jcc4.jar db2jcc4.jar
	#mvn -q install:install-file -Dfile=db2jcc4.jar -DgroupId=com.ibm.db2 -DartifactId=db2jcc4 -Dversion=9.7 -Dpackaging=jar -DgeneratePom=true
	#rm db2jcc4.jar
	#docker cp db2:/home/db2inst1/sqllib/java/db2jcc_license_cu.jar db2jcc_license_cu.jar
	#mvn -q install:install-file -Dfile=db2jcc_license_cu.jar -DgroupId=com.ibm.db2 -DartifactId=db2jcc_license_cu -Dversion=9.7 -Dpackaging=jar -DgeneratePom=true
	#rm db2jcc_license_cu.jar
}

mssql() {
    docker rm -f mssql || true
    docker run --name mssql -d -p 1433:1433 -e "SA_PASSWORD=Blaze-Persistence" -e ACCEPT_EULA=Y microsoft/mssql-server-linux
}

oracle() {
    docker rm -f oracle || true
    docker run --shm-size=1536m --name oracle -d -p 1521:1521 alexeiled/docker-oracle-xe-11g
}

if [ -z ${1} ]; then
    echo "No db name provided"
    echo "Provide one of:"
    echo -e "\tmysql_5_6"
    echo -e "\tmysql_5_7"
    echo -e "\tdb2"
    echo -e "\tmssql"
    echo -e "\toracle"
else
    ${1}
fi