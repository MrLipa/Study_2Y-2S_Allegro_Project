# Define a build argument for the Elastic Stack version
ARG ELK_VERSION

# Use the official Kibana image from Elastic's Docker registry with the specified version
FROM docker.elastic.co/kibana/kibana:${ELK_VERSION}

# Switch to root to modify permissions
USER root
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# Switch back to kibana user
USER kibana

# Set the entrypoint.sh as the default entrypoint
# ENTRYPOINT ["/entrypoint.sh"]

# Additional comments:
# This Dockerfile sets up a Kibana instance with a specific version defined by ELK_VERSION.
# If you're looking to customize the Kibana instance further, you can add additional commands or COPY instructions.
# For instance, if you want to pre-load some saved objects or dashboards into Kibana, you can COPY them into the appropriate directories in the container.
# It's also a good practice to keep the ELK components (Elasticsearch, Logstash, Kibana) at the same version for compatibility.
