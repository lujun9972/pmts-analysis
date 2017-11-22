#!/bin/bash
if [[ $# -ne 1 ]];then
    cat<<EOF
    Usage: $0 YYYYMMDD
EOF
    exit
fi


PMTS_DIR_IN_USB="/run/media/lujun9972/56CB-D4A2/PMTS日志"

PMTS_DATE=$1

mkdir -p ${PMTS_DATE}
cd ${PMTS_DATE}
cp ${PMTS_DIR_IN_USB}/${PMTS_DATE}* ./

tar -xvf ${PMTS_DATE}A.tar.gz && mv ${PMTS_DATE} ${PMTS_DATE}A
tar -xvf ${PMTS_DATE}B.tar.gz && mv ${PMTS_DATE} ${PMTS_DATE}B

../1_pmts_log_combine.sh ${PMTS_DATE}A/msg > ${PMTS_DATE}A.log
../1_pmts_log_combine.sh ${PMTS_DATE}B/msg > ${PMTS_DATE}B.log

cp ../MQTimeAnalysisTool.class ./
../2_analysis.sh ${PMTS_DATE} > total.log

tail -n 1 *A *B >> total.log
