#!/bin/bash
#
# Sets up environment for Blaze-Persistence backend MSSQL at travis-ci.com
#

docker run --shm-size=1536m --name oracle -d -p 1521:1521 quillbuilduser/oracle-18-xe