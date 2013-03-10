#!/bin/bash 
# Get list of files to be formatted
file_list=`find . -name "*.xml" -o -name "*.xprj" -o -name "*.xodr" -type f`
for fn in $file_list
    do 
        echo "Processing $fn ...... "
        # format file
        java -jar xmlformatter-0.0.1-SNAPSHOT-jar-with-dependencies.jar $fn
        # line ending is set in xmlformatter plattform-dependent
        # sed -i 's/\r$//' $fn
        done

echo "    Done" 
