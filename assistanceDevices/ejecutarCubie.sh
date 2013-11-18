#!/bin/bash
java 	'-Djavax.sound.sampled.SourceDataLine=#sunxicodec [plughw:0,0]' \
        '-Djavax.sound.sampled.Clip=#sunxicodec [plughw:0,0]' \
	-Djava.util.logging.config.file="logging.properties" -jar target/assistanceDevices-1.0-SNAPSHOT.jar
