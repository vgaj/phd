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

if [ -z "${BG_LOAD_SVR}" ]; then
    echo "Environment variable BG_LOAD_SVR needs to be set."
    echo "That server needs to be running: sudo nc -lk 80 > /dev/null"
    echo "Use: export BG_LOAD_SVR=\"192.168.1.2\""
    exit 1
fi

echo "Starting background load, use Ctrl-C to stop"

bgCommand="nc ${BG_LOAD_SVR} 80 < /dev/zero"
pkill -f "$bgCommand"

trap ctrl_c INT
function ctrl_c() {
  echo "Stopping background load"
  pkill -f "$bgCommand"
}

echo "running: ${bgCommand}"
eval "$bgCommand"
