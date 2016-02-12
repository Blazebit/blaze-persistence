#!/bin/bash -e

sudo apt-get update -qq
sudo /bin/echo -e oracle-java9-installer shared/accepted-oracle-license-v1-1 select true | sudo debconf-set-selections
sudo apt-get install -y oracle-java9-installer
sudo apt-get install -y oracle-java9-unlimited-jce-policy

sh travis/install_latest_maven.sh
