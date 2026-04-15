#!/usr/bin/env python3
"""
MCPTravel AI Agent (Claude API Version)
Uses Claude API with native tool use to interact with the MCPTravel API.
"""

import os
import json
import requests
import anthropic
from dotenv import load_dotenv

load_dotenv()

# Configuration
API_BASE_URL = os.getenv("API_BASE_URL", "http://localhost:8080")
ANTHROPIC_API_KEY = os.getenv("ANTHROPIC_API_KEY")
MODEL = os.getenv("CLAUDE_MODEL", "claude-haiku-4-5-20251001")


def fetch_mcp_tools() -> tuple:
    """Fetch tool definitions from MCP discovery endpoint and convert to Claude format."""
    claude_tools = []
    tools_data = {}
    service_info = {"name": "MCPTravel", "description": "business discovery service"}

    try:
        response = requests.get(f"{API_BASE_URL}/api/discovery/tools")
        if response.status_code == 200:
            mcp_data = response.json()
            tools = mcp_data.get("tools", [])
            service_info = {
                "name": mcp_data.get("service", "MCPTravel"),
                "description": mcp_data.get("description", "business discovery service")
            }

            for tool in tools:
                name = tool.get("name")

                # Store tool data for execution
                tools_data[name] = {
                    "endpoint": tool.get("endpoint"),
                    "method": tool.get("method", "GET"),
                    "parameters": {p["name"]: p for p in tool.get("parameters", [])}
                }

                # Convert to Claude tool format
                properties = {}
                required = []

                for param in tool.get("parameters", []):
                    param_name = param["name"]
                    param_type = param.get("type", "string")

                    # Map types to JSON schema types
                    json_type = "string"
                    if param_type in ["number", "integer"]:
                        json_type = "number"
                    elif param_type == "boolean":
                        json_type = "boolean"

                    properties[param_name] = {
                        "type": json_type,
                        "description": param.get("description", "")
                    }

                    if param.get("required", False):
                        required.append(param_name)

                claude_tool = {
                    "name": name,
                    "description": tool.get("description", ""),
                    "input_schema": {
                        "type": "object",
                        "properties": properties,
                        "required": required
                    }
                }
                claude_tools.append(claude_tool)

    except Exception as e:
        print(f"Warning: Could not fetch MCP tools: {e}")

    return claude_tools, tools_data, service_info


def execute_tool(tool_name: str, params: dict, tools_data: dict) -> dict:
    """Execute a tool dynamically using MCP tool definitions."""
    try:
        tool_def = tools_data.get(tool_name)
        if not tool_def:
            return {"error": f"Unknown tool: {tool_name}"}

        endpoint = tool_def["endpoint"]
        method = tool_def["method"]

        # Replace path parameters like {id} or {companyId}
        for key, value in params.items():
            placeholder = "{" + key + "}"
            if placeholder in endpoint:
                endpoint = endpoint.replace(placeholder, str(value))
            # Also try camelCase variations
            camel_key = key.replace("_", "")
            placeholder_camel = "{" + camel_key + "}"
            if placeholder_camel in endpoint:
                endpoint = endpoint.replace(placeholder_camel, str(value))

        url = f"{API_BASE_URL}{endpoint}"

        # Separate path params from query params
        query_params = {}
        for key, value in params.items():
            if "{" + key + "}" not in tool_def["endpoint"] and \
               "{" + key.replace("_", "") + "}" not in tool_def["endpoint"]:
                # Convert snake_case to camelCase for API
                camel_key = ''.join(word.capitalize() if i > 0 else word
                                   for i, word in enumerate(key.split('_')))
                query_params[camel_key] = value

        # Make request
        if method == "GET":
            response = requests.get(url, params=query_params)
        elif method == "POST":
            response = requests.post(url, json=params)
        else:
            response = requests.get(url, params=query_params)

        return response.json()

    except requests.RequestException as e:
        return {"error": str(e)}
    except Exception as e:
        return {"error": str(e)}


def chat(user_message: str, conversation_history: list, client: anthropic.Anthropic,
         claude_tools: list, tools_data: dict, service_info: dict, context: dict) -> str:
    """Process user message using Claude API with tool use."""

    system_prompt = f"""You are a helpful assistant for {service_info['name']}, a {service_info['description']}.

You help users find businesses like restaurants, cafes, bars, hotels, and more in Chisinau, Moldova.

IMPORTANT RULES:
- Use the available tools to search for businesses and get information.
- Prices are in Moldovan Lei (MDL).
- Be concise and friendly in your responses.
- When showing results, format them nicely with name, address, and relevant details.
- If a user asks about a menu or details for a business mentioned earlier, use the stored company ID.
- company_id/companyId must always be a NUMBER, never a name string.
"""

    # Add context hint if available
    if context.get("last_company_id") and context.get("last_company_name"):
        system_prompt += f"\n\nCONTEXT: The last discussed company was '{context['last_company_name']}' with id={context['last_company_id']}. Use this ID if user asks about 'their menu', 'this place', etc."

    # Add user message to history
    conversation_history.append({
        "role": "user",
        "content": user_message
    })

    # Call Claude API
    response = client.messages.create(
        model=MODEL,
        max_tokens=1024,
        system=system_prompt,
        tools=claude_tools,
        messages=conversation_history
    )

    # Handle tool use loop
    while response.stop_reason == "tool_use":
        # Find tool use blocks
        tool_uses = [block for block in response.content if block.type == "tool_use"]

        # Add assistant's response to history
        conversation_history.append({
            "role": "assistant",
            "content": response.content
        })

        # Process each tool call
        tool_results = []
        for tool_use in tool_uses:
            tool_name = tool_use.name
            tool_input = tool_use.input

            print(f"[Calling {tool_name}...]")

            # Execute the tool
            result = execute_tool(tool_name, tool_input, tools_data)

            # Store context from results
            if result.get("data") and isinstance(result["data"], list) and len(result["data"]) > 0:
                first_result = result["data"][0]
                if first_result.get("id"):
                    context["last_company_id"] = first_result["id"]
                    context["last_company_name"] = first_result.get("name")

            # Truncate large results - limit to first 10 items if list is large
            if result.get("data") and isinstance(result["data"], list) and len(result["data"]) > 10:
                result["data"] = result["data"][:10]
                result["truncated"] = True
                result["total_count"] = result.get("count", len(result["data"]))

            result_str = json.dumps(result, ensure_ascii=False)

            tool_results.append({
                "type": "tool_result",
                "tool_use_id": tool_use.id,
                "content": result_str
            })

        # Add tool results to history
        conversation_history.append({
            "role": "user",
            "content": tool_results
        })

        # Get next response
        response = client.messages.create(
            model=MODEL,
            max_tokens=1024,
            system=system_prompt,
            tools=claude_tools,
            messages=conversation_history
        )

    # Extract final text response
    final_response = ""
    for block in response.content:
        if hasattr(block, "text"):
            final_response += block.text

    # Add final response to history
    conversation_history.append({
        "role": "assistant",
        "content": response.content
    })

    return final_response


def main():
    """Main chat loop."""
    print("=" * 50)
    print("MCPTravel AI Assistant (Claude API)")
    print("=" * 50)

    # Check API key
    if not ANTHROPIC_API_KEY:
        print("\nError: ANTHROPIC_API_KEY not set!")
        print("Add it to ai-agent/.env:")
        print("ANTHROPIC_API_KEY=sk-ant-xxxxx")
        return

    # Initialize Claude client
    client = anthropic.Anthropic(api_key=ANTHROPIC_API_KEY)

    # Fetch MCP tools
    print("\nConnecting to backend...")
    claude_tools, tools_data, service_info = fetch_mcp_tools()

    if not claude_tools:
        print("Warning: No tools loaded. Make sure backend is running:")
        print("  docker-compose up -d")
    else:
        print(f"Connected to: {service_info['name']}")
        print(f"Tools available: {len(claude_tools)}")

    print(f"\nUsing model: {MODEL}")
    print("Ask me about restaurants, cafes, bars, and more!")
    print("Type 'quit' to exit.\n")

    conversation_history = []
    context = {"last_company_id": None, "last_company_name": None}

    while True:
        try:
            user_input = input("You: ").strip()

            if not user_input:
                continue
            if user_input.lower() in ["quit", "exit", "q"]:
                print("Goodbye!")
                break
            if user_input.lower() == "clear":
                conversation_history = []
                context = {"last_company_id": None, "last_company_name": None}
                print("Conversation cleared.\n")
                continue

            response = chat(
                user_input,
                conversation_history,
                client,
                claude_tools,
                tools_data,
                service_info,
                context
            )
            print(f"\nAssistant: {response}\n")

        except KeyboardInterrupt:
            print("\nGoodbye!")
            break
        except anthropic.APIError as e:
            print(f"\nClaude API Error: {e}\n")
        except Exception as e:
            print(f"\nError: {e}\n")


if __name__ == "__main__":
    main()
