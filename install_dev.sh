#!/bin/bash

echo "*** Stopping..."
sudo systemctl stop phd.service

echo "*** Deploying..."
sudo mkdir -p /opt/phone-home-detector
cwd=$(pwd)
sudo rm -f /opt/phone-home-detector/*.jar
sudo ln -s $cwd/server/target/phd-server-0.0.1-SNAPSHOT.jar /opt/phone-home-detector/phd-server-0.0.1-SNAPSHOT.jar
sudo ln -s $cwd/cli/target/phd-cli-0.0.1-SNAPSHOT-jar-with-dependencies.jar /opt/phone-home-detector/phd-cli-0.0.1-SNAPSHOT-jar-with-dependencies.jar
sudo cp server/src/main/resources/phone-home-detector /usr/bin/phone-home-detector
sudo cp server/src/main/resources/phone-home-detector.service /etc/systemd/system/phd.service

echo "*** Enabling..."
sudo systemctl daemon-reload
sudo systemctl enable --now phd.service

