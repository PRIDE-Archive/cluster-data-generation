#!/bin/sh

##### OPTIONS
OUTPUT_PATH=$1
FILTER_XML=$2
JOB_FILE=$3

while read p; do
    ./runExport.sh ${OUTPUT_PATH} ${FILTER_XML} $p
done < ${JOB_FILE}