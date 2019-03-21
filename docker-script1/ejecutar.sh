#!/bin/bash
echo $1
docker run --rm -d -v /src/github/firmware/scripts:/scripts -p 5678:5678 --name scripts script1:latest $1
