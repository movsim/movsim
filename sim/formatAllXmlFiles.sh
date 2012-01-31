#!/bin/bash 
# Get list of files to be formatted
file_list=`find . -name "*.xml" -o -name "*.xodr" -type f`
for fn in $file_list
    do 
        echo "Processing $fn ...... "
        java -jar xmlformatter-0.0.1-SNAPSHOT-jar-with-dependencies.jar $fn
        done

echo "    Done" 
