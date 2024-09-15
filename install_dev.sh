#!/bin/bash

echo "*** Stopping..."
sudo systemctl stop phone-home-detector.service

sudo mkdir -p /usr/share/phone-home-detector
cwd=$(pwd)

echo "*** Linking files..."
sudo ln -f -s $cwd/server/target/phd-server.jar /usr/share/phone-home-detector/phd-server.jar
sudo ln -f -s $cwd/cli/target/phd-cli-jar-with-dependencies.jar /usr/share/phone-home-detector/phd-cli-jar-with-dependencies.jar
sudo ln -f -s $cwd/server/src/main/bpf/phone_home_detector_bpf_count.c /usr/share/phone-home-detector/phone_home_detector_bpf_count.c
sudo ln -f -s $cwd/server/src/main/bpf/phone_home_detector_bpf_pid.c /usr/share/phone-home-detector/phone_home_detector_bpf_pid.c
sudo ln -f -s $cwd/server/src/main/resources/phone-home-detector-service-start.sh /usr/share/phone-home-detector/phone-home-detector-service-start.sh
sudo ln -f -s $cwd/server/src/main/resources/phone-home-detector-service-stop.sh /usr/share/phone-home-detector/phone-home-detector-service-stop.sh
sudo ln -f -s $cwd/server/src/main/resources/phone-home-detector /usr/bin/phone-home-detector
sudo ln -f -s $cwd/server/src/main/resources/phone-home-detector.service /usr/lib/systemd/system/phone-home-detector.service

echo "*** Clean up..."
if [ -f "/usr/share/phone-home-detector/phone_home_detector_bpf_count.o" ]; then
  rm /usr/share/phone-home-detector/phone_home_detector_bpf_count.o
fi
if [ -f "/usr/share/phone-home-detector/phone_home_detector_bpf_pid.o" ]; then
  rm /usr/share/phone-home-detector/phone_home_detector_bpf_pid.o
fi

echo "*** Starting..."
sudo systemctl daemon-reload
sudo systemctl enable phone-home-detector --now
sudo systemctl start phone-home-detector.service