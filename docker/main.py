import requests
import os
import json
from urllib.parse import urlparse

# Swagger configuration
swagger_config = {
    "swagger": "2.0",
    "info": {
        "title": "Microservices API",
        "version": "1.0.0"
    },
    "apis": [
        {"url": "http://localhost:3001/v3/api-docs", "name": "User Service"},
        {"url": "http://localhost:3002/v3/api-docs", "name": "Notification Service"},
        {"url": "http://localhost:3003/v3/api-docs", "name": "Flight Service"},
        {"url": "http://localhost:3004/v3/api-docs", "name": "Airport Service"},
        {"url": "http://localhost:3005/v3/api-docs", "name": "Airplane Service"},
        {"url": "http://localhost:3006/v3/api-docs", "name": "Admin Service"}
    ]
}

# Directory for saving Swagger files
output_dir = "./swagger"
os.makedirs(output_dir, exist_ok=True)

# Initial structure for the merged Swagger
merged_swagger = {
    "openapi": "3.0.1",
    "info": {
        "title": "Microservices API",
        "version": "1.0.0"
    },
    "servers": [],
    "paths": {},
    "components": {}
}

# Function to fetch Swagger files and merge them
def fetch_and_merge_swagger(api):
    try:
        response = requests.get(api['url'])
        response.raise_for_status()
        swagger_data = response.json()
        
        # Parse URL and construct base URL
        parsed_url = urlparse(api['url'])
        base_url = f"{parsed_url.scheme}://{parsed_url.netloc}"
        
        # Merge servers
        server_url = {"url": base_url, "description": f"Generated server url for {api['name']}"}
        if server_url not in merged_swagger['servers']:
            merged_swagger['servers'].append(server_url)
        
        # Merge paths
        for path, path_data in swagger_data['paths'].items():
            if path in merged_swagger['paths']:
                merged_swagger['paths'][path].update(path_data)
            else:
                merged_swagger['paths'][path] = path_data
        
        # Merge components
        if 'components' in swagger_data:
            for component, component_data in swagger_data['components'].items():
                if component in merged_swagger['components']:
                    merged_swagger['components'][component].update(component_data)
                else:
                    merged_swagger['components'][component] = component_data

        return f"Successfully merged {api['name']}"
    except Exception as e:
        return f"Error fetching/merging {api['name']}: {e}"

# Fetch and merge Swagger files for each API
results = [fetch_and_merge_swagger(api) for api in swagger_config['apis']]

# Save the merged Swagger file
merged_file_path = os.path.join(output_dir, "openapi.json")
with open(merged_file_path, 'w') as file:
    json.dump(merged_swagger, file, indent=4)

print(results)
print(merged_file_path)
