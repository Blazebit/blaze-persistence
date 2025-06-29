#! /bin/bash

if command -v podman > /dev/null; then
  CONTAINER_CLI=$(command -v podman)
  HEALTCHECK_PATH="{{.State.Healthcheck.Status}}"
  # Only use sudo for podman
  if command -v sudo > /dev/null; then
    PRIVILEGED_CLI="sudo"
  else
    PRIVILEGED_CLI=""
  fi
else
  CONTAINER_CLI=$(command -v docker)
  HEALTCHECK_PATH="{{.State.Health.Status}}"
  PRIVILEGED_CLI=""
fi

mysql() {
  mysql_8_1
}

mysql_5_7() {
    $CONTAINER_CLI rm -f mysql || true
    $CONTAINER_CLI run --name mysql -e MYSQL_ALLOW_EMPTY_PASSWORD=yes -e MYSQL_DATABASE=test -p3306:3306 -d docker.io/mysql:5.7.43 --character-set-server=utf8mb4 --collation-server=utf8mb4_bin --skip-character-set-client-handshake --log-bin-trust-function-creators=1
    # Give the container some time to start
    OUTPUT=
    n=0
    until [ "$n" -ge 5 ]
    do
        # Need to access STDERR. Thanks for the snippet https://stackoverflow.com/a/56577569/412446
        { OUTPUT="$( { $CONTAINER_CLI logs mysql; } 2>&1 1>&3 3>&- )"; } 3>&1;
        if [[ $OUTPUT == *"ready for connections"* ]]; then
          break;
        fi
        n=$((n+1))
        echo "Waiting for MySQL to start..."
        sleep 3
    done
    if [ "$n" -ge 5 ]; then
      echo "MySQL failed to start and configure after 15 seconds"
    else
      echo "MySQL successfully started"
    fi
}

mysql_8_0() {
    $CONTAINER_CLI rm -f mysql || true
    $CONTAINER_CLI run --name mysql -e MYSQL_ALLOW_EMPTY_PASSWORD=yes -e MYSQL_DATABASE=test -p3306:3306 -d docker.io/mysql:8.0.31 --character-set-server=utf8mb4 --collation-server=utf8mb4_0900_as_cs --skip-character-set-client-handshake --log-bin-trust-function-creators=1
    # Give the container some time to start
    OUTPUT=
    n=0
    until [ "$n" -ge 5 ]
    do
        # Need to access STDERR. Thanks for the snippet https://stackoverflow.com/a/56577569/412446
        { OUTPUT="$( { $CONTAINER_CLI logs mysql; } 2>&1 1>&3 3>&- )"; } 3>&1;
        if [[ $OUTPUT == *"ready for connections"* ]]; then
          break;
        fi
        n=$((n+1))
        echo "Waiting for MySQL to start..."
        sleep 3
    done
    if [ "$n" -ge 5 ]; then
      echo "MySQL failed to start and configure after 15 seconds"
    else
      echo "MySQL successfully started"
    fi
}

mysql_8_1() {
    $CONTAINER_CLI rm -f mysql || true
    $CONTAINER_CLI run --name mysql -e MYSQL_ALLOW_EMPTY_PASSWORD=yes -e MYSQL_DATABASE=test -p3306:3306 -d docker.io/mysql:8.1.0 --character-set-server=utf8mb4 --collation-server=utf8mb4_0900_as_cs --skip-character-set-client-handshake --log-bin-trust-function-creators=1
    # Give the container some time to start
    OUTPUT=
    n=0
    until [ "$n" -ge 5 ]
    do
        # Need to access STDERR. Thanks for the snippet https://stackoverflow.com/a/56577569/412446
        { OUTPUT="$( { $CONTAINER_CLI logs mysql; } 2>&1 1>&3 3>&- )"; } 3>&1;
        if [[ $OUTPUT == *"ready for connections"* ]]; then
          break;
        fi
        n=$((n+1))
        echo "Waiting for MySQL to start..."
        sleep 3
    done
    if [ "$n" -ge 5 ]; then
      echo "MySQL failed to start and configure after 15 seconds"
    else
      echo "MySQL successfully started"
    fi
}

postgresql() {
  postgresql_15
}

postgresql_11() {
    $CONTAINER_CLI rm -f postgres || true
    $CONTAINER_CLI run --name postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=test -p5432:5432 -d docker.io/postgis/postgis:11-3.3
}

postgresql_13() {
    $CONTAINER_CLI rm -f postgres || true
    $CONTAINER_CLI run --name postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=test -p5432:5432 -d docker.io/postgis/postgis:13-3.1
}

postgresql_14() {
    $CONTAINER_CLI rm -f postgres || true
    $CONTAINER_CLI run --name postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=test -p5432:5432 -d docker.io/postgis/postgis:14-3.3
}

postgresql_15() {
    $CONTAINER_CLI rm -f postgres || true
    $CONTAINER_CLI run --name postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=test -p5432:5432 -d docker.io/postgis/postgis:15-3.3
}

db2() {
  db2_11_5
}

db2_11_5() {
    $PRIVILEGED_CLI $CONTAINER_CLI rm -f db2 || true
    $PRIVILEGED_CLI $CONTAINER_CLI run --name db2 --privileged -e DB2INST1_PASSWORD=db2inst1-pwd -e DBNAME=db2inst1 -e LICENSE=accept -e AUTOCONFIG=false -e ARCHIVE_LOGS=false -e TO_CREATE_SAMPLEDB=false -e REPODB=false -p 50000:50000 -d docker.io/ibmcom/db2:11.5.8.0
    # Give the container some time to start
    OUTPUT=
    while [[ $OUTPUT != *"INSTANCE"* ]]; do
        echo "Waiting for DB2 to start..."
        sleep 10
        OUTPUT=$($PRIVILEGED_CLI $CONTAINER_CLI logs db2 2>&1)
    done
    $PRIVILEGED_CLI $CONTAINER_CLI exec -t db2 su - db2inst1 bash -c ". /database/config/db2inst1/sqllib/db2profile; /database/config/db2inst1/sqllib/bin/db2 create database test &&
    /database/config/db2inst1/sqllib/bin/db2 'connect to test' &&
    /database/config/db2inst1/sqllib/bin/db2 'CREATE BUFFERPOOL BP8K pagesize 8K' &&
    /database/config/db2inst1/sqllib/bin/db2 'CREATE SYSTEM TEMPORARY TABLESPACE STB_8 PAGESIZE 8K BUFFERPOOL BP8K' &&
    /database/config/db2inst1/sqllib/bin/db2 'CREATE BUFFERPOOL BP16K pagesize 16K' &&
    /database/config/db2inst1/sqllib/bin/db2 'CREATE SYSTEM TEMPORARY TABLESPACE STB_16 PAGESIZE 16K BUFFERPOOL BP16K' &&
    /database/config/db2inst1/sqllib/bin/db2 'CREATE BUFFERPOOL BP32K pagesize 32K' &&
    /database/config/db2inst1/sqllib/bin/db2 'CREATE SYSTEM TEMPORARY TABLESPACE STB_32 PAGESIZE 32K BUFFERPOOL BP32K' &&
    /database/config/db2inst1/sqllib/bin/db2 'CREATE USER TEMPORARY TABLESPACE usr_tbsp MANAGED BY AUTOMATIC STORAGE'"
}

db2_10_5() {
    $PRIVILEGED_CLI $CONTAINER_CLI rm -f db2 || true
    # The sha represents the tag 10.5.0.5-3.10.0
    $PRIVILEGED_CLI $CONTAINER_CLI run --name db2 --privileged -e DB2INST1_PASSWORD=db2inst1-pwd -e LICENSE=accept -p 50000:50000 -d docker.io/ibmoms/db2express-c@sha256:a499afd9709a1f69fb41703e88def9869955234c3525547e2efc3418d1f4ca2b db2start
    # Give the container some time to start
    OUTPUT=
    while [[ $OUTPUT != *"DB2START"* ]]; do
        echo "Waiting for DB2 to start..."
        sleep 10
        OUTPUT=$($PRIVILEGED_CLI $CONTAINER_CLI logs db2 2>&1)
    done
    $PRIVILEGED_CLI $CONTAINER_CLI exec -t db2 su - db2inst1 bash -c "/home/db2inst1/sqllib/bin/db2 create database test &&
    /home/db2inst1/sqllib/bin/db2 'connect to test' &&
    /home/db2inst1/sqllib/bin/db2 'CREATE BUFFERPOOL BP8K pagesize 8K' &&
    /home/db2inst1/sqllib/bin/db2 'CREATE SYSTEM TEMPORARY TABLESPACE STB_8 PAGESIZE 8K BUFFERPOOL BP8K' &&
    /home/db2inst1/sqllib/bin/db2 'CREATE BUFFERPOOL BP16K pagesize 16K' &&
    /home/db2inst1/sqllib/bin/db2 'CREATE SYSTEM TEMPORARY TABLESPACE STB_16 PAGESIZE 16K BUFFERPOOL BP16K' &&
    /home/db2inst1/sqllib/bin/db2 'CREATE BUFFERPOOL BP32K pagesize 32K' &&
    /home/db2inst1/sqllib/bin/db2 'CREATE SYSTEM TEMPORARY TABLESPACE STB_32 PAGESIZE 32K BUFFERPOOL BP32K' &&
    /home/db2inst1/sqllib/bin/db2 'CREATE USER TEMPORARY TABLESPACE usr_tbsp MANAGED BY AUTOMATIC STORAGE'"
}

mssql() {
  mssql_2022
}

mssql_2017() {
    $CONTAINER_CLI rm -f mssql || true
    #This sha256 matches a specific tag of mcr.microsoft.com/mssql/server:2017-latest :
    $CONTAINER_CLI run --name mssql -d -p 1433:1433 -e "SA_PASSWORD=Blaze-Persistence" -e ACCEPT_EULA=Y mcr.microsoft.com/mssql/server@sha256:7d194c54e34cb63bca083542369485c8f4141596805611e84d8c8bab2339eede
    sleep 5
    echo "SQL Server successfully started"
}

mssql_2022() {
    $CONTAINER_CLI rm -f mssql || true
    #This sha256 matches a specific tag of mcr.microsoft.com/mssql/server:2022-latest :
    $CONTAINER_CLI run --name mssql -d -p 1433:1433 -e "SA_PASSWORD=Blaze-Persistence" -e ACCEPT_EULA=Y mcr.microsoft.com/mssql/server@sha256:b94071acd4612bfe60a73e265097c2b6388d14d9d493db8f37cf4479a4337480
    sleep 5
    echo "SQL Server successfully started"
}

oracle_setup() {
    HEALTHSTATUS=
    until [ "$HEALTHSTATUS" == "healthy" ];
    do
        echo "Waiting for Oracle to start..."
        sleep 5;
        # On WSL, health-checks intervals don't work for Podman, so run them manually
        if command -v podman > /dev/null; then
          $PRIVILEGED_CLI $CONTAINER_CLI healthcheck run oracle > /dev/null
        fi
        HEALTHSTATUS="`$PRIVILEGED_CLI $CONTAINER_CLI inspect -f $HEALTCHECK_PATH oracle`"
        HEALTHSTATUS=${HEALTHSTATUS##+( )} #Remove longest matching series of spaces from the front
        HEALTHSTATUS=${HEALTHSTATUS%%+( )} #Remove longest matching series of spaces from the back
    done
    sleep 2;
    echo "Oracle successfully started"
    # We increase file sizes to avoid online resizes as that requires lots of CPU which is restricted in XE
    $PRIVILEGED_CLI $CONTAINER_CLI exec oracle bash -c "source /home/oracle/.bashrc; bash -c \"
cat <<EOF | \$ORACLE_HOME/bin/sqlplus / as sysdba
set timing on
-- Remove DISABLE_OOB parameter from Listener configuration and restart it
!echo Enabling OOB for Listener...
!echo NAMES.DIRECTORY_PATH=\(EZCONNECT,TNSNAMES\) > /opt/oracle/oradata/dbconfig/XE/sqlnet.ora
!lsnrctl reload

-- Increasing redo logs
alter database add logfile group 4 '\$ORACLE_BASE/oradata/XE/redo04.log' size 500M reuse;
alter database add logfile group 5 '\$ORACLE_BASE/oradata/XE/redo05.log' size 500M reuse;
alter database add logfile group 6 '\$ORACLE_BASE/oradata/XE/redo06.log' size 500M reuse;
alter system switch logfile;
alter system switch logfile;
alter system switch logfile;
alter system checkpoint;
alter database drop logfile group 1;
alter database drop logfile group 2;
alter database drop logfile group 3;
!rm \$ORACLE_BASE/oradata/XE/redo01.log
!rm \$ORACLE_BASE/oradata/XE/redo02.log
!rm \$ORACLE_BASE/oradata/XE/redo03.log

-- Increasing SYSAUX data file
alter database datafile '\$ORACLE_BASE/oradata/XE/sysaux01.dbf' resize 600M;

-- Modifying database init parameters
alter system set open_cursors=1000 sid='*' scope=both;
alter system set session_cached_cursors=500 sid='*' scope=spfile;
alter system set db_securefile=ALWAYS sid='*' scope=spfile;
alter system set dispatchers='(PROTOCOL=TCP)(SERVICE=XEXDB)(DISPATCHERS=0)' sid='*' scope=spfile;
alter system set recyclebin=OFF sid='*' SCOPE=SPFILE;

-- Comment the 2 next lines to be able to use Diagnostics Pack features
alter system set sga_target=0m sid='*' scope=both;
-- alter system set statistics_level=BASIC sid='*' scope=spfile;

-- Restart the database
SHUTDOWN IMMEDIATE;
STARTUP MOUNT;
ALTER DATABASE OPEN;

-- Switch to the XEPDB1 pluggable database
alter session set container=xepdb1;

-- Modify XEPDB1 datafiles and tablespaces
alter database datafile '\$ORACLE_BASE/oradata/XE/XEPDB1/system01.dbf' resize 320M;
alter database datafile '\$ORACLE_BASE/oradata/XE/XEPDB1/sysaux01.dbf' resize 360M;
alter database datafile '\$ORACLE_BASE/oradata/XE/XEPDB1/undotbs01.dbf' resize 400M;
alter database datafile '\$ORACLE_BASE/oradata/XE/XEPDB1/undotbs01.dbf' autoextend on next 16M;
alter database tempfile '\$ORACLE_BASE/oradata/XE/XEPDB1/temp01.dbf' resize 400M;
alter database tempfile '\$ORACLE_BASE/oradata/XE/XEPDB1/temp01.dbf' autoextend on next 16M;
alter database datafile '\$ORACLE_BASE/oradata/XE/XEPDB1/users01.dbf' resize 100M;
alter database datafile '\$ORACLE_BASE/oradata/XE/XEPDB1/users01.dbf' autoextend on next 16M;
alter tablespace USERS nologging;
alter tablespace SYSTEM nologging;
alter tablespace SYSAUX nologging;
EOF\""
}

oracle_free_setup() {
    HEALTHSTATUS=
    until [ "$HEALTHSTATUS" == "healthy" ];
    do
        echo "Waiting for Oracle Free to start..."
        sleep 5;
        # On WSL, health-checks intervals don't work for Podman, so run them manually
        if command -v podman > /dev/null; then
          $PRIVILEGED_CLI $CONTAINER_CLI healthcheck run oracle > /dev/null
        fi
        HEALTHSTATUS="`$PRIVILEGED_CLI $CONTAINER_CLI inspect -f $HEALTCHECK_PATH oracle`"
        HEALTHSTATUS=${HEALTHSTATUS##+( )} #Remove longest matching series of spaces from the front
        HEALTHSTATUS=${HEALTHSTATUS%%+( )} #Remove longest matching series of spaces from the back
    done
    sleep 2;
    echo "Oracle successfully started"
    # We increase file sizes to avoid online resizes as that requires lots of CPU which is restricted in XE
    $PRIVILEGED_CLI $CONTAINER_CLI exec oracle bash -c "source /home/oracle/.bashrc; bash -c \"
cat <<EOF | \$ORACLE_HOME/bin/sqlplus / as sysdba
set timing on
-- Remove DISABLE_OOB parameter from Listener configuration and restart it
!echo Enabling OOB for Listener...
!echo NAMES.DIRECTORY_PATH=\(EZCONNECT,TNSNAMES\) > /opt/oracle/oradata/dbconfig/FREE/sqlnet.ora
!lsnrctl reload
-- Increasing redo logs
alter database add logfile group 4 '\$ORACLE_BASE/oradata/FREE/redo04.log' size 500M reuse;
alter database add logfile group 5 '\$ORACLE_BASE/oradata/FREE/redo05.log' size 500M reuse;
alter database add logfile group 6 '\$ORACLE_BASE/oradata/FREE/redo06.log' size 500M reuse;
alter system switch logfile;
alter system switch logfile;
alter system switch logfile;
alter system checkpoint;
alter database drop logfile group 1;
alter database drop logfile group 2;
alter database drop logfile group 3;
!rm \$ORACLE_BASE/oradata/FREE/redo01.log
!rm \$ORACLE_BASE/oradata/FREE/redo02.log
!rm \$ORACLE_BASE/oradata/FREE/redo03.log

-- Increasing SYSAUX data file
alter database datafile '\$ORACLE_BASE/oradata/FREE/sysaux01.dbf' resize 600M;

-- Modifying database init parameters
alter system set open_cursors=1000 sid='*' scope=both;
alter system set session_cached_cursors=500 sid='*' scope=spfile;
alter system set db_securefile=ALWAYS sid='*' scope=spfile;
alter system set dispatchers='(PROTOCOL=TCP)(SERVICE=FREEXDB)(DISPATCHERS=0)' sid='*' scope=spfile;
alter system set recyclebin=OFF sid='*' SCOPE=SPFILE;

-- Comment the 2 next lines to be able to use Diagnostics Pack features
alter system set sga_target=0m sid='*' scope=both;
-- alter system set statistics_level=BASIC sid='*' scope=spfile;

-- Restart the database
SHUTDOWN IMMEDIATE;
STARTUP MOUNT;
ALTER DATABASE OPEN;

-- Switch to the FREEPDB1 pluggable database
alter session set container=freepdb1;

-- Modify FREEPDB1 datafiles and tablespaces
alter database datafile '\$ORACLE_BASE/oradata/FREE/FREEPDB1/system01.dbf' resize 320M;
alter database datafile '\$ORACLE_BASE/oradata/FREE/FREEPDB1/sysaux01.dbf' resize 360M;
alter database datafile '\$ORACLE_BASE/oradata/FREE/FREEPDB1/undotbs01.dbf' resize 400M;
alter database datafile '\$ORACLE_BASE/oradata/FREE/FREEPDB1/undotbs01.dbf' autoextend on next 16M;
alter database tempfile '\$ORACLE_BASE/oradata/FREE/FREEPDB1/temp01.dbf' resize 400M;
alter database tempfile '\$ORACLE_BASE/oradata/FREE/FREEPDB1/temp01.dbf' autoextend on next 16M;
alter database datafile '\$ORACLE_BASE/oradata/FREE/FREEPDB1/users01.dbf' resize 100M;
alter database datafile '\$ORACLE_BASE/oradata/FREE/FREEPDB1/users01.dbf' autoextend on next 16M;
alter tablespace USERS nologging;
alter tablespace SYSTEM nologging;
alter tablespace SYSAUX nologging;
EOF\""
}

oracle_setup_old() {
    HEALTHSTATUS=
    until [ "$HEALTHSTATUS" == "healthy" ];
    do
        echo "Waiting for Oracle to start..."
        sleep 5;
        # On WSL, health-checks intervals don't work for Podman, so run them manually
        if command -v podman > /dev/null; then
          $PRIVILEGED_CLI $CONTAINER_CLI healthcheck run oracle > /dev/null
        fi
        HEALTHSTATUS="`$PRIVILEGED_CLI $CONTAINER_CLI inspect -f $HEALTCHECK_PATH oracle`"
        HEALTHSTATUS=${HEALTHSTATUS##+( )} #Remove longest matching series of spaces from the front
        HEALTHSTATUS=${HEALTHSTATUS%%+( )} #Remove longest matching series of spaces from the back
    done
    # We increase file sizes to avoid online resizes as that requires lots of CPU which is restricted in XE
    $PRIVILEGED_CLI $CONTAINER_CLI exec oracle bash -c "source /home/oracle/.bashrc; bash -c \"
cat <<EOF | \$ORACLE_HOME/bin/sqlplus / as sysdba
alter database tempfile '\$ORACLE_BASE/oradata/XE/temp.dbf' resize 400M;
alter database datafile '\$ORACLE_BASE/oradata/XE/system.dbf' resize 1000M;
alter database datafile '\$ORACLE_BASE/oradata/XE/sysaux.dbf' resize 700M;
alter database datafile '\$ORACLE_BASE/oradata/XE/undotbs1.dbf' resize 300M;
alter database add logfile group 4 '\$ORACLE_BASE/oradata/XE/redo04.log' size 500M reuse;
alter database add logfile group 5 '\$ORACLE_BASE/oradata/XE/redo05.log' size 500M reuse;
alter database add logfile group 6 '\$ORACLE_BASE/oradata/XE/redo06.log' size 500M reuse;

alter system switch logfile;
alter system switch logfile;
alter system switch logfile;
alter system checkpoint;

alter database drop logfile group 1;
alter database drop logfile group 2;
alter system set open_cursors=1000 sid='*' scope=both;
alter system set session_cached_cursors=500 sid='*' scope=spfile;
alter system set recyclebin=OFF sid='*' SCOPE=spfile;
alter system set processes=150 scope=spfile;
alter system set filesystemio_options=asynch scope=spfile;
alter system set disk_asynch_io=true scope=spfile;

shutdown immediate;
startup;
EOF\""
#  echo "Waiting for Oracle to restart after configuration..."
#  $CONTAINER_CLI stop oracle
#  $CONTAINER_CLI start oracle
#  HEALTHSTATUS=
#  until [ "$HEALTHSTATUS" == "healthy" ];
#  do
#      echo "Waiting for Oracle to start..."
#      sleep 5;
#      # On WSL, health-checks intervals don't work for Podman, so run them manually
#      if command -v podman > /dev/null; then
#        $CONTAINER_CLI healthcheck run oracle > /dev/null
#      fi
#      HEALTHSTATUS="`$CONTAINER_CLI inspect -f $HEALTCHECK_PATH oracle`"
#      HEALTHSTATUS=${HEALTHSTATUS##+( )} #Remove longest matching series of spaces from the front
#      HEALTHSTATUS=${HEALTHSTATUS%%+( )} #Remove longest matching series of spaces from the back
#  done
#  sleep 2;
  echo "Oracle successfully started"
}

disable_userland_proxy() {
  if [[ "$HEALTCHECK_PATH" == "{{.State.Health.Status}}" ]]; then
    if [[ ! -f /etc/docker/daemon.json ]]; then
      sudo service docker stop
      sudo sh -c "echo '{\"userland-proxy\": false}' > /etc/docker/daemon.json"
      sudo service docker start
    elif ! grep -q userland-proxy /etc/docker/daemon.json; then
      docker_daemon_json=$(</etc/docker/daemon.json)
      sudo service docker stop
      sudo sh -c "echo '${docker_daemon_json/\}/,}\"userland-proxy\": false}' > /etc/docker/daemon.json"
      sudo service docker start
    fi
  fi
}

oracle() {
  oracle_23
}

oracle_11() {
    $PRIVILEGED_CLI $CONTAINER_CLI rm -f oracle || true
    # We need to use the defaults
    # SYSTEM/Oracle18
    $PRIVILEGED_CLI $CONTAINER_CLI run --name oracle -d -p 1521:1521 -e ORACLE_PASSWORD=Oracle18 \
      --health-cmd healthcheck.sh \
      --health-interval 5s \
      --health-timeout 5s \
      --health-retries 10 \
      docker.io/gvenzl/oracle-xe:11.2.0.2-full
    oracle_setup_old
}

oracle_21() {
    $PRIVILEGED_CLI $CONTAINER_CLI rm -f oracle || true
    disable_userland_proxy
    # We need to use the defaults
    # SYSTEM/Oracle18
    $PRIVILEGED_CLI $CONTAINER_CLI run --name oracle -d -p 1521:1521 -e ORACLE_PASSWORD=Oracle18 \
       --cap-add cap_net_raw \
       --health-cmd healthcheck.sh \
       --health-interval 5s \
       --health-timeout 5s \
       --health-retries 10 \
       docker.io/gvenzl/oracle-xe:21.3.0-full
    oracle_setup
}

oracle_23() {
    $PRIVILEGED_CLI $CONTAINER_CLI rm -f oracle || true
    disable_userland_proxy
    # We need to use the defaults
    # SYSTEM/Oracle18
    $PRIVILEGED_CLI $CONTAINER_CLI run --name oracle -d -p 1521:1521 -e ORACLE_PASSWORD=Oracle18 \
       --health-cmd healthcheck.sh \
       --health-interval 5s \
       --health-timeout 5s \
       --health-retries 10 \
       ${DB_IMAGE_ORACLE_23:-docker.io/gvenzl/oracle-free:23}
    oracle_free_setup
}

if [ -z ${1} ]; then
    echo "No db name provided"
    echo "Provide one of:"
    echo -e "\tdb2"
    echo -e "\tdb2_11_5"
    echo -e "\tdb2_10_5"
    echo -e "\tmssql"
    echo -e "\tmssql_2022"
    echo -e "\tmssql_2017"
    echo -e "\tmysql"
    echo -e "\tmysql_8_1"
    echo -e "\tmysql_8_0"
    echo -e "\tmysql_5_7"
    echo -e "\toracle"
    echo -e "\toracle_21"
    echo -e "\toracle_11"
    echo -e "\tpostgresql"
    echo -e "\tpostgresql_15"
    echo -e "\tpostgresql_14"
    echo -e "\tpostgresql_13"
else
    ${1}
fi