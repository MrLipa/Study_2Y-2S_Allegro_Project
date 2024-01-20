#!/bin/bash

# sudo chmod +x cleanAndStartDockerGHCR.sh
# sudo ./cleanAndStartDockerGHCR.sh
echo "=== Removing all Docker containers ==="

docker ps -aq | xargs -r docker rm -f
docker images -q | xargs -r docker rmi -f
docker volume ls -q | xargs -r docker volume rm

docker-compose -f docker-compose-ghcr.yml build
docker-compose -f docker-compose-ghcr.yml up