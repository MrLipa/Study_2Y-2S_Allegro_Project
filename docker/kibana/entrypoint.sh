#!/bin/bash
set -e

# Wait for Kibana to be ready
until $(curl --output /dev/null --silent --head --fail http://kibana:5601); do
    printf '.'
    sleep 5
done

# Your curl command to import configuration into Kibana
curl -u elastic:changeme -X POST "http://kibana:5601/api/saved_objects/_import" -H "kbn-xsrf: true" --form file=@/usr/share/kibana/config/kibana.ndjson

# Run the original Kibana entrypoint
exec /usr/local/bin/kibana-docker
