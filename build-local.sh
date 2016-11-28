#!/bin/bash
set -e

DB_HOST=${DOCKER_MACHINE_IP:-192.168.99.100}
ORACLE_USER=${ORACLE_USER:-SYSTEM}
ORACLE_PASSWORD=${ORACLE_PASSWORD:-oracle}

JPA_PROVIDER="$1"
DBMS="$2"
BUILD="$3"
PROPERTIES=

if [ "$JPA_PROVIDER" == "" ]; then
	echo "JPA provider (hibernate-4.2, hibernate-4.3, hibernate-5.0, hibernate-5.1, hibernate-5.2, datanucleus-4, datanucleus-5, eclipselink, openjpa) [hibernate-5.2]: "
	read input </dev/tty
	
	if [ "$input" == "" ]; then
		JPA_PROVIDER="hibernate-5.2"
	elif [ "$input" == "hibernate-4.2" ]; then
		JPA_PROVIDER="hibernate"
	else
		JPA_PROVIDER=$input
	fi
fi

if [ "$DBMS" == "" ]; then
	echo "DBMS (h2, mysql, postgresql, sqlite, db2, firebird, oracle, mssql) [h2]: "
	read input </dev/tty
	
	if [ "$input" == "" ]; then
		DBMS="h2"
	elif [ "$input" == "oracle" ]; then
		DBMS=$input
		PROPERTIES="-Djdbc.url=jdbc:oracle:thin:@$DB_HOST:1521/xe -Djdbc.user=$ORACLE_USER -Djdbc.password=$ORACLE_PASSWORD"
	elif [ "$input" == "mssql" ]; then
		DBMS=$input
		PROPERTIES="-Djdbc.url=jdbc:sqlserver://$DB_HOST:1433"
	else
		DBMS=$input
	fi
fi

PROFILES="$JPA_PROVIDER,$DBMS"

if [ "$BUILD" == "" ]; then
	echo "BUILD (full, test) [full]: "
	read input </dev/tty
	
	if [ "$input" == "" ]; then
		BUILD="full"
	else
		BUILD=$input
	fi
fi

PROPERTIES="$PROPERTIES -Duser.country=US -Duser.language=en"

if [ "$BUILD" == "test" ]; then
	eval exec mvn -P "$PROFILES" --projects "core/testsuite,entity-view/testsuite,jpa-criteria/testsuite" -am clean test $PROPERTIES
else
	eval exec mvn -P "$PROFILES" clean test $PROPERTIES
fi
