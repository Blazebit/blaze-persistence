#! /bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

java -version

if [ "$COMPONENT" == 'hibernate-6.2' ]; then
  export JPAPROVIDER="hibernate-6.2"
  export PROPERTIES="-s $DIR/latest-settings.xml -Dversion.hibernate-6.2=[6.2,6.3.Alpha)"
elif [ "$COMPONENT" == 'hibernate-6.3' ]; then
  export JPAPROVIDER="hibernate-6.3"
  export PROPERTIES="-s $DIR/latest-settings.xml -Dversion.hibernate-6.3=[6.3,6.4.Alpha)"
elif [ "$COMPONENT" == 'hibernate-6.4' ]; then
  export JPAPROVIDER="hibernate-6.4"
  export PROPERTIES="-s $DIR/latest-settings.xml -Dversion.hibernate-6.4=[6.4,6.5.Alpha)"
else
  export JPAPROVIDER="hibernate-6.2"
  export PROPERTIES="-Dversion.spring-data-3.1=[3,4.Alpha) -Dversion.spring-data-3.1-spring=[6.0,6.1.Alpha) -Dversion.spring-data-3.1-spring-boot=[3,4.Alpha)"
fi

exec bash $DIR/../build.sh