#!/usr/bin/env bash

set -e

echo "##### Running benchamarks with AuditTrail"
gradle clean assemble -DauditTrail.enabled=true
java -jar build/libs/grails-gpars-batch-load-benchmark-0.1.war

echo "##### Running benchamarks without AuditTrail"
gradle clean assemble --no-daemon check --stacktrace
java -jar build/libs/grails-gpars-batch-load-benchmark-0.1.war

echo "###### Running benchamarks with custom IdGenerator"
java -Didgenerator.enabled=true -jar build/libs/grails-gpars-batch-load-benchmark-0.1.war

echo "###### Running benchamarks with autowire off"
java -Dautowire.enabled=false -jar build/libs/grails-gpars-batch-load-benchmark-0.1.war