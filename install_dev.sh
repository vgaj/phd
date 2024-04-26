#!/bin/bash

echo "*** Installing..."
sudo dpkg -i deb/target/phone-home-detector-0.0.1-SNAPSHOT.deb

echo "*** Linking JARs..."
cwd=$(pwd)
sudo rm -f /opt/phone-home-detector/*.jar
sudo rm -f /opt/phone-home-detector/*.0
sudo ln -s $cwd/server/target/phd-server-0.0.1-SNAPSHOT.jar /opt/phone-home-detector/phd-server-0.0.1-SNAPSHOT.jar
sudo ln -s $cwd/server/target/phone_home_detector_bpf_count.o /opt/phone-home-detector/phone_home_detector_bpf_count.o
sudo ln -s $cwd/cli/target/phd-cli-0.0.1-SNAPSHOT-jar-with-dependencies.jar /opt/phone-home-detector/phd-cli-0.0.1-SNAPSHOT-jar-with-dependencies.jar

echo "*** Starting..."
sudo systemctl start phone-home-detector.service