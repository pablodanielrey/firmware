#!/bin/bash
docker run --rm -ti -v /src/github/firmware/scripts:/scripts -p 5683:5683 --name script1 script1:latest bash