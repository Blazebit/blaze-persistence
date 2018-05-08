#!/bin/bash

if [ "$1" == "" ]; then
	echo "Stage (staging, prod) [staging]: "
	read input </dev/tty
	
	if [ "$input" == "" ]; then
		STAGE="staging"
	elif [ "$input" == "prod" ]; then
		STAGE="blazebit-release"
	else
		STAGE=$input
	fi
else
	if [ "$1" == "prod" ]; then
		STAGE="blazebit-release"
	else
		STAGE=$1
	fi
fi

if [ "$STAGE" == "staging" ]; then
	REPO=staging-persistence.blazebit.com
elif [ "$STAGE" == "blazebit-release" ]; then
	REPO=persistence.blazebit.com
else
	echo "Invalid stage: $STAGE"
	exit 1
fi

mvn -P $STAGE --projects documentation -am clean compile
mvn -P $STAGE --projects documentation site:deploy -DrepositoryId=$REPO