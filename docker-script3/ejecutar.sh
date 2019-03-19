#!/bin/bash
docker run --rm -ti -v /src/github/firmware/scripts:/scripts -p 5682:5682 --name script3 script3:latest bash
