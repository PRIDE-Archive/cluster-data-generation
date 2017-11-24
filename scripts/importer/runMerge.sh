#!/bin/sh

##### OPTIONS
INPUT_PATH=$1
OUTPUT_PATH=$2
OUTPUT_SIZE=$3
NUMBER_OF_BATCH=$4

# Log file name
LOG_FILE_NAME="$(basename ${INPUT_PATH})-merge"
MEMORY_LIMIT=30000

##### RUN it on the production LSF cluster
## this is not queued in the PRIDE LSF submission group, this is submitted as regular job as it is independent of any other job
bsub -M ${MEMORY_LIMIT} -R "rusage[mem=${MEMORY_LIMIT}]" -q production-rh6 -o /dev/null -g /spectra_cluster_exporter -J PRIDE-CLUSTER-EXPORT ./runJava.sh ./log/${LOG_FILE_NAME}.log ${MEMORY_LIMIT}m -cp ${project.build.finalName}.jar uk.ac.ebi.pride.spectracluster.merger.Merger ${INPUT_PATH} ${OUTPUT_PATH} ${OUTPUT_SIZE} ${NUMBER_OF_BATCH}