#!/bin/bash

echo "############## INSTALATION START ###################"
apt-get update -y
apt-get install wget -y
apt-get install nano -y
apt-get install x11-apps -y
apt-get install tightvncserver -y


# Install Node
echo "############## INSTALATION NODE ###################"
# curl -sL https://deb.nodesource.com/setup_current.x | bash -
apt-get install -y nodejs


# Install Java
echo "############## INSTALATION JAVA ###################"
apt-get install openjdk-17-jdk -y
echo "export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64" >> ~/.bashrc
echo "export PATH=\$JAVA_HOME/bin:\$PATH" >> ~/.bashrc
. ~/.bashrc
curl -s http://localhost:8080/pluginManager/api/json?depth=1 \
  | jq -r '.plugins[] | "\(.shortName):\(.version)"' \
  | sort

# Setup VNC server
echo "############## SETUP VNC SERVER ###################"
mkdir -p ~/.vnc && echo -n "hackme123" | vncpasswd -f > ~/.vnc/passwd
chmod 600 ~/.vnc/passwd
USER=admin vncserver :1 -auth /root/.Xauthority -geometry 1024x768 -depth 24 -rfbwait 120000 -rfbauth /root/.vnc/passwd -fp /usr/share/fonts/X11/misc/,/usr/share/fonts/X11/Type1/,/usr/share/fonts/X11/75dpi/,/usr/share/fonts/X11/100dpi/ -co /etc/X11/rgb
export DISPLAY=:1
xhost +
# ps aux | grep Xtightvnc
# echo $DISPLAY
# rm -f /tmp/.X1-lock && rm -f /tmp/.X11-unix/X1
# ps aux | grep Xtightvnc
# echo $DISPLAY
# vncserver -kill :1
# rm -f /tmp/.X1-lock
# rm -f /tmp/.X11-unix/X1


# Install Google Chrome
echo "############## INSTALATION CHROME ###################"
wget https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb
dpkg -i google-chrome-stable_current_amd64.deb
apt-get install -f -y
dpkg -i google-chrome-stable_current_amd64.deb


# Install ChromeDriver
echo "############## INSTALATION CHROMEDRIVER ###################"
CHROME_DRIVER_VERSION=`curl -sS https://chromedriver.storage.googleapis.com/LATEST_RELEASE`
wget https://chromedriver.storage.googleapis.com/$CHROME_DRIVER_VERSION/chromedriver_linux64.zip
unzip chromedriver_linux64.zip
rm chromedriver_linux64.zip
mv chromedriver /usr/bin/chromedriver
chown root:root /usr/bin/chromedriver
chmod +x /usr/bin/chromedriver
apt-get install -y libglib2.0-0
apt-get install -y libnss3


# Install Python
echo "############## INSTALATION PYTHON ###################"
apt-get install -y python3 python3-pip
if [ -f /var/jenkins_home/requirements.txt ]; then
    pip3 install -r /var/jenkins_home/requirements.txt  --break-system-packages
else
    echo "Nie znaleziono pliku /var/jenkins_home/requirements.txt"
fi

echo "##############INSTALATION SUCCESS###################"
wget --version
nano --version
node --version
java --version
google-chrome --version
chromedriver --version
python3 --version
pip3 --version
pip3 freeze
echo "##############INSTALATION SUCCESS###################"