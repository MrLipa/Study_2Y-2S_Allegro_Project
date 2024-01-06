import requests
from requests.auth import HTTPBasicAuth

jenkins_url = "http://localhost:8080"

username = "admin"
password = "admin"

api_url = f"{jenkins_url}/pluginManager/api/json?depth=1"

response = requests.get(api_url, auth=HTTPBasicAuth(username, password))

if response.status_code == 200:
    data = response.json()

    plugins = list(map(lambda i: i["url"].replace("https://plugins.jenkins.io/", "") + ":" + i["version"], data["plugins"]))
    plugins.sort()

    with open("plugins.txt", "w") as file:
        for plugin in plugins:
            file.write(plugin + "\n")

    print("Plugins saved to plugins.txt")
else:
    print(f"Error: {response.status_code}")
    print(response.text)
