#!/bin/bash -e

sudo -u db2inst1 -i db2 "CREATE DATABASE TEST"
sudo -u db2inst1 -i db2 "ACTIVATE DATABASE TEST"