import assistantRequest from '@/utils/assistantRequest'
import { getToken } from '@/utils/auth'

export function createConversation() {
  return assistantRequest({
    url: '/conversation',
    method: 'post'
  })
}

export function getConversationList() {
  return assistantRequest({
    url: '/conversation/list',
    method: 'get'
  })
}

export function getConversation(id) {
  return assistantRequest({
    url: `/conversation/${id}`,
    method: 'get'
  })
}

export function deleteConversation(id) {
  return assistantRequest({
    url: `/conversation/${id}`,
    method: 'delete'
  })
}

function parseSSEBlock(block) {
  const evt = { data: [] }
  for (const line of block.split('\n')) {
    if (line.startsWith('event:')) {
      evt.event = line.slice(6).trim()
    } else if (line.startsWith('data:')) {
      evt.data.push(line.slice(5).trimStart())
    }
  }
  if (evt.data.length === 0) return null
  const raw = evt.data.join('\n')
  try {
    evt.data = JSON.parse(raw)
  } catch {
    evt.data = raw
  }
  return evt
}

export async function sendMessageStream(id, content, { onDelta, onDone, onError } = {}) {
  const token = getToken()
  const baseURL = import.meta.env.VITE_ASSISTANT_BASE_URL || ''
  const res = await fetch(`${baseURL}/conversation/${id}/message`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { token } : {})
    },
    body: JSON.stringify({ content })
  })

  if (!res.ok) {
    const err = res.status === 401 ? new Error('请先登录') : new Error(`请求失败 (${res.status})`)
    onError?.(err)
    throw err
  }

  const reader = res.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''

  const dispatch = (evt) => {
    if (!evt) return
    switch (evt.event) {
      case 'delta':
        onDelta?.(evt.data?.content ?? '')
        break
      case 'done':
        onDone?.(evt.data?.message)
        break
      case 'error':
        onError?.(evt.data)
        break
    }
  }

  while (true) {
    const { value, done } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true })
    buffer = buffer.replace(/\r\n/g, '\n')
    const blocks = buffer.split('\n\n')
    buffer = blocks.pop()
    for (const block of blocks) dispatch(parseSSEBlock(block))
  }

  if (buffer.trim()) dispatch(parseSSEBlock(buffer))
}
