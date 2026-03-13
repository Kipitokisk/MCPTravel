#!/usr/bin/env python3
"""
MCPTravel AI Agent (Ollama - Free Local Version)
Uses local LLM via Ollama to interact with the MCPTravel API.
"""

import os
import json
import requests
import re
from dotenv import load_dotenv

load_dotenv()

# Configuration
API_BASE_URL = os.getenv("API_BASE_URL", "http://localhost:8080")
OLLAMA_URL = os.getenv("OLLAMA_URL", "http://localhost:11434")
MODEL = os.getenv("OLLAMA_MODEL", "llama3.1")


def fetch_mcp_tools() -> tuple:
    """Fetch tool definitions from MCP discovery endpoint."""
    tools_text = ""
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

            # Store tool data for dynamic execution
            for tool in tools:
                name = tool.get("name")
                tools_data[name] = {
                    "endpoint": tool.get("endpoint"),
                    "method": tool.get("method", "GET"),
                    "parameters": {p["name"]: p for p in tool.get("parameters", [])}
                }

            # Format tools for the prompt
            tools_text = "AVAILABLE TOOLS (from MCP Discovery):\n\n"
            for tool in tools:
                name = tool.get("name")
                desc = tool.get("description")
                params = tool.get("parameters", [])

                tools_text += f"• {name}: {desc}\n"
                tools_text += f"  Endpoint: {tool.get('method')} {tool.get('endpoint')}\n"
                if params:
                    param_strs = [f"{p['name']} ({p['type']})" for p in params]
                    tools_text += f"  Params: {', '.join(param_strs)}\n"
                tools_text += "\n"

    except Exception as e:
        print(f"Warning: Could not fetch MCP tools: {e}")

    return tools_text, tools_data, service_info


# Fetch tools from MCP endpoint
MCP_TOOLS_TEXT, MCP_TOOLS_DATA, MCP_SERVICE_INFO = fetch_mcp_tools()

TOOLS_DESCRIPTION = f"""
{MCP_TOOLS_TEXT}
HOW TO USE TOOLS:
To call a tool, respond with JSON:
{{"tool": "tool_name", "params": {{"param1": "value1"}}}}

RULES:
- company_id/companyId must be a NUMBER, never a name
- If user asks for menu/details by NAME, first search to get the ID
- Respond with ONLY the JSON when using a tool
- You can chain tools: after search results, use the ID for menu/details
"""

SYSTEM_PROMPT = f"""You are a helpful assistant for {MCP_SERVICE_INFO['name']}, a {MCP_SERVICE_INFO['description']}.

{TOOLS_DESCRIPTION}

IMPORTANT RULES:
- If you need to use a tool, respond ONLY with the JSON, nothing else.
- After receiving tool results, provide a natural, friendly response WITHOUT any JSON.
- Be concise and helpful. Prices are in Moldovan Lei (MDL).
- If the user asks something you can answer without tools, just answer directly.
"""


def execute_tool(tool_name: str, params: dict) -> dict:
    """Execute a tool dynamically using MCP tool definitions."""
    try:
        # Get tool definition from MCP data
        tool_def = MCP_TOOLS_DATA.get(tool_name)
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

        # Build full URL
        url = f"{API_BASE_URL}{endpoint}"

        # Separate path params from query params
        query_params = {}
        for key, value in params.items():
            # Skip if it was a path parameter
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


def extract_tool_call(text: str) -> tuple:
    """Extract tool call JSON from response."""
    text = text.strip()

    # Try parsing the entire response as JSON first
    try:
        data = json.loads(text)
        if "tool" in data:
            return data.get("tool"), data.get("params", {})
    except json.JSONDecodeError:
        pass

    # Look for JSON pattern in response
    json_pattern = r'\{[^{}]*"tool"\s*:\s*"[^"]+"\s*,\s*"params"\s*:\s*\{[^{}]*\}\s*\}'
    matches = re.findall(json_pattern, text, re.DOTALL)

    for match in matches:
        try:
            data = json.loads(match)
            if "tool" in data:
                return data.get("tool"), data.get("params", {})
        except json.JSONDecodeError:
            continue

    return None, None


def chat_ollama(messages: list) -> str:
    """Send messages to Ollama and get response."""
    response = requests.post(
        f"{OLLAMA_URL}/api/chat",
        json={
            "model": MODEL,
            "messages": messages,
            "stream": False
        }
    )

    if response.status_code == 200:
        return response.json()["message"]["content"]
    else:
        raise Exception(f"Ollama error: {response.text}")


def clean_response(text: str) -> str:
    """Remove tool call JSON from response text."""
    # Remove JSON tool calls from the visible response
    json_pattern = r'\{[^{}]*"tool"[^{}]*\}'
    cleaned = re.sub(json_pattern, '', text, flags=re.DOTALL)
    # Clean up extra whitespace
    cleaned = re.sub(r'\n\s*\n', '\n\n', cleaned)
    return cleaned.strip()


def chat(user_message: str, conversation_history: list, context: dict = None) -> str:
    """Process user message, handle tool calls, return response."""
    if context is None:
        context = {}

    # Add context hint if we have previous company info
    message_with_context = user_message
    if context.get("last_company_id") and context.get("last_company_name"):
        message_with_context = f"{user_message}\n[Context: Last discussed company was '{context['last_company_name']}' with id={context['last_company_id']}]"

    # Add user message
    conversation_history.append({
        "role": "user",
        "content": message_with_context
    })

    # Build messages with system prompt
    messages = [{"role": "system", "content": SYSTEM_PROMPT}] + conversation_history

    # Get initial response
    response = chat_ollama(messages)

    # Check if response contains a tool call
    tool_name, params = extract_tool_call(response)

    # Handle up to 3 tool calls in a row
    attempts = 0
    while tool_name and attempts < 3:
        attempts += 1
        print(f"[Calling {tool_name}...]")

        # Execute tool
        tool_result = execute_tool(tool_name, params or {})

        # Format result nicely
        result_str = json.dumps(tool_result, indent=2, ensure_ascii=False)
        if len(result_str) > 3000:
            result_str = result_str[:3000] + "...(truncated)"

        # Build hint based on results
        hint = "Summarize the results helpfully."
        company_id = None

        if tool_result.get("data") and isinstance(tool_result["data"], list) and len(tool_result["data"]) > 0:
            user_query = conversation_history[0]["content"].lower() if conversation_history else ""
            first_result = tool_result["data"][0]
            company_id = first_result.get("id")
            company_name = first_result.get("name")

            # Store in context for follow-up questions
            if company_id:
                context["last_company_id"] = company_id
                context["last_company_name"] = company_name

            if company_id and any(word in user_query for word in ["menu", "price", "cost", "food", "drink"]):
                hint = f"Found company id={company_id}. Get the menu: {{\"tool\": \"get_company_menu\", \"params\": {{\"company_id\": {company_id}}}}}"
            else:
                hint = "List the businesses found with name, address, status."
        elif isinstance(tool_result, list):
            hint = "Present the items with prices nicely formatted."

        # Store tool result as assistant's knowledge (not as separate message)
        # This helps maintain cleaner context
        conversation_history.append({
            "role": "assistant",
            "content": f"[Used {tool_name}. Results: {result_str[:500]}{'...' if len(result_str) > 500 else ''}]\n\n{hint}"
        })

        # Add system instruction for next response
        conversation_history.append({
            "role": "user",
            "content": "Based on the above data, respond naturally to my original question. NO JSON. Remember: company_id=" + str(company_id) + " for future reference."
        })

        # Get next response
        messages = [{"role": "system", "content": SYSTEM_PROMPT}] + conversation_history
        response = chat_ollama(messages)

        # Check for another tool call
        tool_name, params = extract_tool_call(response)

    # Clean any remaining JSON from response
    response = clean_response(response)

    # Add response to history
    conversation_history.append({
        "role": "assistant",
        "content": response
    })

    return response


def check_ollama():
    """Check if Ollama is running."""
    try:
        response = requests.get(f"{OLLAMA_URL}/api/tags")
        if response.status_code == 200:
            models = [m["name"] for m in response.json().get("models", [])]
            return True, models
        return False, []
    except:
        return False, []


def main():
    """Main chat loop."""
    print("=" * 50)
    print("MCPTravel AI Assistant (Local - Ollama)")
    print("=" * 50)

    # Check MCP endpoint
    try:
        resp = requests.get(f"{API_BASE_URL}/api/discovery/tools")
        if resp.status_code == 200:
            mcp_info = resp.json()
            print(f"Connected to: {mcp_info.get('service')} (MCP v{mcp_info.get('mcp_version')})")
            print(f"Tools available: {len(mcp_info.get('tools', []))}")
        else:
            print("Warning: Backend not responding. Start with: docker-compose up -d")
    except:
        print("Warning: Cannot connect to backend. Start with: docker-compose up -d")

    # Check Ollama
    running, models = check_ollama()
    if not running:
        print("\nError: Ollama is not running!")
        print("Install from: https://ollama.ai")
        print("Then run: ollama pull llama3.1")
        return

    if MODEL not in [m.split(":")[0] for m in models]:
        print(f"\nModel '{MODEL}' not found. Pulling it now...")
        os.system(f"ollama pull {MODEL}")

    print(f"\nUsing model: {MODEL}")
    print("Ask me about restaurants, cafes, bars, and more!")
    print("Type 'quit' to exit.\n")

    conversation_history = []
    context = {"last_company_id": None, "last_company_name": None}  # Remember context

    while True:
        try:
            user_input = input("You: ").strip()

            if not user_input:
                continue
            if user_input.lower() in ["quit", "exit", "q"]:
                print("Goodbye!")
                break

            response = chat(user_input, conversation_history, context)
            print(f"\nAssistant: {response}\n")

        except KeyboardInterrupt:
            print("\nGoodbye!")
            break
        except Exception as e:
            print(f"\nError: {e}\n")


if __name__ == "__main__":
    main()
