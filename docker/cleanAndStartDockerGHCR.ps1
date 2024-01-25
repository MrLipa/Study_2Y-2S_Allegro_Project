# Execute the cleanAndStartDockerGHCR.ps1 script with bypassed execution policy
# powershell -ExecutionPolicy Bypass -File .\cleanAndStartDockerGHCR.ps1

Write-Host "=== Removing all Docker containers ==="
# Get-ChildItem -File
# docker ps -a
docker ps -aq | ForEach-Object { docker rm -f $_ }
# docker images
docker images -q | ForEach-Object { docker rmi -f $_ }
# docker volume ls
docker volume ls -q | ForEach-Object { docker volume rm $_ }
docker-compose -f docker-compose-ghcr.yml build
docker-compose -f docker-compose-ghcr.yml up -d