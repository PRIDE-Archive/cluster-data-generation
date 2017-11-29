#!/bin/sh

LOG_FOLDER=/nfs/pride/prod/logs/cluster/

### This script take a PeptideAtlas path and compute the corresponding Parameters ###

##### PARAMETERS
INPUT_PATH=$1
MEMORY_LIMIT=$3
OUTPUT_PATH=$2

##### SCRIPT VARIABLES
# the name to give to the LSF job (to be extended with additional info)
JOB_NAME="PRIDE-CLUSTER-PARAMETER-PREDICTOR"
# the job parameters that are going to be passed on to the job (build below)
JOB_PARAMETERS=""
# memory limit
JOB_EMAIL="yperez@ebi.ac.uk"
# Log file name
NOW=$(date +"%m-%d-%Y")
LOG_FILE_NAME=$(basename ${INPUT_PATH})



##### FUNCTIONS
printUsage() {
    echo "LSF script to run estimate the parameters from PeptideAtlas submissions"
    echo ""
    echo "Usage: ./PeptideAtlasParametersLSF.sh <input-path> <output-path> <memory-limit>"
}

##### RUN it on the production LSF cluster
## this is not queued in the PRIDE LSF submission group, this is submitted as regular job as it is independent of any other job
bsub -M ${MEMORY_LIMIT} -R "rusage[mem=${MEMORY_LIMIT}]" -q production-rh7 -g /pride_cluster_archive_import -u ${JOB_EMAIL} -J ${JOB_NAME}-${NOW}-${LOG_FILE_NAME} ./runJava.sh $LOG_FOLDER/${LOG_FILE_NAME}-${NOW}.log ${MEMORY_LIMIT}m -jar ${project.build.finalName}.jar -cp uk.ac.ebi.pride.spectracluster.cli.ImportFromArchiveCLI -o ${OUTPUT_PATH} -c ${FILTER_XML} -i ${INPUT_PATH} -s