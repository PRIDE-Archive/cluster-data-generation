#!/bin/sh

##### OPTIONS
MEMORY_LIMIT=100000
OUTPUT_PATH=$2
FILTER_XML=$3
JOB_FILE=$4

while read p; do
    runImportArchiveSpectra.sh ${MEMORY_LIMIT} ${OUTPUT_PATH} ${FILTER_XML} $p
done < ${JOB_FILE}