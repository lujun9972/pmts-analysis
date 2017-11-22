#!/bin/bash
if [[ $# -ne 1 ]];then
    cat<<EOF
    Usage: $0 YYYYMMDD
EOF
    exit
fi


PMTS_DATE=$1

for interval in 10 20 30 40 50 60;do
    echo analysis $interval >&2
    java MQTimeAnalysisTool ${PMTS_DATE}A.log $interval >${interval}A
    java MQTimeAnalysisTool ${PMTS_DATE}B.log $interval >${interval}B
done
echo CNAPS_A total: $(grep "首选发送队列名:\[MSGTOCNAP" ${PMTS_DATE}A.log|wc -l)
echo CNAPS_B total: $(grep "首选发送队列名:\[MSGTOCNAP" ${PMTS_DATE}B.log|wc -l)
echo IBPS_A total: $(grep "首选发送队列名:\[MSGTOIBPS" ${PMTS_DATE}A.log|wc -l)
echo IBPS_B total: $(grep "首选发送队列名:\[MSGTOIBPS" ${PMTS_DATE}B.log|wc -l)
