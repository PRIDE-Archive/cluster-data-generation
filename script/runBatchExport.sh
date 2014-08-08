#!/bin/sh

##### OPTIONS
OUTPUT_PATH=$1
FILTER_XML=$2
INPUT_ROOT_PATH=/nfs/pride/prod/archive
JOB_FILE=$3

while read p; do
    ./runExport.sh ${OUTPUT_PATH} ${FILTER_XML} ${INPUT_ROOT_PATH}/$p
done < ${JOB_FILE}