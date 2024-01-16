#!/bin/bash

echo "Setting initial password..."
neo4j-admin set-initial-password Admin123

/var/lib/neo4j/bin/neo4j start
echo "Start"

sleep 20

bin/cypher-shell -u neo4j -p Admin123 < init-db.cypher
echo "Import"

tail -f /var/lib/neo4j/logs/neo4j.log