#!/bin/bash
# MIT License
#
# Copyright (c) 2025 Viru Gajanayake
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

if [[ $# -lt 1 ]]; then
  echo "Usage: $0 <DESTINATION_IP>"
  exit 1
fi

DESTINATION_IP="$1"

curl -s "localhost:9200/monitor_index/_search" \
  -H 'Content-Type: application/json' \
  -d @- <<EOF | jq -r '
    .hits.hits
    | map(._source.epochMinute)
    | sort
    | . as $a
    | [range(1;length) | $a[.] - $a[.-1]]
    | @csv
'
{"_source":["epochMinute"],"query":{"match":{"destination":"$DESTINATION_IP"}}}
EOF
