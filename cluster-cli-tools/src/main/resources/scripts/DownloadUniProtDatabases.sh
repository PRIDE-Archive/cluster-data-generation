#!/bin/sh
## This can be run as a single script in the logging machine
OUTPUT_FOLDER=$1
INITIAL_TAXONOMIES=$2
LOG_FOLDER=/nfs/pride/prod/logs/cluster/
MEMORY_LIMIT=40000

##### SCRIPT VARIABLES
# the name to give to the LSF job (to be extended with additional info)
JOB_NAME="PRIDE-CLUSTER-DATABASE-DOWNLOAD"
# the job parameters that are going to be passed on to the job (build below)
JOB_PARAMETERS=""
# memory limit
JOB_EMAIL="yperez@ebi.ac.uk"
# Log file name
NOW=$(date +"%m-%d-%Y")
LOG_FILE_NAME=$(basename ${OUTPUT_FOLDER})

runJava.sh ${LOG_FOLDER}/${LOG_FILE_NAME}-${NOW}.log ${MEMORY_LIMIT}m -jar ../${project.artifactId}/${project.artifactId}-${project.version}.jar -cp uk.ac.ebi.pride.cluster.tools.fasta.FastaDownloadTool -o ${OUTPUT_FOLDER} -lc ${INITIAL_TAXONOMIES} -d