FROM neo4j:latest

COPY ./init-db.cypher init-db.cypher
COPY ./init-db.sh init-db.sh

RUN chmod +x init-db.sh

EXPOSE 7474 7473 7687
