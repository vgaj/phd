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

import logging
import subprocess

from fastmcp import FastMCP
from mcp.types import TextContent 

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

server_name = "network_data_server"
server_instructions = """
This MCP server provides a search capability for chat and deep research connectors. 
Use the search tool to find the sizes of data sent to a given IP address.
"""
tool_name_sizes = "get_data_sizes_for_ip_address"
tool_description_sizes = """
Returns the sizes of data sent to a given IP address.
The sizes are delimited by a comma.
Use this to obtain the sizes of data sent to a given IP address.
"""
tool_name_intervals = "get_intervals_for_ip_address"
tool_description_intervals = """
Returns the intervals at which data is sent to a given IP address.
The sizes are delimited by a comma.
Use this to obtain intervals at which data sent to a given IP address.
"""
def create_server():

    mcp = FastMCP(name=server_name,
                  instructions=server_instructions)

    @mcp.tool(name=tool_name_sizes, description=tool_description_sizes)
    def get_sizes(ip_address: str = "") -> TextContent:
        logger.info(f"Searching sizes for: '{ip_address}'")
        result = subprocess.check_output( ["/tmp/elastic_query_sizes_for_address.sh", ip_address], text=True)
        return TextContent(type="text", text=result)

    @mcp.tool(name=tool_name_intervals, description=tool_description_intervals)
    def get_intervals(ip_address: str = "") -> TextContent:
        logger.info(f"Searching intervals for: '{ip_address}'")
        result = subprocess.check_output( ["/tmp/elastic_query_intervals_for_address.sh", ip_address], text=True)
        return TextContent(type="text", text=result)

    return mcp

server = create_server()

def main():

    try:
        server.run()
    except KeyboardInterrupt:
        logger.info("Server stopped by user")
    except Exception as e:
        logger.error(f"Server error: {e}")
        raise

if __name__ == "__main__":
    main()

