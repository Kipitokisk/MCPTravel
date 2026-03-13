# MCPTravel AI Agent

AI-powered assistant for discovering businesses using natural language.

## Ollama 

### Setup

1. Install Ollama from https://ollama.ai

2. Pull a model:
```bash
ollama pull llama3.1
```

3. Install Python dependencies:
```bash
cd ai-agent
pip install -r requirements.txt
```

4. Start the backend:
```bash
docker-compose up -d
```

5. Run the agent:
```bash
python agent_ollama.py
```

### Alternative Models
Edit `.env` to use different models:
```
OLLAMA_MODEL=mistral
OLLAMA_MODEL=llama3.1:70b
OLLAMA_MODEL=qwen2.5
```

---

## Example Queries

- "What restaurants are open right now?"
- "Find me a cafe"
- "Show me the menu for La Placinte"
- "What bars are there?"
- "Find pizza places"
- "What categories do you have?"
