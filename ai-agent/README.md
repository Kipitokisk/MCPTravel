# MCPTravel AI Agent

AI-powered assistant for discovering businesses using natural language.

Two versions available:
- **Ollama** - Free, runs locally (requires decent hardware)
- **Claude API** - Paid, fast cloud-based (recommended)

---

## Quick Start

### 1. Install Dependencies

```bash
cd ai-agent
pip install -r requirements.txt
```

### 2. Configure Environment

```bash
cp .env.example .env
```

Edit `.env` with your settings.

### 3. Start Backend

```bash
cd ..
docker-compose up -d
```

### 4. Run Agent

**Claude API (recommended):**
```bash
python agent_claude.py
```

**Ollama (free/local):**
```bash
python agent_ollama.py
```

---

## Claude API Setup (Paid)

### Get API Key

1. Go to https://console.anthropic.com
2. Sign up / Log in
3. Navigate to API Keys
4. Create new key
5. Add credits ($5 minimum)

### Configure

Add to `.env`:
```env
ANTHROPIC_API_KEY=sk-ant-api03-your-key-here
CLAUDE_MODEL=claude-haiku-4-5-20251001
```

### Available Models

| Model | Cost (Input) | Cost (Output) | Best For |
|-------|--------------|---------------|----------|
| `claude-haiku-4-5-20251001` | $1/1M | $5/1M | Development, fast queries |
| `claude-sonnet-4-20250514` | $3/1M | $15/1M | Balanced performance |
| `claude-opus-4-20250514` | $15/1M | $75/1M | Complex reasoning |

### Run

```bash
python agent_claude.py
```

---

## Ollama Setup (Free)

### Install Ollama

Download from https://ollama.ai

### Pull a Model

```bash
ollama pull llama3.1
```

**Recommended models by RAM:**

| RAM | Model | Command |
|-----|-------|---------|
| 8GB | Mistral 7B | `ollama pull mistral` |
| 16GB | LLaMA 3.1 8B | `ollama pull llama3.1` |
| 32GB | Qwen 2.5 32B | `ollama pull qwen2.5:32b` |

### Configure

Add to `.env`:
```env
OLLAMA_URL=http://localhost:11434
OLLAMA_MODEL=llama3.1
```

### Run

```bash
python agent_ollama.py
```

---

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `API_BASE_URL` | MCPTravel backend URL | `http://localhost:8080` |
| `ANTHROPIC_API_KEY` | Claude API key | - |
| `CLAUDE_MODEL` | Claude model to use | `claude-haiku-4-5-20251001` |
| `OLLAMA_URL` | Ollama server URL | `http://localhost:11434` |
| `OLLAMA_MODEL` | Ollama model to use | `llama3.1` |

---

## Example Queries

```
You: What restaurants are open right now?
You: Find me a cafe nearby
You: Show me the menu for La Placinte
You: What bars are there?
You: Find pizza places
You: What's the cheapest coffee?
You: What categories do you have?
```

---

## Commands

| Command | Description |
|---------|-------------|
| `quit` / `exit` / `q` | Exit the agent |
| `clear` | Clear conversation history (Claude only) |

---

## Troubleshooting

### Backend not responding
```bash
docker-compose up -d
curl http://localhost:8080/api/discovery/tools
```

### Ollama not running
```bash
ollama serve
```

### Claude API error
- Check your API key is correct
- Verify you have credits: https://console.anthropic.com
- Check model name spelling

### No tools loaded
Make sure backend is running and accessible:
```bash
curl http://localhost:8080/api/discovery/tools
```
