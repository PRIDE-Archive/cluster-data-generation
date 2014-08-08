#!/bin/sh

##### OPTIONS
OUTPUT_PATH=$1
FILTER_XML=$2
INPUT_PATH=$3

##### RUN it on the production LSF cluster
## this is not queued in the PRIDE LSF submission group, this is submitted as regular job as it is independent of any other job
bsub -M 4000 -R "rusage[mem=4000]" -q production-rh6 -u pride.eb.test@gmail.com -J PRIDE-CLUSTER-EXPORT java -Xmx4000m -jar spectra-filter-archive-1.0-SNAPSHOT.jar ${OUTPUT_PATH} ${FILTER_XML} ${INPUT_PATH}