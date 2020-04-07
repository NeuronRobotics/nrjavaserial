#! /bin/bash

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

export OPENCV_DIR=$DIR/opencv249build/
echo Setting OPENCV_DIR = ${OPENCV_DIR}
if (java -jar "$DIR/JavaVersionCheck.jar" 8 45) ; then
    java -jar "$DIR/bin/BowlerStudio.jar"  -r "$@"
else
    echo "Update Java and try again"
fi

