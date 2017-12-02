#!/bin/sh
### This script creates a folder of the project and add all the spectra for the corresponding project in assay based

LOG_FOLDER=/nfs/pride/prod/logs/cluster

##### PARAMETERS
MEMORY_LIMIT=$1
OUTPUT_PATH=$2
FILTER_XML=$3
INPUT_PATH=$4

##### SCRIPT VARIABLES
# the name to give to the LSF job (to be extended with additional info)
JOB_NAME="PRIDE-CLUSTER-ARCHIVE-IMPORT"
# the job parameters that are going to be passed on to the job (build below)
JOB_PARAMETERS=""
# memory limit
JOB_EMAIL="yperez@ebi.ac.uk"
# Log file name
NOW=$(date +"%m-%d-%Y")
LOG_FILE_NAME=$(basename ${INPUT_PATH})

##### FUNCTIONS
printUsage() {
    echo "Description: Import PRIDE Cluster data mgf into assay based system... "
    echo ""
    echo "Usage: ./runImportArchiveSpectra.sh <memory-limit> <outputpath> <filter-xml> <input-path>"
}

if if [ "$#" -ne 4 ]
then
  printUsage
  exit 1
fi

##### RUN it on the production LSF cluster
## this is not queued in the PRIDE LSF submission group, this is submitted as regular job as it is independent of any other job
bsub -M ${MEMORY_LIMIT} -R "rusage[mem=${MEMORY_LIMIT}]" -q production-rh7 -g /cluster-data-generation -u ${JOB_EMAIL} -J ${JOB_NAME}-${NOW}-${LOG_FILE_NAME} ./runJava.sh ${LOG_FOLDER}/${LOG_FILE_NAME}-${NOW}.log ${MEMORY_LIMIT}m -cp ../cluster-cli-tools/cluster-cli-tools-0.0.1-SNAPSHOT.jar uk.ac.ebi.pride.cluster.tools.importers.ArchiveSpectraImportTool -o ${OUTPUT_PATH} -c ${FILTER_XML} -i ${INPUT_PATH} -s