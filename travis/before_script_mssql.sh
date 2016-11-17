#!/bin/bash
#
# Sets up environment for Blaze-Persistence backend MSSQL at travis-ci.org
#

docker run -d -p 1433:1433 -e "SA_PASSWORD=Blaze-Persistence" -e ACCEPT_EULA=Y microsoft/mssql-server-linux