#!/bin/bash
echo $1
docker run --rm -d -v /src/github/firmware/scripts/Cerradura/programa:/scripts/Cerradura/programa -p 1883:1883 -p 5678:5678 --name cerradura cerradura:latest $1