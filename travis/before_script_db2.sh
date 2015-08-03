#!/bin/bash -e

sudo -u db2inst1 -i db2 "CREATE DATABASE TEST"
sudo -u db2inst1 -i db2 "ACTIVATE DATABASE TEST"
export ADDITIONAL_PROPERTIES="-Djdbc.user=db2inst1 -Djdbc.password=db2inst1"