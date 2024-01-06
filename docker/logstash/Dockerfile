ARG ELK_VERSION
FROM docker.elastic.co/logstash/logstash:${ELK_VERSION}

# Logs will be displayed in the console as the pipeline is configured with:
# stdout { codec => rubydebug }

# Example configuration:
# output {
#     stdout { codec => rubydebug }
#     elasticsearch {
#         hosts => "elasticsearch:9200"
#         user => "elastic"
#         password => "changeme"
#         ecs_compatibility => disabled
#     }
# }

# Note: It's vital to update the 'password' field with your own secure credentials before deploying to a production environment.
