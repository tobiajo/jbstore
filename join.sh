#!/usr/bin/env bash

if [ $# -eq 4 ]; then
    a=$1
    b=$2
    c=$3
    d=$4
elif [ $# -eq 0 ]; then
    a="127.0.0.1"
    b="45678"
    c="127.0.0.1"
    d="34567"
else
    echo "Invalid number of arguments"
    exit 1
fi

mvn exec:java -Dexec.mainClass="se.kth.id2203.jbstore.Main" -Dexec.args="$a $b $c $d"
