## AI-assisted approach to detecting patterns in network traffic
This is an experimental AI-assisted approach to detecting patterns in network traffic.

The Phone Home Detector (see parent directory) analyses traffic between IP address pairs. 
It aggregates traffic into one-minute buckets and applies a set of fixed rules based on byte counts and transmission intervals.

This prototype adds an experimental, AI-based capability for identifying patterns.
This extension exposes transmission size and timing data through an MCP tool, making it queryable by an LLM.
The goal is to explore whether an LLM can identify patterns that are difficult to capture using static rules alone.

## Steps
1. Ensure phone-home-detector is set up by following the instructions in the parent directory.
2. Set up phone-home-detector to store a copy the data in Elastic Search ```./1_setup_insecure_elastic_search.sh```
3. Set up the data to be queried via an MCP server ```./2_setup_mcp_server_and_host.sh```
4. Add credentials for the LLM (tested with OpenAI GPT-4) ```export OPENAI_API_KEY='your-openai-key'```
5. Query the data using ```.\3_run_query.sh``` as an example

## Output
Here is an example of the output that it produces:
```The data sizes sent to IP address 91.189.91.49 are mostly consistent at 200, after an initial size of 168, and the intervals at which they are sent vary without any apparent pattern.```