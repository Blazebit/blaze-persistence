@echo off

if "%1"=="" (
    echo "No db name provided"
    echo "Provide one of:"
    echo -e "\tmysql_5_6"
    echo -e "\tmysql_5_7"
    echo -e "\tdb2"
    echo -e "\tmssql"
    echo -e "\toracle"
) else (
    call :%1
)
EXIT /B

:mysql_5_6
docker rm -f mysql || true
docker run --name mysql -e MYSQL_ALLOW_EMPTY_PASSWORD=yes -e MYSQL_DATABASE=test -p3306:3306 -d mysql:5.6.25 --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
goto:EOF

:mysql_5_7
docker rm -f mysql || true
docker run --name mysql -e MYSQL_ALLOW_EMPTY_PASSWORD=yes -e MYSQL_DATABASE=test -p3306:3306 -d mysql:5.7 --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
goto:EOF

:db2
docker rm -f db2 || true
docker run --name db2 -e DB2INST1_PASSWORD=db2inst1-pwd -e LICENSE=accept -p50000:50000 -d ibmcom/db2express-c:10.5.0.5-3.10.0 db2start
REM Give the container some time to start
ping -n 5 127.0.0.1 >nul
docker exec -t db2 sudo -u db2inst1 /home/db2inst1/sqllib/bin/db2 create database test
goto:EOF

:mssql
docker rm -f mssql || true
docker run --name mssql -d -p 1433:1433 -e "SA_PASSWORD=Blaze-Persistence" -e ACCEPT_EULA=Y microsoft/mssql-server-linux
goto:EOF

:oracle
docker rm -f oracle || true
docker run --shm-size=1536m --name oracle -d -p 1521:1521 alexeiled/docker-oracle-xe-11g
goto:EOF