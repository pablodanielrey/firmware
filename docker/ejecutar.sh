#!/bin/bash
docker run --rm -ti -v /src/github/firmware/scripts:/scripts -p 5678:5678 -p 5679:5679 -p 5680:5680 --name scripts hardware:latest bash
