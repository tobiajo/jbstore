#!/usr/bin/env bash

run_time=60

mvn clean compile
mvn exec:java -Dexec.mainClass="se.kth.id2203.jbstore.deploy.NodeLauncher" -Dexec.args="127.0.0.1 10000 0 5 $run_time" &
sleep 5
for i in {1..4}
do
    run_time=$((run_time-5))
    mvn exec:java -Dexec.mainClass="se.kth.id2203.jbstore.deploy.NodeLauncher" -Dexec.args="127.0.0.1 1000$i 127.0.0.1 10000 $i 5 $run_time" &
    sleep 5
done
echo "cluster_start: all nodes started"
sleep $run_time
echo "cluster_start: stopped"
