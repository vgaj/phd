#!/bin/sh
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

# Because of lintian errors and problems building on launchpad.net
# we package the .c and build on start rather than packaging the .o
mkdir -p /tmp/phone-home-detector
if [ ! -f "/tmp/phone-home-detector/phone_home_detector_bpf_count.o" ]; then
  clang -O2 -g -target bpf -c /usr/share/phone-home-detector/phone_home_detector_bpf_count.c -o /tmp/phone-home-detector/phone_home_detector_bpf_count.o
  if [ $? -ne 0 ]; then
    echo "ERROR failed build phone_home_detector_bpf_count.c"
    exit 1
  fi
fi
if [ ! -f "/tmp/phone-home-detector/phone_home_detector_bpf_pid.o" ]; then
  clang -O2 -g -target bpf -c /usr/share/phone-home-detector/phone_home_detector_bpf_pid.c -o /tmp/phone-home-detector/phone_home_detector_bpf_pid.o
  if [ $? -ne 0 ]; then
    echo "ERROR failed build phone_home_detector_bpf_pid.c"
    exit 1
  fi
fi

interfaces=$(nmcli -t dev | grep :connected: | awk -F':' '{print $1}')

for interface in $interfaces; do
  echo "Checking $interface ..."
  tc qdisc show dev $interface | grep clsact > /dev/null
  if [ $? -eq 1 ]; then
      tc qdisc add dev $interface clsact
      if [ $? -eq 0 ]; then
        tc filter add dev $interface egress bpf da obj /tmp/phone-home-detector/phone_home_detector_bpf_count.o sec phone_home_detector_bpf_count
        if [ $? -eq 0 ]; then
          echo "Attached BPF program for $interface"
        else
          echo "ERROR failed to add BPF program for $interface"
        fi
      else
        echo "ERROR failed to add clsact for $interface"
      fi
  else
    echo "ERROR $interface already has a qdisc clsact, not modifying"
  fi
done

/usr/sbin/bpftool prog load /tmp/phone-home-detector/phone_home_detector_bpf_pid.o /sys/fs/bpf/phd_connect_pid autoattach
if [ $? -eq 0 ]; then
  echo "Attached BPF program for IP to PID tracking"
else
  echo "ERROR failed to add BPF program for IP to PID tracking"
fi

/usr/bin/java -Djava.net.preferIPv4Stack=true -jar /usr/share/phone-home-detector/phd-server.jar

