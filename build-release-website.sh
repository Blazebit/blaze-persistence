#!/bin/bash

if [ "$1" == "" ]; then
	echo "Stage (local, prod, staging) [local]: "
	read input </dev/tty
	
	if [ "$input" == "" ]; then
		STAGE="local"
	elif [ "$input" == "prod" ]; then
		STAGE="blazebit-release"
	elif [ "$input" == "staging" ]; then
		STAGE="staging"
	else
		STAGE=$input
	fi
else
	STAGE=$1
fi

mvn -P $STAGE --projects website -am clean compile