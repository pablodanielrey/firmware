#!/bin/bash
echo $1
docker run --rm -d -v /src/github/firmware/scripts:/scripts -p 5681:5681 --name script1 script1:latest $1
