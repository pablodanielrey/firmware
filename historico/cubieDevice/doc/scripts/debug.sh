#!/bin/bash
java -Djava.util.logging.config.file="logging.properties" -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=8111,suspend=n -jar cubieDevice-1.0-SNAPSHOT.jar
