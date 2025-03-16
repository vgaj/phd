#!/bin/bash
# MIT License
#
# Copyright (c) 2022-2025 Viru Gajanayake
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.

if [ "$UID" -ne 0 ]; then
    echo "This needs to be run as root"
    exit 1
fi

echo "*** Stopping..."
sudo systemctl stop phone-home-detector-ui.service
sudo systemctl stop phone-home-detector.service

sudo mkdir -p /usr/share/phone-home-detector
cwd=$(pwd)

echo "*** Linking / Copying files..."
# This is accessed by a non-root user
sudo cp -f $cwd/ui/target/phd-ui.jar /usr/share/phone-home-detector/phd-ui.jar
sudo ln -f -s $cwd/server/target/phd-server.jar /usr/share/phone-home-detector/phd-server.jar
sudo ln -f -s $cwd/cli/target/phd-cli-jar-with-dependencies.jar /usr/share/phone-home-detector/phd-cli-jar-with-dependencies.jar
sudo ln -f -s $cwd/server/src/main/bpf/phone_home_detector_bpf_count.c /usr/share/phone-home-detector/phone_home_detector_bpf_count.c
sudo ln -f -s $cwd/server/src/main/bpf/phone_home_detector_bpf_pid.c /usr/share/phone-home-detector/phone_home_detector_bpf_pid.c
sudo ln -f -s $cwd/server/src/main/resources/phone-home-detector-service-start.sh /usr/share/phone-home-detector/phone-home-detector-service-start.sh
sudo ln -f -s $cwd/server/src/main/resources/phone-home-detector-service-stop.sh /usr/share/phone-home-detector/phone-home-detector-service-stop.sh
sudo cp -f $cwd/server/src/main/resources/phone-home-detector-config.sh /usr/share/phone-home-detector/phone-home-detector-config.sh

sudo ln -f -s $cwd/server/src/main/resources/phone-home-detector /usr/bin/phone-home-detector
sudo cp -f $cwd/server/src/main/resources/phone-home-detector.service /usr/lib/systemd/system/phone-home-detector.service
sudo cp -f $cwd/ui/src/main/resources/phone-home-detector-ui.service /usr/lib/systemd/system/phone-home-detector-ui.service

echo "*** Clean up..."
if [ -f "/usr/share/phone-home-detector/phone_home_detector_bpf_count.o" ]; then
  rm /usr/share/phone-home-detector/phone_home_detector_bpf_count.o
fi
if [ -f "/usr/share/phone-home-detector/phone_home_detector_bpf_pid.o" ]; then
  rm /usr/share/phone-home-detector/phone_home_detector_bpf_pid.o
fi

echo "*** Starting..."
sudo systemctl daemon-reload
sudo systemctl enable phone-home-detector.service
sudo systemctl enable phone-home-detector-ui.service
sudo systemctl start phone-home-detector.service
sudo systemctl start phone-home-detector-ui.service