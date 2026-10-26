<template>
  <button class="float-ball" :class="{ open }" @click="toggleOpen">
    <el-icon v-if="!open"><ChatDotRound /></el-icon>
    <el-icon v-else><Close /></el-icon>
  </button>

  <aside class="assistant-panel" :class="{ open }">
    <div class="panel-inner">
      <!-- 未登录 -->
      <div v-if="!isLoggedIn" class="panel-body">
        <div class="panel-header">
          <span class="panel-title">🐾 宠物助手</span>
          <el-icon class="close-btn" @click="toggleOpen"><Close /></el-icon>
        </div>
        <div class="panel-placeholder">
          <el-empty description="请先登录后使用宠物助手" :image-size="80" />
        </div>
      </div>

      <!-- 已登录 -->
      <template v-else>
        <!-- 列表视图 -->
        <div v-if="!currentConversation" class="panel-body">
          <div class="panel-header">
            <span class="panel-title">🐾 宠物助手</span>
            <div class="header-actions">
              <el-button type="primary" size="small" @click="handleCreate">
                <el-icon><Plus /></el-icon> 新增对话
              </el-button>
              <el-icon class="close-btn" @click="toggleOpen"><Close /></el-icon>
            </div>
          </div>

          <div class="conversation-list" v-loading="loading">
            <el-empty v-if="conversations.length === 0 && !loading" description="暂无对话，点击上方新增" :image-size="60" />
            <div class="conversation-items">
              <div
                v-for="conv in conversations"
                :key="conv.id"
                class="conversation-item"
                @click="handleSelect(conv)"
              >
                <div class="conversation-info">
                  <div class="conversation-title">{{ conv.title || '新对话' }}</div>
                  <div class="conversation-time">{{ formatTime(conv.updateTime || conv.createTime) }}</div>
                </div>
                <el-icon class="delete-btn" @click.stop="handleDelete(conv)"><Delete /></el-icon>
              </div>
            </div>
          </div>
        </div>

        <!-- 详情视图 -->
        <div v-else class="panel-body">
          <div class="panel-header">
            <el-icon class="back-btn" @click="currentConversation = null"><ArrowLeft /></el-icon>
            <span class="panel-title ellipsis">{{ currentConversation.title || '新对话' }}</span>
            <div class="header-actions">
              <el-icon class="close-btn" @click="handleDelete(currentConversation)"><Delete /></el-icon>
              <el-icon class="close-btn" @click="toggleOpen"><Close /></el-icon>
            </div>
          </div>

          <div ref="messageArea" class="message-area">
            <el-empty v-if="messages.length === 0" description="暂无消息" :image-size="60" />
            <div v-else class="message-list">
              <div
                v-for="msg in messages"
                :key="msg.id"
                class="message-row"
                :class="msg.role === 'user' ? 'user' : 'assistant'"
              >
                <div class="message-bubble">{{ msg.content }}</div>
              </div>
            </div>
          </div>

        </div>

        <div class="input-bar">
          <el-input
            v-model="inputText"
            type="textarea"
            :rows="2"
            resize="none"
            placeholder="输入消息，回车发送..."
            @keydown.enter.exact.prevent="handleSend"
          />
          <el-button type="primary" :loading="sending" @click="handleSend">发送</el-button>
        </div>
      </template>
    </div>
  </aside>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, ArrowLeft, ChatDotRound, Close } from '@element-plus/icons-vue'
import { createConversation, getConversationList, getConversation, deleteConversation, sendMessageStream } from '@/api/assistant'

const userStore = useUserStore()

const isLoggedIn = computed(() => !!userStore.token)

const open = ref(false)
const loading = ref(false)
const conversations = ref([])
const currentConversation = ref(null)
const inputText = ref('')
const sending = ref(false)
const messageArea = ref(null)

const messages = computed(() => currentConversation.value?.messages || [])

const scrollToBottom = () => {
  nextTick(() => {
    const el = messageArea.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

const toggleOpen = () => {
  open.value = !open.value
}

const loadConversations = async () => {
  loading.value = true
  try {
    const res = await getConversationList()
    conversations.value = res.data || []
  } catch (error) {
    console.error('加载会话列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleCreate = async () => {
  try {
    const res = await createConversation()
    await loadConversations()
    const conv = res.data
    if (conv?.id) {
      await handleSelect(conv)
    }
  } catch (error) {
    console.error('新增对话失败:', error)
  }
}

const handleSelect = async (conv) => {
  try {
    const res = await getConversation(conv.id)
    currentConversation.value = res.data
    scrollToBottom()
  } catch (error) {
    console.error('查询会话失败:', error)
  }
}

const handleDelete = async (conv) => {
  try {
    await ElMessageBox.confirm('确定要删除该对话吗？删除后无法恢复', '警告', {
      type: 'warning',
      confirmButtonText: '确定删除',
      confirmButtonClass: 'el-button--danger'
    })
    await deleteConversation(conv.id)
    ElMessage.success('删除成功')
    if (currentConversation.value?.id === conv.id) {
      currentConversation.value = null
    }
    await loadConversations()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

const handleSend = async () => {
  const content = inputText.value.trim()
  if (!content) return

  if (!currentConversation.value) {
    try {
      const res = await createConversation()
      const conv = res.data
      if (!conv?.id) {
        ElMessage.error('创建对话失败')
        return
      }
      currentConversation.value = conv
      await loadConversations()
    } catch (error) {
      console.error('创建对话失败:', error)
      return
    }
  }

  const conv = currentConversation.value
  if (!Array.isArray(conv.messages)) conv.messages = []
  conv.messages.push({
    id: `local-${Date.now()}`,
    role: 'user',
    content,
    createTime: new Date().toISOString()
  })
  conv.messages.push({
    id: `local-${Date.now()}-pending`,
    role: 'assistant',
    content: '',
    createTime: new Date().toISOString()
  })
  const assistantMsg = conv.messages[conv.messages.length - 1]

  inputText.value = ''
  sending.value = true
  try {
    await sendMessageStream(conv.id, content, {
      onDelta: (delta) => {
        assistantMsg.content += delta
        scrollToBottom()
      },
      onDone: (message) => {
        if (message) {
          assistantMsg.id = message.id
          assistantMsg.content = message.content
          assistantMsg.createTime = message.createTime
        }
      },
      onError: (err) => {
        const msg = typeof err === 'string' ? err : err?.message || '回复失败，请重试'
        assistantMsg.content = assistantMsg.content
          ? `${assistantMsg.content}\n[回复中断：${msg}]`
          : `回复失败：${msg}`
        ElMessage.error(msg)
      }
    })
  } catch (error) {
    console.error('发送消息失败:', error)
    if (!assistantMsg.content) {
      assistantMsg.content = '回复失败，请重试'
    }
  } finally {
    sending.value = false
  }
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN', { hour12: false })
}

watch(isLoggedIn, (val) => {
  if (val) {
    loadConversations()
  } else {
    conversations.value = []
    currentConversation.value = null
  }
})

onMounted(() => {
  if (isLoggedIn.value) {
    loadConversations()
  }
})
</script>

<style scoped>
.float-ball {
  position: fixed;
  right: 24px;
  bottom: 80px;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  border: none;
  background: #409eff;
  color: #fff;
  font-size: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 2000;
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.4);
  transition: transform 0.2s, box-shadow 0.2s, background-color 0.2s;
}

.float-ball:hover {
  transform: scale(1.08);
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.55);
}

.float-ball.open {
  background: #909399;
}

.assistant-panel {
  width: 0;
  flex-shrink: 0;
  align-self: flex-start;
  position: sticky;
  top: 60px;
  height: min(600px, calc(100vh - 160px));
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  background: #fff;
}

.assistant-panel.open {
  width: min(380px, 85vw);
}

.panel-inner {
  width: min(380px, 85vw);
  height: 100%;
  display: flex;
  flex-direction: column;
  border-left: 1px solid #f0f0f0;
  box-shadow: -4px 0 12px rgba(0, 0, 0, 0.04);
  box-sizing: border-box;
}

.panel-body {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 14px 16px;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.panel-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.panel-title.ellipsis {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.close-btn,
.back-btn {
  cursor: pointer;
  color: #909399;
  font-size: 18px;
  transition: color 0.3s;
  flex-shrink: 0;
}

.close-btn:hover,
.back-btn:hover {
  color: #409eff;
}

.panel-placeholder {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.conversation-list,
.message-area {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
}

.input-bar {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.input-bar :deep(.el-textarea) {
  flex: 1;
}

.input-bar .el-button {
  flex-shrink: 0;
}

.conversation-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  cursor: pointer;
  border-bottom: 1px solid #fafafa;
  transition: background-color 0.2s;
}

.conversation-item:hover {
  background-color: #f5f7fa;
}

.conversation-info {
  flex: 1;
  min-width: 0;
}

.conversation-title {
  font-size: 14px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conversation-time {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.delete-btn {
  color: #c0c4cc;
  font-size: 16px;
  opacity: 0;
  transition: all 0.2s;
  flex-shrink: 0;
}

.conversation-item:hover .delete-btn {
  opacity: 1;
  color: #f56c6c;
}

.delete-btn:hover {
  color: #f56c6c;
}

.message-list {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-row {
  display: flex;
}

.message-row.user {
  justify-content: flex-end;
}

.message-row.assistant {
  justify-content: flex-start;
}

.message-bubble {
  max-width: 80%;
  padding: 10px 14px;
  border-radius: 10px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
  white-space: pre-wrap;
}

.message-row.user .message-bubble {
  background: #409eff;
  color: #fff;
  border-top-right-radius: 2px;
}

.message-row.assistant .message-bubble {
  background: #f4f4f5;
  color: #303133;
  border-top-left-radius: 2px;
}
</style>
