#!/bin/sh

##### OPTIONS
MEMORY_LIMIT=100000
INPUT_FILE=$1
OUTPUT_PATH=$2
FILTER_XML=$3

##### FUNCTIONS
printUsage() {
    echo "Description: Import the PRIDE Archive data in Batch .. "
    echo ""
    echo "Usage: ./runBatchExportArchiveImport.sh <input-text-projects> <output-path> <filter-xml>"
}

if [ "$#" -ne 3 ]
then
  printUsage
  exit 1
fi

while read p; do
    ./runImportArchiveSpectra.sh ${MEMORY_LIMIT} ${OUTPUT_PATH} ${FILTER_XML} $p
done < ${INPUT_FILE}