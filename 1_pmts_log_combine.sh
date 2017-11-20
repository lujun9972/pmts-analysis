#! /bin/env sh
if [[ $# -ne 1 ]];then
	cat<<EOF
usage:$0 PMTS/MSG/DIRECTORY
EOF
else
	cd $1
	sed -s '/=======/,/=======/ d' $(ls -tr)
fi
