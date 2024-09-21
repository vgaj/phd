#!/bin/bash
# MIT License
#
# Copyright (c) 2022-2024 Viru Gajanayake
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

echo "Removing old results"
systemctl stop phone-home-detector
sleep 2
rm /usr/share/phone-home-detector/results.xml
sleep 2

echo "Starting"
systemctl start phone-home-detector
sleep 2

requestCount=3
for ((i=1; i<=requestCount; i++)); do
  echo "Making request ${i} of ${requestCount}"
  url="https://8.8.8.8"
  curl $url &> /dev/null
  sleep 120
done
sleep 1

echo "Checking"
cliResult=$(phone-home-detector)

if [[ $cliResult == *"8.8.8.8 (dns.google)"* ]]; then
  echo "PASS"
else
  echo "FAIL"
  echo "$cliResult"
fi
