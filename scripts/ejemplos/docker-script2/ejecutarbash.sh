#!/bin/bash
docker run --rm -ti -v /src/github/firmware/scripts:/scripts -p 5682:5682 --name script2 script2:latest bash