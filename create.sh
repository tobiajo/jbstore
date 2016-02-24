#!/usr/bin/env bash

if [ $# -eq 2 ]; then
    a=$1
    b=$2
    c=$3
    d=$4
elif [ $# -eq 0 ]; then
    a="127.0.0.1"
    b="34567"
else
    echo "Invalid number of arguments"
    exit 1
fi

mvn exec:java -Dexec.mainClass="se.kth.id2203.jbstore.NodeLauncher" -Dexec.args="$a $b"
