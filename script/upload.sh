#!/bin/bash
image=$1

scp -r images/$image Arastorn@10.100.2.2:~/$image
ssh Arastorn@10.100.2.2 << EOF
hadoop fs -put $image /user/projet_ALL/images/$image
hadoop fs -ls /user/projet_ALL/images/$image
rm -r $image
EOF
