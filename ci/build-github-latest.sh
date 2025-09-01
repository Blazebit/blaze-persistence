#! /bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

java -version

if [ "$COMPONENT" == 'hibernate-6.2' ]; then
  export JPAPROVIDER="hibernate-6.2"
  export SPRING_DATA="spring-data-3.1.x"
  export PROPERTIES="-s $DIR/latest-settings.xml -Dversion.hibernate-6.2=[6.2,6.3.Alpha)"
elif [ "$COMPONENT" == 'hibernate-6.4' ]; then
  export JPAPROVIDER="hibernate-6.4"
  export SPRING_DATA="spring-data-3.2.x"
  export PROPERTIES="-s $DIR/latest-settings.xml -Dversion.hibernate-6.4=[6.4,6.5.Alpha)"
elif [ "$COMPONENT" == 'hibernate-6.5' ]; then
  export JPAPROVIDER="hibernate-6.5"
  export SPRING_DATA="spring-data-3.3.x"
  export PROPERTIES="-s $DIR/latest-settings.xml -Dversion.hibernate-6.5=[6.5,6.6.Alpha)"
elif [ "$COMPONENT" == 'hibernate-6.6' ]; then
  export JPAPROVIDER="hibernate-6.6"
  export SPRING_DATA="spring-data-3.3.x"
  export PROPERTIES="-s $DIR/latest-settings.xml -Dversion.hibernate-6.6=[6.6,6.6.Alpha)"
elif [ "$COMPONENT" == 'hibernate-7.1' ]; then
  export JPAPROVIDER="hibernate-7.1"
  export SPRING_DATA="spring-data-4.0.x"
  export PROPERTIES="-s $DIR/latest-settings.xml -Dversion.hibernate-7.1=[7.1,7.1.Alpha)"
else
  export JPAPROVIDER="hibernate-6.2"
  export SPRING_DATA="spring-data-3.1.x"
  export PROPERTIES="-Dversion.spring-data-3.1=[3,4.Alpha) -Dversion.spring-data-3.1-spring=[6.0,6.1.Alpha) -Dversion.spring-data-3.1-spring-boot=[3,4.Alpha)"
fi

exec bash $DIR/../build.sh