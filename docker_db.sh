#! /bin/sh

mysql_5_6() {
    docker rm -f mysql || true
    docker run --name mysql -e MYSQL_ALLOW_EMPTY_PASSWORD=yes -e MYSQL_DATABASE=test -p3306:3306 -d mysql:5.6.25 --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
}

mysql_5_7() {
    docker rm -f mysql || true
    docker run --name mysql -e MYSQL_ALLOW_EMPTY_PASSWORD=yes -e MYSQL_DATABASE=test -p3306:3306 -d mysql:5.7 --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
}

mysql_8_0() {
    docker rm -f mysql || true
    docker run --name mysql -e MYSQL_ALLOW_EMPTY_PASSWORD=yes -e MYSQL_DATABASE=test -p3306:3306 -d mysql:8.0 --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
}

db2() {
    docker rm -f db2 || true
    docker run --name db2 --privileged -e DB2INST1_PASSWORD=db2inst1-pwd -e DBNAME=testdb -e LICENSE=accept -p 50000:50000 -d ibmcom/db2:11.5.0.0a
    # Give the container some time to start
    OUTPUT=
    while [[ $OUTPUT != *"Setup has completed"* ]]; do
        echo "Waiting for DB2 to start..."
        sleep 10
        OUTPUT=$(docker logs db2)
    done
    docker exec -t db2 su - db2inst1 bash -c ". /database/config/db2inst1/sqllib/db2profile && /database/config/db2inst1/sqllib/bin/db2 'connect to test' && /database/config/db2inst1/sqllib/bin/db2 'CREATE USER TEMPORARY TABLESPACE usr_tbsp MANAGED BY AUTOMATIC STORAGE'"

	#docker cp db2:/database/config/db2inst1/sqllib/java/db2jcc4.jar db2jcc4.jar
	#mvn -q install:install-file -Dfile=db2jcc4.jar -DgroupId=com.ibm.db2 -DartifactId=db2jcc4 -Dversion=9.7 -Dpackaging=jar -DgeneratePom=true
	#rm db2jcc4.jar
	#docker cp db2:/database/config/db2inst1/sqllib/java/db2jcc_license_cu.jar db2jcc_license_cu.jar
	#mvn -q install:install-file -Dfile=db2jcc_license_cu.jar -DgroupId=com.ibm.db2 -DartifactId=db2jcc_license_cu -Dversion=9.7 -Dpackaging=jar -DgeneratePom=true
	#rm db2jcc_license_cu.jar
}

mssql() {
    docker rm -f mssql || true
    docker run --name mssql -d -p 1433:1433 -e "SA_PASSWORD=Blaze-Persistence" -e ACCEPT_EULA=Y microsoft/mssql-server-linux
}

oracle() {
    docker rm -f oracle || true
    docker run --shm-size=1536m --name oracle -d -p 1521:1521 wnameless/oracle-xe-11g
}

if [ -z ${1} ]; then
    echo "No db name provided"
    echo "Provide one of:"
    echo -e "\tmysql_5_6"
    echo -e "\tmysql_5_7"
    echo -e "\tmysql_8_0"
    echo -e "\tdb2"
    echo -e "\tmssql"
    echo -e "\toracle"
else
    ${1}
fi