# Getting Started with Air-Book

psql -h postgres -p 5432 -U postgres -d postgres 
psql -h postgres-service -p 5432 -U postgres -d postgres 
SELECT * FROM users LIMIT 100;
\q


gradlew build
gradlew build --no-daemon -x test  



echo YOUR_PERSONAL_ACCESS_TOKEN | docker login ghcr.io -u MrLipa --password-stdin


curl https://static.snyk.io/cli/latest/snyk-win.exe -o snyk.exe
snyk auth
snyk test --all-projects
snyk monitor --all-projects
snyk code test










