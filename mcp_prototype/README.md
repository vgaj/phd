## What is it?
The Phone Home Detector processes data grouped by source address, destination address, minute-level timestamp, and data size. 
This prototype adds an experimental AI-based capability to identify patterns. 
A copy of all data is stored in persistent storage, which can be queried by an MCP server.

## Steps
1. Ensure phone-home-detector is set up by following the instructions in the parent directory.
2. Set up phone-home-detector to store a copy the data in Elastic Search ```./1_setup_insecure_elastic_search```
3. Set up the data to be queried via an MCP server.
4. Add credentials for the LLM (tested with OpenAI GPT-4) ```export OPENAI_API_KEY='your-openai-key'```
5. Query the data using ```.\run_query.sh``` as an example
