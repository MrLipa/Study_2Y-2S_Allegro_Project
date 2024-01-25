# Getting Started with Air-Book



http://prometheus:9090/

Get-Process | Where-Object {$_.ProcessName -like "*ssh*"}
Stop-Process -Id
ssh -N -f -L 81:172.20.73.4:81 9szkaradek@taurus.fis.agh.edu.pl

ssh -N -f -L 81:172.20.73.4:81 -L 3001:172.20.73.4:3001 -L 3002:172.20.73.4:3002 -L 3003:172.20.73.4:3003 -L 3004:172.20.73.4:3004 -L 3005:172.20.73.4:3005 -L 3006:172.20.73.4:3006 -L 8080:172.20.73.4:8080 -L 3010:172.20.73.4:3010 9szkaradek@taurus.fis.agh.edu.pl

http://localhost:3001/oauth2/authorization/github


docker-compose logs -f pdadmin


gradlew build
gradlew build --no-daemon -x test  
gradlew --refresh-dependencies
gradlew clean build --refresh-dependencies

http_server_requests_seconds_count

powershell -ExecutionPolicy Bypass -File .\cleanAndStartDocker.ps1
powershell -ExecutionPolicy Bypass -File .\cleanAndStartDockerGHCR.ps1

chmod +x cleanAndStartDocker.sh
./cleanAndStartDocker.sh

chmod +x cleanAndStartDockerGHCR.sh
./cleanAndStartDockerGHCR.sh





psql -h postgres -p 5432 -U postgres -d postgres 
psql -h postgres-service -p 5432 -U postgres -d postgres 
SELECT * FROM users LIMIT 100;
\q




  notification-service:
    build:
      context: ./../notification-service
      dockerfile: Dockerfile
    restart: always
    command:
      - --config.file=/etc/prometheus/prometheus.yml
    ports:
      - '8080:8080'
    networks: 
      - app-network
    privileged: true

  prometheus:
    container_name: prometheus
    image: prom/prometheus
    restart: always
    command:
      - --config.file=/etc/prometheus/prometheus.yml
    volumes:
      - ./prometheus/prometheus.yml:/etc/prometheus/prometheus.yml
    ports:
      - "9090:9090"
    networks: 
      - app-network

 


echo YOUR_PERSONAL_ACCESS_TOKEN | docker login ghcr.io -u MrLipa --password-stdin


curl https://static.snyk.io/cli/latest/snyk-win.exe -o snyk.exe
snyk auth
snyk test --all-projects
snyk monitor --all-projects
snyk code test









