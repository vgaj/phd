import logging

from fastmcp import FastMCP
from mcp.types import TextContent 

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

server_name = "network_data_server"
server_instructions = """
This MCP server provides a search capability for chat and deep research connectors. 
Use the search tool to find the sizes of data sent to a given IP address.
"""
tool_name = "get_data_sizes_for_ip_address"
tool_description = """
Returns the sizes of data sent to a given IP address.
The sizes are delimited by a comma.
Use this to obtain the sizes of data sent to a given IP address.
"""
def create_server():

    mcp = FastMCP(name=server_name,
                  instructions=server_instructions)

    #@mcp.tool(name=tool_name,description=tool_description)
    @mcp.tool(name=tool_name)
    def get_sizes(ip_address: str = "") -> TextContent:
        logger.info(f"Searching for query: '{ip_address}'")           
        return TextContent(type="text", text="32,33,33,33,32,33")

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

