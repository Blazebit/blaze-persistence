#!/bin/bash

mvn -P "local-serve" --projects website -am clean compile -Djbake.port=8820