#!/bin/sh

##### PARAMETERS
OUTPUT_PATH=$1
FILTER_XML=$2
INPUT_PATH=$3

##### SCRIPT VARIABLES
# the name to give to the LSF job (to be extended with additional info)
JOB_NAME="PRIDE-CLUSTER-ARCHIVE-IMPORT"
# the job parameters that are going to be passed on to the job (build below)
JOB_PARAMETERS=""
# memory limit
MEMORY_LIMIT=60000
JOB_EMAIL="pride-report@ebi.ac.uk"
# Log file name
NOW=$(date +"%m-%d-%Y")
LOG_FILE_NAME=$(basename ${INPUT_PATH})



##### FUNCTIONS
printUsage() {
    echo "Description: Import of PRIDE Complete Submissions into PRIDE Cluster"
    echo ""
    echo "Usage: ./runExport.sh <outputpath> <filter-xml> <inputpath>"
}

##### RUN it on the production LSF cluster
## this is not queued in the PRIDE LSF submission group, this is submitted as regular job as it is independent of any other job
bsub -M ${MEMORY_LIMIT} -R "rusage[mem=${MEMORY_LIMIT}]" -q production-rh7 -g /pride_cluster_archive_import -u ${JOB_EMAIL} -J ${JOB_NAME}-${NOW}-${LOG_FILE_NAME} ./runJava.sh ./log/${LOG_FILE_NAME}-${NOW}.log ${MEMORY_LIMIT}m -jar ${project.build.finalName}.jar -o ${OUTPUT_PATH} -c ${FILTER_XML} -i ${INPUT_PATH} -s