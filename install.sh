#!/bin/bash

echo "*** Stopping..."
sudo systemctl stop phd.service

echo "*** Deploying..."
sudo mkdir -p /opt/phd
cwd=$(pwd)
sudo rm -f /opt/phd/phonehomedetector-0.0.1-SNAPSHOT.jar
#sudo cp target/phonehomedetector*.jar /opt/phd/
sudo ln -s $cwd/target/phonehomedetector-0.0.1-SNAPSHOT.jar /opt/phd/phonehomedetector-0.0.1-SNAPSHOT.jar
sudo cp src/main/resources/phd.service /etc/systemd/system/

echo "*** Enabling..."
sudo systemctl daemon-reload
sudo systemctl enable --now phd.service

