# Execute the cleanAndStartDocker.ps1 script with bypassed execution policy
# powershell -ExecutionPolicy Bypass -File .\cleanAndStartDocker.ps1

Write-Host "=== Removing all Docker containers ==="
# Get-ChildItem -File
# docker ps -a
docker ps -aq | ForEach-Object { docker rm -f $_ }
# docker images
docker images -q | ForEach-Object { docker rmi -f $_ }
# docker volume ls
docker volume ls -q | ForEach-Object { docker volume rm $_ }
docker-compose build
docker-compose up