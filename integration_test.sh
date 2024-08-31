#!/bin/bash

# CONFIGURATION
# The following server needs to be running: sudo nc -lk 80 > /dev/null
BG_LOAD_SVR="192.168.1.103"

if [ "$UID" -ne 0 ]; then
    echo "This needs to be run as root"
    exit 1
fi

echo "Stopping any existing server"
command="java -jar server/target/phd-server.jar"
pkill -f "$command"

echo "Starting background load"
bgCommand="nc ${BG_LOAD_SVR} 80 < /dev/zero"
pkill -f "$bgCommand"
$bgCommand &
bgPid=$!

export phm_minimum_interval_minutes=1

echo "Starting server and waiting for it to start"
$command > /tmp/phd_integration_test_server.out &
serverPid=$!
sleep 10

requestCount=3
for ((i=1; i<=requestCount; i++)); do
  echo "Making request ${i} of ${requestCount}"
  url="https://8.8.8.8"
  curl $url &> /dev/null
  if [ $i -lt $requestCount ]
  then
    sleep 60
  fi
done
sleep 1

cliResult=$(java -jar cli/target/phd-cli-jar-with-dependencies.jar)

echo "Stopping server"
kill $serverPid
kill $bgPid

if [[ $cliResult == *"8.8.8.8 (dns.google)"* ]]; then
  echo "PASS"
else
  echo "FAIL"
  echo $cliResult
fi
