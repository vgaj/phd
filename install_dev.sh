#!/bin/bash

#echo "*** Installing..."
#sudo dpkg -i deb/target/phone-home-detector-0.0.1-SNAPSHOT.deb

sudo mkdir -p /opt/phone-home-detector
cwd=$(pwd)

echo "*** Linking files..."
sudo ln -s $cwd/server/target/phd-server-0.0.1-SNAPSHOT.jar /opt/phone-home-detector/phd-server-0.0.1-SNAPSHOT.jar
sudo ln -s $cwd/cli/target/phd-cli-0.0.1-SNAPSHOT-jar-with-dependencies.jar /opt/phone-home-detector/phd-cli-0.0.1-SNAPSHOT-jar-with-dependencies.jar
sudo ln -s $cwd/server/target/phone_home_detector_bpf_count.o /opt/phone-home-detector/phone_home_detector_bpf_count.o
sudo ln -s $cwd/server/target/phone_home_detector_bpf_time.o /opt/phone-home-detector/phone_home_detector_bpf_time.o
sudo ln -s $cwd/server/src/main/resources/phone-home-detector-service-start.sh /opt/phone-home-detector/phone-home-detector-service-start.sh
sudo ln -s $cwd/server/src/main/resources/phone-home-detector-service-stop.sh /opt/phone-home-detector/phone-home-detector-service-stop.sh
sudo ln -s $cwd/server/src/main/resources/phone-home-detector /usr/bin/phone-home-detector
sudo ln -s $cwd/server/src/main/resources/phone-home-detector.service /usr/lib/systemd/system/phone-home-detector.service

echo "*** Starting..."
sudo systemctl daemon-reload
sudo systemctl enable phone-home-detector --now
sudo systemctl start phone-home-detector.service