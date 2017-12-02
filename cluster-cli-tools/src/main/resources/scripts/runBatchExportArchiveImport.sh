#!/bin/sh

##### OPTIONS
MEMORY_LIMIT=100000
INPUT_FILE=$1
OUTPUT_PATH=$2
FILTER_XML=$3

while read p; do
    ./runImportArchiveSpectra.sh ${MEMORY_LIMIT} ${OUTPUT_PATH} ${FILTER_XML} $p
done < ${INPUT_FILE}