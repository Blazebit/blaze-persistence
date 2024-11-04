#!/bin/bash
GRAALVM_VERSION=17.0.9

sudo apt-key adv --keyserver keyserver.ubuntu.com --recv-keys 6B05F25D762E3157 78BD65473CB3BD13
sudo apt-get update
sudo apt-get -y install build-essential libz-dev zlib1g-dev
curl -LJ "https://github.com/graalvm/graalvm-ce-builds/releases/download/jdk-${GRAALVM_VERSION}/graalvm-community-jdk-${GRAALVM_VERSION}_linux-x64_bin.tar.gz" --output graalvm.tar.gz
tar -xzf graalvm.tar.gz
export GRAALVM_HOME="$PWD/graalvm-community-openjdk-$GRAALVM_VERSION+9.1"
${GRAALVM_HOME}/bin/gu install native-image