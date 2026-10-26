# 智能助手（宠物助手）Python 后端开发文档

> 版本：v1.0　|　日期：2026-10-21　|　状态：待开发

## 1. 概述

为宠物平台提供「智能助手」后端服务，前端悬浮球组件已完成对接。后端基于 **DeepSeek 大模型 + RAG 知识库**，支持 **SSE 流式（打字机）输出**，回答宠物饲养、商品咨询、领养救助等问题。

**已完成的前端资产（对接本文档）：**
- `src/api/assistant.js` —— 5 个接口定义
- `src/utils/assistantRequest.js` —— axios 实例（`token` 请求头 + `{code,message,data}` 解析）
- `src/components/assistant/AssistantFloatBall.vue` —— 悬浮球 + 侧边面板，已挂载 `Layout.vue`
- 开发环境 `VITE_ASSISTANT_BASE_URL=http://localhost:8000`

> ⚠️ 前端当前是「整段返回」模式，接入 SSE 需小幅改造（见 §11），后端按本文档实现即可。

## 2. 技术选型

| 层 | 技术 | 选型理由 |
|---|---|---|
| Web 框架 | **FastAPI** | 原生 async，`StreamingResponse` 天然支持 SSE |
| ASGI 服务器 | **Uvicorn** | FastAPI 标配 |
| LLM | **DeepSeek** `deepseek-chat` | 国内直连、OpenAI 兼容、支持流式、成本低 |
| LLM SDK | **openai**（官方 Python SDK） | 改 `base_url` 指向 DeepSeek 即可 |
| 向量库 | **ChromaDB**（开发）/ Milvus（生产） | 轻量、零部署、持久化简单 |
| Embedding | **BGE** `bge-small-zh-v1.5` | 本地免费、中文效果好；备选通义 `text-embedding-v3` |
| ORM | **SQLAlchemy 2.0**（async） | 与 FastAPI 异步体系契合 |
| 数据库 | SQLite（开发）/ MySQL（生产） | 与主站 MySQL 可共用 |
| Redis 客户端 | **redis-py**（async） | 复用 Java 后端 Redis 做 token 校验 |
| 配置 | **pydantic-settings** | 环境变量驱动 |

## 3. 系统架构

```
┌─────────────┐   token 头    ┌──────────────────────────────────────────┐
│  前端 Vue3   │ ────────────▶ │           Python 后端 (FastAPI)          │
│  悬浮球组件   │               │                                          │
└─────────────┘               │  ┌──────────┐   ┌─────────────────────┐  │
                              │  │ Auth 依赖 │──▶│ Redis(共享)          │  │
                              │  └──────────┘   │ galen:token:{token}  │  │
                              │        │        └─────────────────────┘  │
                              │        ▼                                 │
                              │  ┌──────────┐   ┌─────────────────────┐  │
                              │  │ 会话/消息  │──▶│ MySQL / SQLite       │  │
                              │  │  Service  │   │ conversation/message │  │
                              │  └──────────┘   └─────────────────────┘  │
                              │        │                                 │
                              │        ▼                                 │
                              │  ┌──────────┐   ┌─────────────────────┐  │
                              │  │ RAG 检索  │──▶│ ChromaDB 向量库       │  │
                              │  └──────────┘   └─────────────────────┘  │
                              │        │                                 │
                              │        ▼                                 │
                              │  ┌──────────┐   ┌─────────────────────┐  │
                              │  │ LLM 调用  │──▶│ DeepSeek API (流式)   │  │
                              │  └──────────┘   └─────────────────────┘  │
                              └──────────────────────────────────────────┘
```

## 4. API 设计（对齐前端）

### 4.1 通用响应格式（REST 接口）

前端 `assistantRequest.js` 响应拦截器按 `res.code !== 200` 判定失败并弹出 `res.message`。因此所有 REST 接口返回：

```json
{ "code": 200, "message": "Success", "data": {}, "timestamp": 1729497600 }
```

### 4.2 会话 CRUD（REST，与前端 4 个接口一一对应）

| 方法 | 路径 | 请求 | 成功响应 `data` |
|---|---|---|---|
| POST | `/conversation` | 无 | `{ id, title, createTime }` |
| GET | `/conversation/list` | 无 | `[ { id, title, updateTime, createTime } ]` |
| GET | `/conversation/{id}` | 无 | `{ id, title, messages: [ {id,role,content,createTime} ] }` |
| DELETE | `/conversation/{id}` | 无 | `null` |

### 4.3 发送消息（SSE 流式，替换原「整段返回」）

前端原 `sendMessage` 是 `POST /conversation/{id}/message`，改为 SSE 后接口保持 **同一路径**，但响应为 `text/event-stream`。

**请求：** `POST /conversation/{id}/message`，`Content-Type: application/json`，body `{ "content": "..." }`。

**SSE 事件协议：**

```
event: meta
data: {"assistant_message_id": 1002, "conversation_id": 1}

event: delta
data: {"content": "你好，"}

event: delta
data: {"content": "有什么可以帮你？"}

event: done
data: {"message": {"id": 1002, "role": "assistant", "content": "你好，有什么可以帮你？", "createTime": "..."}}

// 出错时：
event: error
data: {"code": 500, "message": "LLM 调用失败"}
```

- `meta`：首个事件，返回落库后的助手消息 ID，便于前端断流后定位。
- `delta`：增量文本，前端逐字追加。
- `done`：结束事件，携带完整消息对象（与 REST 详情里的 message 结构一致）。
- `error`：异常终止事件。

> 设计说明：SSE 无法用 `EventSource`（不支持 POST 携带 JSON body），前端需改用 `fetch` + `ReadableStream`（见 §11）。

## 5. 数据模型

```sql
-- conversation 会话表
CREATE TABLE conversation (
  id         BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id    BIGINT NOT NULL,          -- 来自共享 Redis 的用户 id
  title      VARCHAR(255) DEFAULT '新对话',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_user (user_id)
);

-- message 消息表
CREATE TABLE message (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  conversation_id BIGINT NOT NULL,
  role            VARCHAR(16) NOT NULL,   -- user / assistant
  content         TEXT NOT NULL,
  created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_conv (conversation_id),
  FOREIGN KEY (conversation_id) REFERENCES conversation(id) ON DELETE CASCADE
);
```

- 会话标题 `title`：首条用户消息截断前 20 字自动生成。
- 删除会话级联删除消息（`ON DELETE CASCADE`）。
- `user_id` 隔离：所有查询必须带 `user_id` 条件，防止越权访问他人会话。

## 6. 认证鉴权（对接 Java 后端，零改造）

前端在 `token` 请求头携带宠物平台的 `pet_token`（localStorage key `pet_token`）。该 token 由 Java 后端生成，用户信息以 **Redis Hash** 存储：

```
Key   : galen:token:{token}
Type  : Hash
Fields: id, username, phone, email, nickname, avatar, gender, status, role, ...
TTL   : 30 分钟（Java 端每次请求续期）
```

**Python 端鉴权实现（FastAPI 依赖注入）：**

```python
import redis.asyncio as redis

async def get_current_user(token: str = Header(default=None)) -> dict:
    if not token:
        raise HTTPException(401, "请先登录")
    r = redis.from_url("redis://:123456@localhost:6379", decode_responses=True)
    user = await r.hgetall(f"galen:token:{token}")
    if not user:
        raise HTTPException(401, "请先登录")
    user["id"] = int(user.get("id", 0))
    return user
```

- Redis 地址/密码复用 Java 端 `application-dev.yml`（`localhost:6379` / `123456`）。
- 校验通过后 `user["id"]` 即会话归属的 `user_id`。
- 错误码统一 `401`，前端拦截器会弹出「请先登录」。

## 7. RAG 知识库设计

### 7.1 流程

```
文档/商品数据 → 切分(chunk) → Embedding(BGE) → 写入 ChromaDB
                                                      │
用户提问 → Embedding → 相似度检索 top-k → 拼接上下文 → 送入 DeepSeek
```

### 7.2 知识来源（按优先级）

1. **宠物饲养知识**：`docs/` 下的 Markdown/TXT 语料（喂养、疾病、疫苗、训练等）。
2. **平台商品数据**：定时任务从 Java 后端商品/秒杀商品接口拉取，转文本后入库。
3. **领养/救助信息**：领养、救助记录（如有接口）。

### 7.3 关键实现

```python
from sentence_transformers import SentenceTransformer
import chromadb

model = SentenceTransformer("BAAI/bge-small-zh-v1.5")
client = chromadb.PersistentClient(path="./chroma_db")
collection = client.get_or_create_collection("pet_knowledge")

def embed(texts: list[str]) -> list[list[float]]:
    # BGE 建议查询/文档分别加前缀，提升检索质量
    return model.encode(texts, normalize_embeddings=True).tolist()

def add_doc(doc_id, text, chunk_size=500):
    chunks = [text[i:i+chunk_size] for i in range(0, len(text), chunk_size)]
    collection.add(ids=[f"{doc_id}-{i}" for i in range(len(chunks))],
                   embeddings=embed(chunks),
                   documents=chunks)

def retrieve(query, top_k=3):
    q_emb = embed([f"为这个句子生成表示以用于检索相关文章：{query}"])
    res = collection.query(query_embeddings=q_emb, n_results=top_k)
    return res["documents"][0]
```

### 7.4 检索增强 Prompt 模板

```
你是「宠物助手」，一位专业的宠物饲养顾问。请根据以下知识库内容回答用户问题，
语气亲切专业。若知识库不足以回答，明确告知并给出通用建议。

【知识库内容】
{context}

【用户问题】
{question}

【回答】
```

## 8. DeepSeek + SSE 流式实现

DeepSeek 为 OpenAI 兼容协议，直接用 `openai` SDK 改 `base_url`：

```python
from openai import AsyncOpenAI

client = AsyncOpenAI(
    api_key=settings.deepseek_api_key,
    base_url="https://api.deepseek.com",   # 兼容 OpenAI 协议
)

async def stream_chat(messages: list[dict]):
    """messages 为 [{'role': 'user'|'assistant', 'content': str}]"""
    stream = await client.chat.completions.create(
        model="deepseek-chat",             # deepseek-reasoner 为 R1 推理模型
        messages=[{"role": "system", "content": SYSTEM_PROMPT}, *messages],
        stream=True,
        temperature=0.7,
    )
    async for chunk in stream:
        delta = chunk.choices[0].delta.content
        if delta:
            yield delta
```

**FastAPI SSE 端点：**

```python
@app.post("/conversation/{cid}/message")
async def send_message(cid: int, body: MessageIn, user=Depends(get_current_user)):
    conv = await get_conv(cid, user["id"])          # 校验归属
    await save_message(cid, "user", body.content)   # 落库用户消息
    history = await load_history(cid)               # 取最近 N 轮

    # RAG 检索
    context = "\n".join(retrieve(body.content))
    messages = build_messages(history, body.content, context)

    async def gen():
        # 预留助手消息占位并拿到 id
        aid = await save_message(cid, "assistant", "")
        yield sse("meta", {"assistant_message_id": aid, "conversation_id": cid})
        full = ""
        try:
            async for delta in stream_chat(messages):
                full += delta
                yield sse("delta", {"content": delta})
            await update_message(aid, full)         # 流结束后写回完整内容
            yield sse("done", {"message": {"id": aid, "role": "assistant", "content": full}})
        except Exception as e:
            yield sse("error", {"code": 500, "message": str(e)})

    return StreamingResponse(gen(), media_type="text/event-stream",
                             headers={"Cache-Control": "no-cache",
                                      "X-Accel-Buffering": "no"})
```

> `X-Accel-Buffering: no` 关闭 Nginx 代理缓冲，否则流式会被整段缓冲。生产部署务必保留。

## 9. 项目目录结构

```
assistant-backend/
├── app/
│   ├── main.py                 # FastAPI 入口 + CORS
│   ├── config.py               # pydantic-settings 配置
│   ├── deps.py                 # get_current_user 鉴权依赖
│   ├── models.py               # SQLAlchemy 模型
│   ├── schemas.py              # Pydantic 请求/响应模型
│   ├── db.py                   # 数据库会话管理
│   ├── redis_client.py         # 共享 Redis 客户端
│   ├── routers/
│   │   └── conversation.py     # 会话 + 消息路由
│   ├── services/
│   │   ├── chat.py             # LLM 流式调用
│   │   └── rag.py              # 检索 + Prompt 组装
│   └── knowledge/
│       └── ingest.py           # 知识库构建/更新脚本
├── chroma_db/                  # ChromaDB 持久化目录
├── .env                        # 环境变量（不入库）
├── requirements.txt
└── README.md
```

## 10. 环境配置

`.env`：

```bash
DEEPSEEK_API_KEY=sk-xxxx
DEEPSEEK_BASE_URL=https://api.deepseek.com
DEEPSEEK_MODEL=deepseek-chat

REDIS_URL=redis://:123456@localhost:6379/0

DATABASE_URL=sqlite+aiosqlite:///./assistant.db   # 开发
# DATABASE_URL=mysql+aiomysql://root:1234@localhost:3306/assistant  # 生产

EMBEDDING_MODEL=BAAI/bge-small-zh-v1.5
CHROMA_DIR=./chroma_db
```

`requirements.txt`：

```
fastapi
uvicorn[standard]
openai
redis
sqlalchemy[asyncio]
aiosqlite          # 开发库；生产换 aiomysql
chromadb
sentence-transformers
pydantic-settings
```

## 11. 前端改造点（SSE）

仅需改 `sendMessage`，其余接口不动。新增基于 `fetch` 的流式读取（`EventSource` 不支持 POST）：

```js
// src/api/assistant.js 新增
export async function sendMessageStream(id, content, onDelta, onDone) {
  const token = getToken()
  const res = await fetch(`${import.meta.env.VITE_ASSISTANT_BASE_URL}/conversation/${id}/message`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', token },
    body: JSON.stringify({ content })
  })
  const reader = res.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''
  while (true) {
    const { value, done } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true })
    // 按 \n\n 切分 SSE 事件，解析 event/data 字段，
    // delta → onDelta(content)；done → onDone(message)
  }
}
```

对应 `AssistantFloatBall.vue` 的 `handleSend` 改为：先本地插入用户消息，助手消息占位，`onDelta` 逐字追加，`onDone` 落库最终消息。

## 12. 开发里程碑

| 阶段 | 内容 | 产出 |
|---|---|---|
| M1 | 项目骨架 + 会话 CRUD + Redis 鉴权 | 跑通前端 4 个 REST 接口 |
| M2 | DeepSeek 整段对话（先不做流式） | 联调「整段返回」验证全链路 |
| M3 | SSE 流式改造 + 前端 `fetch` 流式读取 | 打字机效果 |
| M4 | RAG：知识库构建脚本 + 检索接入 | 带上下文的专业回答 |
| M5 | 商品数据定时同步 + 部署（Nginx 反代、关缓冲） | 生产可用 |

## 13. 风险与注意事项

- **越权**：所有会话/消息查询强制带 `user_id`，否则不同用户可互看会话。
- **流式缓冲**：Nginx 反代必须 `proxy_buffering off`；应用层加 `X-Accel-Buffering: no`。
- **Embedding 冷启动**：`bge-small-zh-v1.5` 首次加载需下载模型权重（约 400MB），可预下载或换通义 Embedding API。
- **DeepSeek 无 Embedding API**：检索向量统一用 BGE/通义，勿用 DeepSeek 做 embedding。
- **超时**：流式接口前端 `timeout` 需放宽（当前 axios 为 30s，SSE 走 fetch 需自定义超时策略）。
