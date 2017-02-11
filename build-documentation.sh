#!/bin/bash
SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

mvn --projects documentation -am clean compile

if [ $(uname -s) == "Linux" ]; then
  URL="file://$DIR/documentation/target/generated-docs/core/manual/en_US/index.html"
  echo "Goto: $URL"
  
  if which xdg-open > /dev/null
  then
    xdg-open $URL
  elif which gnome-open > /dev/null
  then
    gnome-open $URL
  fi
elif [ $(uname) == "Darwin" ]; then
  URL="file://$DIR/documentation/target/generated-docs/core/manual/en_US/index.html"
  echo "Goto: $URL"
  open $URL
else
  if [[ "$DIR" =~ ^\/([a-zA-Z])\/(.*)$ ]]; 
  then 
    DIR="/${BASH_REMATCH[1]}:/${BASH_REMATCH[2]}"
  fi
  
  URL="file://$DIR/documentation/target/generated-docs/core/manual/en_US/index.html"
  echo "Goto: $URL"
  start $URL
fi
