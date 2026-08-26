---
name: mcp-servers-registry
description: >-
  Comprehensive catalog, directory, and configuration guide for 1000+ Model Context Protocol (MCP) servers
  from awesome-mcp-servers. Use when searching for, configuring, integrating, or creating MCP tool integrations.
---

# Model Context Protocol (MCP) Servers Registry

This skill provides an exhaustive, categorized registry of Model Context Protocol (MCP) servers for extending agent capabilities with tools, databases, APIs, browser automation, filesystem access, and external services.

## Overview
The complete catalog of 1,000+ MCP servers is stored in [catalog.md](./references/catalog.md).

## Common MCP Categories & Tools:
1. **Developer & Git Tools**: GitHub, GitLab, Docker, Kubernetes, Sentry, CI/CD pipelines.
2. **Databases & Storage**: PostgreSQL, SQLite, MySQL, Redis, MongoDB, Supabase, ClickHouse.
3. **Web & Browser Automation**: Puppeteer, Playwright, Brave Search, Fetch, Tavily, Firecrawl.
4. **Knowledge Bases & Notes**: Notion, Obsidian, Linear, Jira, Confluence, Slack.
5. **AI & Vector DBs**: Qdrant, Pinecone, Chroma, Milvus, Weaviate, HuggingFace.

## How to Configure in Antigravity (`mcp_config.json`)

To enable any MCP server globally, add it to `~/.gemini/config/mcp_config.json`:

```json
{
  "mcpServers": {
    "github": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-github"],
      "env": {
        "GITHUB_PERSONAL_ACCESS_TOKEN": "<YOUR_TOKEN>"
      }
    },
    "filesystem": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-filesystem", "C:/UPROJ/32131"]
    },
    "postgres": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-postgres", "postgresql://user:pass@localhost:5432/db"]
    },
    "puppeteer": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-puppeteer"]
    },
    "brave-search": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-brave-search"],
      "env": {
        "BRAVE_API_KEY": "<YOUR_API_KEY>"
      }
    }
  }
}
```

For detailed configurations, arguments, and options for specific tools, refer to [catalog.md](./references/catalog.md).