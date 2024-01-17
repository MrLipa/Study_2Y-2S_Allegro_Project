#!/bin/bash

# sudo chmod +x cleanAndStartDocker.sh
# sudo ./cleanAndStartDocker.sh
echo "=== Removing all Docker containers ==="

docker ps -aq | xargs -r docker rm -f
docker images -q | xargs -r docker rmi -f
docker volume ls -q | xargs -r docker volume rm

docker-compose build
docker-compose up
