FROM jenkins/jenkins:2.414.2

LABEL maintainer="Tomasz Szkaradek"
ENV DISPLAY=:1
ENV JENKINS_HOME /var/jenkins_home

COPY jenkins_config.sh /var/jenkins_home/jenkins_config.sh
COPY requirements.txt /var/jenkins_home/requirements.txt
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
COPY jenkins.yaml /var/jenkins_home/jenkins.yaml


# Without Login
ARG JAVA_OPTS
ENV JAVA_OPTS "-Djenkins.install.runSetupWizard=false ${JAVA_OPTS:-}"

RUN jenkins-plugin-cli --version
RUN jenkins-plugin-cli -f /usr/share/jenkins/ref/plugins.txt

# URL to plugins
# http://localhost:8080/pluginManager/api/json?depth=1

# Not work
# RUN xargs /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt