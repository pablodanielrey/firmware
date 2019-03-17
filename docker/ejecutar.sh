#!/bin/bash
docker run --rm -ti -v /src/github/firmware/scripts:/scripts -p 5678:5678 hardware:latest bash
