#!/bin/sh

##### OPTIONS
MEMORY_LIMIT=40000
INPUT_FILE=$1
OUTPUT_PATH=$2

##### FUNCTIONS
printUsage() {
    echo "Description: Compute the parameters for PRIDE Archive assays in batch"
    echo ""
    echo "Usage: ./runBatchParametersGeneration.sh <input-text-projects> <output-path>"
}

if [ "$#" -ne 2 ]
then
  printUsage
  exit 1
fi

while read p; do
    ./runParametersGeneration.sh ${MEMORY_LIMIT} ${OUTPUT_PATH} $p
done < ${INPUT_FILE}