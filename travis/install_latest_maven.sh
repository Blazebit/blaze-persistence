#!/bin/bash -e

# Download and install newest maven
mkdir /tmp/apache-maven
curl -fSL http://apache.mirrors.ovh.net/ftp.apache.org/dist/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz -o maven.tar.gz \
    && tar -xvf maven.tar.gz -C /tmp/apache-maven --strip-components=1 \
    && rm maven.tar.gz*