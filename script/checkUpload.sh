#!/bin/bash
image=$1

ssh Arastorn@10.100.2.2 << EOF
hadoop fs -ls /user/projet_ALL/images/$image
EOF
