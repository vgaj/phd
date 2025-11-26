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

curl -X GET "localhost:9200/monitor_index/_search?pretty" -H 'Content-Type: application/json' -d' { "query": { "match_all": {} } }'

curl -X GET "localhost:9200/monitor_index/_search?pretty" -H 'Content-Type: application/json' -d'
{
  "_source": ["length","epochMinute"],
  "query": {
    "match": {
      "destination": "8.8.8.8"
    }
  }
}' | jq '
  .hits.hits
  | map({epochMinute: ._source.epochMinute, length: ._source.length})
  | sort_by(.epochMinute)
  | . as $a
  | (
      [range(1; $a|length)
       | {delta: ($a[.].epochMinute - $a[.-1].epochMinute),
          length: $a[.].length,}]
    )
'
