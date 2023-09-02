#!/bin/bash

echo "*** Stopping..."
sudo systemctl stop phd.service

echo "*** Deploying..."
sudo mkdir -p /opt/phd
cwd=$(pwd)
sudo rm -f /opt/phd/*.jar
#sudo cp target/phonehomedetector*.jar /opt/phd/
sudo ln -s $cwd/server/target/phd-server-0.0.1-SNAPSHOT.jar /opt/phd/phd-server-0.0.1-SNAPSHOT.jar
sudo cp server/src/main/resources/phd.service /etc/systemd/system/

echo "*** Enabling..."
sudo systemctl daemon-reload
sudo systemctl enable --now phd.service

