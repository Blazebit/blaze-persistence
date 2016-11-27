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
	STAGE=$1
fi

mvn -P $STAGE --projects website -am clean compile