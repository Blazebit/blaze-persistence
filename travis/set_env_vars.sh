#!/bin/bash -e

if [ '$JDK' = '9' ]; then
	export PATH=/usr/lib/jvm/java-9-oracle/bin:/opt/apache-maven/bin:$PATH
	export MAVEN_OPTS="-Xmx1024m -XX:MaxMetaspaceSize=512m"
	export JAVA_HOME=/usr/lib/jvm/java-9-oracle/
fi

if [ '$LATEST_MAVEN' = 'true' ]; then
	export PATH=/opt/apache-maven/bin:$PATH
	export MAVEN_OPTS="-Xmx1024m -XX:MaxMetaspaceSize=512m"
fi