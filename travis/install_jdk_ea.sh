#!/usr/bin/env bash
set -e

# Adapted from https://sormuras.github.io/blog/2017-12-08-install-jdk-on-travis.html

TMP=$(curl -L jdk.java.net/${JDK})
TMP="${TMP#*Most recent build: jdk-${JDK}-ea+}" # remove everything before the number
TMP="${TMP%%<*}"                                        # remove everything after the number
JDK_BUILD="$(echo "${TMP}" | tr -d '[:space:]')"     # remove all whitespace

JDK_ARCHIVE=jdk-${JDK}-ea+${JDK_BUILD}_linux-x64_bin.tar.gz

cd ~
wget http://download.java.net/java/jdk${JDK}/archive/${JDK_BUILD}/BCL/${JDK_ARCHIVE}
tar -xzf ${JDK_ARCHIVE}
export JAVA_HOME=~/jdk-${JDK}
export PATH=${JAVA_HOME}/bin:$PATH
cd -

java -version