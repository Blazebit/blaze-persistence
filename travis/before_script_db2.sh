#!/bin/bash -e

docker run --name db2 --privileged -e DB2INST1_PASSWORD=db2inst1-pwd -e DBNAME=test -e LICENSE=accept -p 50000:50000 -d ibmcom/db2:11.5.0.0a

# Give the container some time to start
OUTPUT=
while [[ $OUTPUT != *"Setup has completed"* ]]; do
    echo "Waiting for DB2 to start..."
    sleep 10
    OUTPUT=$(docker logs db2)
done

docker exec -t db2 su - db2inst1 bash -c ". /database/config/db2inst1/sqllib/db2profile && /database/config/db2inst1/sqllib/bin/db2 'connect to test' && /database/config/db2inst1/sqllib/bin/db2 'CREATE USER TEMPORARY TABLESPACE usr_tbsp MANAGED BY AUTOMATIC STORAGE'"

docker cp db2:/database/config/db2inst1/sqllib/java/db2jcc4.jar db2jcc4.jar
docker cp db2:/database/config/db2inst1/sqllib/java/db2jcc_license_cu.jar db2jcc_license_cu.jar
mvn -q install:install-file -Dfile=db2jcc4.jar -DgroupId=com.ibm.db2 -DartifactId=db2jcc4 -Dversion=9.7 -Dpackaging=jar -DgeneratePom=true
mvn -q install:install-file -Dfile=db2jcc_license_cu.jar -DgroupId=com.ibm.db2 -DartifactId=db2jcc_license_cu -Dversion=9.7 -Dpackaging=jar -DgeneratePom=true
