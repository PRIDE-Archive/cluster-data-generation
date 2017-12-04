#!/bin/sh

##### OPTIONS
DATABASES_FOLDER=$1
CONTAMINANT_DATABASE=$2
MEMORY_LIMIT=30000
JOB_EMAIL="yperez@ebi.ac.uk"

#### LOG FOLDER
LOG_FOLDER=/nfs/pride/prod/logs/cluster


##### SCRIPT VARIABLES
# the name to give to the LSF job (to be extended with additional info)
JOB_NAME="PRIDE-CLUSTER-ARCHIVE-IMPORT"
# the job parameters that are going to be passed on to the job (build below)
JOB_PARAMETERS=""
# Log file name
NOW=$(date +"%m-%d-%Y")
LOG_FILE_NAME=$(basename ${INPUT_PATH})

##### FUNCTIONS
printUsage() {
    echo "Description: Generate database decoy folders "
    echo ""
    echo "Usage: ./runBatchGenerateDecoy.sh <database-folder> <contaminant-file>"
}

if [ "$#" -ne 2 ]
then
  printUsage
  exit 1
fi

##### RUN it on the production LSF cluster
## this is not queued in the PRIDE LSF submission group, this is submitted as regular job as it is independent of any other job

for a in ${DATABASES_FOLDER}/*.fasta
do
 INPUT_PATH=$a
 COMPLETE_NAME=${a%.fasta}-complete.fasta
 OUTPUT_PATH=${COMPLETE_NAME}
 bsub -M ${MEMORY_LIMIT} -R "rusage[mem=${MEMORY_LIMIT}]" -q production-rh7 -g /cluster-data-generation -u ${JOB_EMAIL} -J ${JOB_NAME}-${NOW}-${LOG_FILE_NAME} ./runJava.sh ${LOG_FOLDER}/${LOG_FILE_NAME}-${NOW}.log ${MEMORY_LIMIT}m -cp ../cluster-cli-tools/cluster-cli-tools-0.0.1-SNAPSHOT.jar uk.ac.ebi.pride.cluster.tools.fasta.FastaProcessingTool -i ${INPUT_PATH} -a ${CONTAMINANT_DATABASE} -o ${OUTPUT_PATH} -d
done