#!/bin/bash
image=$1
username=$2
usernameLower=$2
declare -l usernameLower
usernameLower=$usernameLower

scp -r images/$image $username@10.100.2.2:~/$image
ssh $2@10.100.2.2 << EOF
hadoop fs -put $image /user/$usernameLower/images/$image
hadoop fs -ls /user/$usernameLower/images/$image
rm -r $image
EOF
