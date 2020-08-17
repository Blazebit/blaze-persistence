#!/bin/bash
GRAALVM_VERSION=20.1.0

sudo apt-key adv --keyserver keyserver.ubuntu.com --recv-keys 6B05F25D762E3157 78BD65473CB3BD13
sudo apt-get update
sudo apt-get -y install build-essential libz-dev zlib1g-dev
curl -LJ "https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-$GRAALVM_VERSION/graalvm-ce-java11-linux-amd64-$GRAALVM_VERSION.tar.gz" --output graalvm.tar.gz
tar -xzf graalvm.tar.gz
export GRAALVM_HOME="$PWD/graalvm-ce-java11-$GRAALVM_VERSION"
${GRAALVM_HOME}/bin/gu install native-image