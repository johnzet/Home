#!/bin/bash
cp Services/target/home-automation-server-services-1.0-SNAPSHOT.jar Services/target/HouseServer.jar
java -Dspring.profiles.active=production -jar ./Services/target/HouseServer.jar
