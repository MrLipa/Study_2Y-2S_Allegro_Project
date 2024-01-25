# Getting Started with Air-Book

psql -h postgres -p 5432 -U postgres -d postgres 
psql -h postgres-service -p 5432 -U postgres -d postgres 
SELECT * FROM users LIMIT 100;
\q


gradlew build
gradlew build --no-daemon -x test  
gradlew --refresh-dependencies
gradlew clean build --refresh-dependencies

http_server_requests_seconds_count

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










