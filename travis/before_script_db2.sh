#!/bin/bash -e

sudo -u db2inst1 -i db2 "CREATE DATABASE TEST"
sudo -u db2inst1 -i db2 "ACTIVATE DATABASE TEST"
export ADDITIONAL_PROPERTIES="-Djdbc.user=dasusr1 -Djdbc.password=dasusr1"