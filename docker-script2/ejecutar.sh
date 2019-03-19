#!/bin/bash
docker run --rm -ti -v /src/github/firmware/scripts:/scripts -p 5681:5681 --name scriptss script2:latest bash
