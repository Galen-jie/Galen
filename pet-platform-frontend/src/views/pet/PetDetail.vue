<template>
  <div class="pet-detail-container" v-loading="loading">
    <el-empty v-if="!petDetail && !loading" description="宠物信息不存在" />

    <template v-if="petDetail">
      <!-- 返回按钮 -->
      <el-button @click="$router.back()" class="back-btn">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>

      <!-- 宠物详情卡片 -->
      <el-card class="detail-card">
        <div class="detail-content">
          <!-- 左侧图片区域 -->
          <div class="image-section">
            <el-carousel :interval="4000" type="card" height="400px" v-if="petDetail.imageUrls && petDetail.imageUrls.length > 0">
              <el-carousel-item v-for="(image, index) in petDetail.imageUrls" :key="index">
                <el-image :src="image" fit="cover" class="carousel-image" :preview-src-list="petDetail.imageUrls" :initial-index="index" />
              </el-carousel-item>
            </el-carousel>
            <div v-else class="no-image">
              <el-icon><Picture /></el-icon>
              <p>暂无图片</p>
            </div>

            <!-- 视频区域 -->
            <div v-if="petDetail.videoUrl" class="video-section">
              <h4>视频</h4>
              <video :src="petDetail.videoUrl" controls class="pet-video" />
            </div>
          </div>

          <!-- 右侧信息区域 -->
          <div class="info-section">
            <h1 class="pet-title">{{ petDetail.petName || '未命名' }}</h1>

            <div class="status-badges">
              <el-tag :type="getStatusTagType(petDetail.status)" size="large">{{ petDetail.statusName }}</el-tag>
              <el-tag :type="getPetTypeTag(petDetail.petType)">{{ petDetail.petTypeName }}</el-tag>
            </div>

            <el-divider />

            <!-- 基本信息 -->
            <div class="info-grid">
              <div class="info-item" v-if="petDetail.petBreed">
                <span class="label">品种：</span>
                <span class="value">{{ petDetail.petBreed }}</span>
              </div>
              <div class="info-item" v-if="petDetail.petGenderName">
                <span class="label">性别：</span>
                <span class="value">{{ petDetail.petGenderName }}</span>
              </div>
              <div class="info-item" v-if="petDetail.petAge">
                <span class="label">年龄：</span>
                <span class="value">{{ petDetail.petAge }}</span>
              </div>
              <div class="info-item" v-if="petDetail.petColor">
                <span class="label">毛色：</span>
                <span class="value">{{ petDetail.petColor }}</span>
              </div>
              <div class="info-item" v-if="petDetail.petWeight">
                <span class="label">体重：</span>
                <span class="value">{{ petDetail.petWeight }} kg</span>
              </div>
              <div class="info-item" v-if="petDetail.healthStatus">
                <span class="label">健康状况：</span>
                <span class="value">{{ petDetail.healthStatus }}</span>
              </div>
              <div class="info-item">
                <span class="label">疫苗接种：</span>
                <span class="value">
                  <el-tag :type="petDetail.vaccinationStatus === 1 ? 'success' : 'info'" size="small">
                    {{ petDetail.vaccinationStatus === 1 ? '已接种' : '未接种' }}
                  </el-tag>
                </span>
              </div>
              <div class="info-item">
                <span class="label">绝育状态：</span>
                <span class="value">
                  <el-tag :type="petDetail.sterilizationStatus === 1 ? 'success' : 'info'" size="small">
                    {{ petDetail.sterilizationStatus === 1 ? '已绝育' : '未绝育' }}
                  </el-tag>
                </span>
              </div>
              <div class="info-item" v-if="petDetail.rescueLocation">
                <span class="label">救助地点：</span>
                <span class="value">{{ petDetail.rescueLocation }}</span>
              </div>
              <div class="info-item" v-if="petDetail.rescueTime">
                <span class="label">救助时间：</span>
                <span class="value">{{ formatDate(petDetail.rescueTime) }}</span>
              </div>
            </div>

            <el-divider />

            <!-- 详细描述 -->
            <div class="description-section">
              <h3>详细描述</h3>
              <p>{{ petDetail.description }}</p>
            </div>

            <!-- 发布者信息 -->
            <div class="publisher-info">
              <el-avatar :size="40" :src="petDetail.publisherAvatar" />
              <div class="publisher-details">
                <span class="publisher-name">{{ petDetail.publisherName }}</span>
                <span class="publish-time">{{ formatDate(petDetail.createTime) }}</span>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="action-buttons">
              <el-button :type="petDetail.isLiked ? 'danger' : 'primary'" size="large" @click="toggleLike" :loading="likeLoading">
                <el-icon><Pointer /></el-icon>
                {{ petDetail.isLiked ? '取消点赞' : '点赞' }} ({{ petDetail.likeCount || 0 }})
              </el-button>
              <el-button type="success" size="large" @click="handleAdopt" :disabled="petDetail.status !== 0">
                <el-icon><Promotion /></el-icon>
                申请领养
              </el-button>
            </div>

            <!-- 统计信息 -->
            <div class="stats">
              <span><el-icon><View /></el-icon> 浏览 {{ petDetail.viewCount || 0 }}</span>
              <span><el-icon><ChatDotRound /></el-icon> 评论 {{ petDetail.commentCount || 0 }}</span>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 评论区 -->
      <el-card class="comment-card">
        <template #header>
          <div class="card-header">
            <span>评论区 ({{ commentTotal }})</span>
          </div>
        </template>

        <!-- 发表评论 -->
        <div class="comment-input" v-if="userStore.token">
          <el-input
            v-model="commentForm.content"
            type="textarea"
            :rows="3"
            placeholder="发表你的评论..."
            maxlength="500"
            show-word-limit
          />
          <el-button type="primary" @click="submitComment" :loading="commentLoading" :disabled="!commentForm.content.trim()">
            发表评论
          </el-button>
        </div>
        <el-empty v-else description="登录后才能评论" :image-size="60">
          <el-button type="primary" @click="$router.push('/login?redirect=' + $route.fullPath)">去登录</el-button>
        </el-empty>

        <el-divider />

        <!-- 评论列表 -->
        <div class="comment-list" v-if="commentList.length > 0">
          <div class="comment-item" v-for="comment in commentList" :key="comment.id">
            <el-avatar :size="40" :src="comment.userAvatar" />
            <div class="comment-content">
              <div class="comment-header">
                <span class="username">{{ comment.userName }}</span>
                <span class="time">{{ formatTime(comment.createTime) }}</span>
              </div>
              <div class="comment-text">{{ comment.content }}</div>
              <div class="comment-actions">
                <el-button text size="small" @click="toggleCommentLike(comment)">
                  <el-icon><Pointer /></el-icon>
                  {{ comment.isLiked ? '取消' : '点赞' }} ({{ comment.likeCount || 0 }})
                </el-button>
                <el-button v-if="comment.userId === userStore.userInfo?.id" text size="small" type="danger" @click="handleDeleteComment(comment.id)">
                  删除
                </el-button>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无评论" :image-size="60" />
      </el-card>
    </template>

    <!-- 领养申请对话框 -->
    <el-dialog v-model="adoptDialogVisible" title="申请领养" width="600px">
      <el-form :model="adoptForm" :rules="adoptRules" ref="adoptFormRef" label-width="100px">
        <el-form-item label="姓名" prop="applicantName">
          <el-input v-model="adoptForm.applicantName" placeholder="请输入您的姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="applicantPhone">
          <el-input v-model="adoptForm.applicantPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="地址" prop="applicantAddress">
          <el-input v-model="adoptForm.applicantAddress" type="textarea" :rows="2" placeholder="请输入详细地址" />
        </el-form-item>
        <el-form-item label="职业">
          <el-input v-model="adoptForm.applicantJob" placeholder="请输入您的职业" />
        </el-form-item>
        <el-form-item label="居住情况">
          <el-input v-model="adoptForm.livingSituation" type="textarea" :rows="2" placeholder="请描述您的居住情况（如住房类型、面积等）" />
        </el-form-item>
        <el-form-item label="养宠经验">
          <el-input v-model="adoptForm.experience" type="textarea" :rows="2" placeholder="请描述您的养宠经验" />
        </el-form-item>
        <el-form-item label="领养理由" prop="reason">
          <el-input v-model="adoptForm.reason" type="textarea" :rows="3" placeholder="请说明您想领养这只宠物的理由" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adoptDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAdoption" :loading="adoptLoading">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Picture, Pointer, View, ChatDotRound, Promotion } from '@element-plus/icons-vue'
import { getPetDetail, likePet, unlikePet } from '@/api/pet'
import { getCommentList, createComment, deleteComment, likeComment, unlikeComment } from '@/api/comment'
import { createAdoption } from '@/api/adoption'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const petDetail = ref(null)
const likeLoading = ref(false)

const commentList = ref([])
const commentTotal = ref(0)
const commentLoading = ref(false)
const commentForm = reactive({
  content: ''
})

const adoptDialogVisible = ref(false)
const adoptFormRef = ref()
const adoptLoading = ref(false)
const adoptForm = reactive({
  applicantName: '',
  applicantPhone: '',
  applicantAddress: '',
  applicantJob: '',
  livingSituation: '',
  experience: '',
  reason: ''
})

const adoptRules = {
  applicantName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  applicantPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  applicantAddress: [{ required: true, message: '请输入地址', trigger: 'blur' }],
  reason: [{ required: true, message: '请输入领养理由', trigger: 'blur' }]
}

onMounted(() => {
  loadPetDetail()
  loadComments()
})

const loadPetDetail = async () => {
  loading.value = true
  try {
    const res = await getPetDetail(route.params.id)
    petDetail.value = res.data
  } catch (error) {
    console.error('加载宠物详情失败:', error)
    ElMessage.error('加载宠物详情失败')
  } finally {
    loading.value = false
  }
}

const loadComments = async () => {
  try {
    const res = await getCommentList(route.params.id, { pageNum: 1, pageSize: 100 })
    commentList.value = res.data.records || []
    commentTotal.value = res.data.total || 0
  } catch (error) {
    console.error('加载评论失败:', error)
  }
}

const toggleLike = async () => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push('/login?redirect=' + route.fullPath)
    return
  }

  likeLoading.value = true
  try {
    if (petDetail.value.isLiked) {
      await unlikePet(petDetail.value.id)
      petDetail.value.isLiked = false
      petDetail.value.likeCount--
    } else {
      await likePet(petDetail.value.id)
      petDetail.value.isLiked = true
      petDetail.value.likeCount++
    }
  } catch (error) {
    console.error('操作失败:', error)
  } finally {
    likeLoading.value = false
  }
}

const submitComment = async () => {
  if (!commentForm.content.trim()) return

  commentLoading.value = true
  try {
    await createComment({
      petId: route.params.id,
      content: commentForm.content
    })
    ElMessage.success('评论成功')
    commentForm.content = ''
    loadComments()
  } catch (error) {
    console.error('评论失败:', error)
  } finally {
    commentLoading.value = false
  }
}

const toggleCommentLike = async (comment) => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    return
  }

  try {
    if (comment.isLiked) {
      await unlikeComment(comment.id)
      comment.isLiked = false
      comment.likeCount--
    } else {
      await likeComment(comment.id)
      comment.isLiked = true
      comment.likeCount++
    }
  } catch (error) {
    console.error('操作失败:', error)
  }
}

const handleDeleteComment = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      type: 'warning'
    })
    await deleteComment(id)
    ElMessage.success('删除成功')
    loadComments()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

const handleAdopt = () => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push('/login?redirect=' + route.fullPath)
    return
  }

  // 自动填充用户信息
  if (userStore.userInfo) {
    adoptForm.applicantName = userStore.userInfo.nickname || userStore.userInfo.username
    adoptForm.applicantPhone = userStore.userInfo.phone || ''
  }

  adoptDialogVisible.value = true
}

const submitAdoption = async () => {
  if (!adoptFormRef.value) return

  await adoptFormRef.value.validate(async (valid) => {
    if (valid) {
      adoptLoading.value = true
      try {
        await createAdoption({
          petId: route.params.id,
          ...adoptForm
        })
        ElMessage.success('申请已提交，请等待审核')
        adoptDialogVisible.value = false
        // 刷新宠物详情
        loadPetDetail()
      } catch (error) {
        console.error('提交申请失败:', error)
      } finally {
        adoptLoading.value = false
      }
    }
  })
}

const getStatusTagType = (status) => {
  const map = {
    0: 'success',
    1: 'warning',
    2: 'info',
    3: 'danger'
  }
  return map[status] || 'info'
}

const getPetTypeTag = (type) => {
  const map = {
    1: 'warning',
    2: 'success',
    3: 'info'
  }
  return map[type] || 'info'
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days === 0) {
    const hours = Math.floor(diff / (1000 * 60 * 60))
    if (hours === 0) {
      const minutes = Math.floor(diff / (1000 * 60))
      return minutes <= 1 ? '刚刚' : `${minutes}分钟前`
    }
    return `${hours}小时前`
  } else if (days < 7) {
    return `${days}天前`
  } else {
    return formatDate(time)
  }
}
</script>

<style scoped>
.pet-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.back-btn {
  margin-bottom: 20px;
}

.detail-card {
  margin-bottom: 20px;
}

.detail-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
}

.image-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.carousel-image {
  width: 100%;
  height: 100%;
  cursor: pointer;
}

.no-image {
  width: 100%;
  height: 400px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  color: #909399;
  font-size: 48px;
}

.video-section h4 {
  margin-bottom: 10px;
  color: #303133;
}

.pet-video {
  width: 100%;
  border-radius: 8px;
}

.info-section {
  display: flex;
  flex-direction: column;
}

.pet-title {
  font-size: 28px;
  color: #303133;
  margin: 0 0 15px;
}

.status-badges {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.info-item {
  display: flex;
  align-items: center;
}

.info-item .label {
  color: #909399;
  min-width: 80px;
}

.info-item .value {
  color: #303133;
  font-weight: 500;
}

.description-section {
  margin-top: 20px;
}

.description-section h3 {
  margin: 0 0 10px;
  color: #303133;
}

.description-section p {
  color: #606266;
  line-height: 1.8;
  margin: 0;
  white-space: pre-wrap;
}

.publisher-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.publisher-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.publisher-name {
  font-weight: 600;
  color: #303133;
}

.publish-time {
  font-size: 12px;
  color: #909399;
}

.action-buttons {
  display: flex;
  gap: 15px;
  margin-top: 20px;
}

.action-buttons .el-button {
  flex: 1;
}

.stats {
  display: flex;
  gap: 20px;
  margin-top: 15px;
  color: #909399;
  font-size: 14px;
}

.stats span {
  display: flex;
  align-items: center;
  gap: 5px;
}

.comment-card {
  margin-top: 20px;
}

.card-header {
  font-size: 18px;
  font-weight: 600;
}

.comment-input {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comment-item {
  display: flex;
  gap: 12px;
}

.comment-content {
  flex: 1;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.comment-header .username {
  font-weight: 600;
  color: #303133;
}

.comment-header .time {
  font-size: 12px;
  color: #909399;
}

.comment-text {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 8px;
}

.comment-actions {
  display: flex;
  gap: 10px;
}

@media (max-width: 768px) {
  .detail-content {
    grid-template-columns: 1fr;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .action-buttons {
    flex-direction: column;
  }
}
</style>