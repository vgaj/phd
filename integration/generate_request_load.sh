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

sleep_time=10
firefox_command="firefox -private -headless"

if [ "$UID" -eq 0 ]; then
    echo "This needs to be run as the logged in user not root"
    exit 1
fi

pgrep firefox > /dev/null
if [ $? -eq 0 ]; then
  echo "Not starting as firefox is currently running"
  exit 1
fi

echo "Starting background load, use Ctrl-C to stop"

finished=false

trap ctrl_c INT
function ctrl_c() {
  echo "Stopping background load, can take up to ${sleep_time} seconds"
  finished=true
}
while [ "$finished" = "false" ]
do
  if [ "$finished" = false ] ; then
    $firefox_command https://www.abc.net.au 2> /dev/null &
    sleep 1
    $firefox_command https://www.msnbc.com 2> /dev/null &
    sleep 1
    $firefox_command https://www.bbc.com/news 2> /dev/null &
    sleep ${sleep_time}
    pkill --signal TERM firefox
  fi

  if [ "$finished" = false ] ; then
    $firefox_command https://edition.cnn.com 2> /dev/null &
    sleep 1
    $firefox_command https://www.msnbc.com 2> /dev/null &
    sleep 1
    $firefox_command https://askubuntu.com 2> /dev/null &
    sleep ${sleep_time}
    pkill --signal TERM firefox
  fi

  if [ "$finished" = false ] ; then
    $firefox_command https://stackoverflow.com 2> /dev/null &
    sleep 1
    $firefox_command https://serverfault.com 2> /dev/null &
    sleep 1
    $firefox_command https://superuser.com 2> /dev/null &
    sleep ${sleep_time}
    pkill --signal TERM firefox
  fi

  if [ "$finished" = false ] ; then
    $firefox_command https://stackexchange.com 2> /dev/null &
    sleep 1
    $firefox_command https://www.reddit.com 2> /dev/null &
    sleep 1
    $firefox_command https://www.trivago.com.au 2> /dev/null &
    sleep ${sleep_time}
    pkill --signal TERM firefox
  fi

  if [ "$finished" = false ] ; then
    $firefox_command https://hotels.com 2> /dev/null &
    sleep 1
    $firefox_command https://www.expedia.com 2> /dev/null &
    sleep 1
    $firefox_command https://www.booking.com 2> /dev/null &
    sleep ${sleep_time}
    pkill --signal TERM firefox
  fi
done

pkill --signal TERM firefox