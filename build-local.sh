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
	echo "JPA provider (hibernate-6.6, hibernate-6.4, hibernate-6.2, eclipselink) [hibernate-6.6]: "
	read input </dev/tty
	
	if [ "$input" == "" ]; then
		JPA_PROVIDER="hibernate-6.6"
	else
		JPA_PROVIDER=$input
	fi
fi

if [ "$DBMS" == "" ]; then
	echo "DBMS (h2, mysql, postgresql, db2, oracle, mssql) [h2]: "
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
	eval exec ./mvnw -P "$PROFILES" --projects "core/testsuite,entity-view/testsuite,jpa-criteria/testsuite" -am clean test $PROPERTIES
else
	eval exec ./mvnw -P "$PROFILES" clean test $PROPERTIES
fi
