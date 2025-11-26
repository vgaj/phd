#!/bin/bash
sudo apt install python3-pip
sudo apt  install golang-go
pip3 install fastmcp --break-system-packages
go install github.com/mark3labs/mcphost@latest
cp .mcphost.json ~/
cp mcp_server.py /tmp

