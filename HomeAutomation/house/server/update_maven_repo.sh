#!/usr/bin/bash

mvn install:install-file -DgroupId=com.rapplogic -DartifactId=xbee -Dversion=0.9.Z -Dfile=../xbee-api/xbee-api-0.9.Z.jar -Dpackaging=jar -DgeneratePom=true 

mvn install:install-file -DgroupId=org.rxtx -DartifactId=rxtx -Dversion=2.2 -Dfile=../ch-rxtx-2.2-20081207-win-x64/RXTXcomm.jar -Dpackaging=jar -DgeneratePom=true 

