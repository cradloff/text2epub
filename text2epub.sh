#/bin/sh
# Path where script resides
T2E_PATH=`dirname $0`
# Path to jar archive
JAR=$T2E_PATH/text2epub-1.0-jar-with-dependencies.jar

java -classpath $T2E_PATH:$JAR text2epub.Text2Epub "$@"

