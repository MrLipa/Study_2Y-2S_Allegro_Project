FROM neo4j:4.4

COPY ./init-db.cypher init-db.cypher
COPY ./init-db.sh init-db.sh

RUN chmod +x init-db.sh

EXPOSE 7474 7473 7687