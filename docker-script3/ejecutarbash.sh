#!/bin/bash
docker run --rm -ti -v /src/github/firmware/scripts:/scripts -p 5683:5683 --name script3 script3:latest bash