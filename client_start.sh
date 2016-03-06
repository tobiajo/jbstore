#!/usr/bin/env bash

mvn clean compile
mvn exec:java -Dexec.mainClass="se.kth.id2203.jbstore.deploy.ClientLauncher" -Dexec.args="127.0.0.1 65535 127.0.0.1 10000 5"
