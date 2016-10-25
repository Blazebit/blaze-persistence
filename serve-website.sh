#!/bin/bash

mvn --projects website -am clean compile jbake:serve -Djbake.port=8820