#!/bin/sh
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

/bin/kill -TERM $MAINPID

HOTSPOT_NIC_FILE="$(dirname "$0")/hotspotnic"
echo "$HOTSPOT_NIC_FILE"

if [ -f "$HOTSPOT_NIC_FILE" ] && [ -s "$HOTSPOT_NIC_FILE" ]; then
    HOTSPOT_NIC=$(cat $HOTSPOT_NIC_FILE)
    echo "Removing XDP monitoring on $HOTSPOT_NIC"
    sudo ip link set dev $HOTSPOT_NIC xdp off
    if [ $? -eq 0 ]; then
      echo "Removed XDP BPF program for $HOTSPOT_NIC"
    else
      echo "ERROR failed to remove XDP BPF program for $HOTSPOT_NIC"
    fi

else
    interfaces=$(nmcli -t dev | grep :connected: | awk -F':' '{print $1}')

    for interface in $interfaces; do
      echo "Checking $interface ..."
      tc filter show dev $interface egress | grep phone_home_detector_bpf_count > /dev/null
      if [ $? -eq 0 ]; then
        tc qdisc del dev $interface clsact
        if [ $? -eq 0 ]; then
          echo "Removed BPF program for $interface"
        else
          echo "ERROR failed to remove BPF program for $interface"
        fi
      fi
    done
fi

/usr/bin/rm /sys/fs/bpf/phd_connect_pid
if [ $? -eq 0 ]; then
  echo "Removed BPF program for PID to connection time tracking"
else
  echo "ERROR failed to remove BPF program for PID to connection time tracking"
fi

