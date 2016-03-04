#!/usr/bin/env bash

mvn exec:java -Dexec.mainClass="se.kth.id2203.jbstore.deploy.NodeLauncher" -Dexec.args="127.0.0.1 10002 127.0.0.1 10001"
