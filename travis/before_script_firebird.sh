#!/bin/bash -e

isql-fb -z -q -i /dev/null # --version
echo 'CREATE DATABASE "LOCALHOST:/tmp/test.fdb" PAGE_SIZE = 16384;' > /tmp/create_test.sql
isql-fb -u SYSDBA -p masterkey -i /tmp/create_test.sql -q
cat /tmp/create_test.sql