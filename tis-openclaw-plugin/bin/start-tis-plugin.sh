#!/bin/bash
# TIS OpenClaw Plugin Startup Script

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PLUGIN_HOME="$(dirname "$SCRIPT_DIR")"

# Set environment variables
export TIS_PLUGIN_HOME="$PLUGIN_HOME"
export TIS_MCP_ENDPOINT="${TIS_MCP_ENDPOINT:-http://localhost:8080/mcp}"

echo "TIS OpenClaw Plugin"
echo "==================="
echo "Plugin Home: $PLUGIN_HOME"
echo "MCP Endpoint: $TIS_MCP_ENDPOINT"
echo ""
echo "This plugin provides TIS data integration capabilities via MCP protocol."
echo "Available tools:"
echo "  - list_plugin_types: List available TIS plugin types"
echo "  - create_plugin_instance: Create TIS plugin instances"
echo "  - get_plugin_schema: Get plugin JSON schema"
echo "  - create_pipeline: Create data sync pipeline"
echo ""
echo "Plugin is ready. OpenClaw can now use TIS skills via MCP."
