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

watch -n 0.2 '
bpftool map dump name phd_ip_to_pid | grep key | wc -l | echo IP to PID Entires: $(cat -) &&
bpftool map dump name phd_ip_to_bytes | grep key | wc -l | echo IP to Bytes Entires: $(cat -) &&
bpftool map dump name phd_ip_to_bytes | grep value |  cut -d : -f 2 | awk '"'"'{sum+=$1} END {print sum}'"'"' | echo Total Bytes: $(cat -)
'
