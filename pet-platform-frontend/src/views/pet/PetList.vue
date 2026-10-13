<template>
  <div class="pet-list-container">
    <!-- 搜索筛选区域 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="宠物类型">
          <el-select v-model="searchForm.petType" placeholder="全部" clearable @change="handleSearch">
            <el-option label="狗狗" :value="1" />
            <el-option label="猫咪" :value="2" />
            <el-option label="其他" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable @change="handleSearch">
            <el-option label="待领养" :value="0" />
            <el-option label="已预约" :value="1" />
            <el-option label="已领养" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="搜索宠物名称、品种" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="$router.push('/pet/publish')">
            <el-icon><Plus /></el-icon> 发布宠物
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 宠物列表 -->
    <div class="pet-list" v-loading="loading">
      <el-empty v-if="petList.length === 0 && !loading" description="暂无宠物信息">
        <el-button type="primary" @click="$router.push('/pet/publish')">
          发布宠物
        </el-button>
      </el-empty>

      <el-card v-for="pet in petList" :key="pet.id" class="pet-card" @click="goToDetail(pet.id)">
        <!-- 用户信息 -->
        <div class="user-info">
          <el-avatar :size="40" :src="pet.publisherAvatar" />
          <div class="user-details">
            <span class="username">{{ pet.publisherName }}</span>
            <span class="publish-time">{{ formatTime(pet.createTime) }}</span>
          </div>
          <el-tag :type="getStatusTagType(pet.status)" size="small" class="status-tag">{{ pet.statusName }}</el-tag>
        </div>

        <!-- 宠物图片 -->
        <div class="pet-image-wrapper">
          <el-image
            :src="pet.imageUrls && pet.imageUrls.length > 0 ? pet.imageUrls[0] : defaultImage"
            fit="cover"
            class="pet-image"
            lazy
          >
            <template #error>
              <div class="image-placeholder">
                <el-icon><Picture /></el-icon>
              </div>
            </template>
          </el-image>
        </div>

        <!-- 宠物信息 -->
        <div class="pet-info">
          <h3 class="pet-name">{{ pet.petName || '未命名' }}</h3>
          <div class="pet-tags">
            <el-tag :type="getPetTypeTag(pet.petType)" size="small">{{ pet.petTypeName }}</el-tag>
            <el-tag v-if="pet.petBreed" size="small" effect="plain">{{ pet.petBreed }}</el-tag>
          </div>
          <div class="pet-description">{{ pet.description }}</div>

          <!-- 操作栏 -->
          <div class="pet-footer">
            <div class="pet-stats">
              <span><el-icon><View /></el-icon> {{ pet.viewCount || 0 }}</span>
              <span><el-icon><Pointer /></el-icon> {{ pet.likeCount || 0 }}</span>
              <span><el-icon><ChatDotRound /></el-icon> {{ pet.commentCount || 0 }}</span>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 分页 -->
    <el-pagination
      v-if="total > 0"
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[12, 24, 36, 48]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="handleSearch"
      @current-change="handleSearch"
      class="pagination"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPetList } from '@/api/pet'
import { useUserStore } from '@/stores/user'
import { Picture, View, Pointer, Plus, ChatDotRound } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const petList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(12)

const searchForm = ref({
  petType: null,
  status: null,
  keyword: ''
})

const defaultImage = 'https://via.placeholder.com/300x200?text=No+Image'

onMounted(() => {
  loadPetList()
})

const loadPetList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      ...searchForm.value
    }
    const res = await getPetList(params)
    petList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('加载宠物列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
  loadPetList()
}

const handleReset = () => {
  searchForm.value = {
    petType: null,
    status: null,
    keyword: ''
  }
  handleSearch()
}

const goToDetail = (id) => {
  router.push(`/pet/${id}`)
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
  const tagMap = {
    1: 'warning',
    2: 'success',
    3: 'info'
  }
  return tagMap[type] || 'info'
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
  } else if (days < 30) {
    return `${Math.floor(days / 7)}周前`
  } else {
    return `${Math.floor(days / 30)}个月前`
  }
}
</script>

<style scoped>
.pet-list-container {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.filter-card {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.pet-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
  min-height: 400px;
}

.pet-card {
  cursor: pointer;
  transition: all 0.3s;
  border-radius: 8px;
  overflow: hidden;
}

.pet-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

/* 用户信息区域 */
.user-info {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  border-bottom: 1px solid #f0f0f0;
}

.user-details {
  flex: 1;
  margin-left: 10px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.username {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.publish-time {
  font-size: 11px;
  color: #909399;
}

.status-tag {
  margin-left: auto;
}

/* 宠物图片 */
.pet-image-wrapper {
  width: 100%;
  max-height: 280px;
  overflow: hidden;
  background-color: #f5f7fa;
}

.pet-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-placeholder {
  width: 100%;
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  color: #909399;
  font-size: 48px;
}

/* 宠物信息 */
.pet-info {
  padding: 10px 12px;
}

.pet-name {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.pet-tags {
  display: flex;
  gap: 6px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.pet-description {
  color: #606266;
  line-height: 1.5;
  font-size: 13px;
  margin-bottom: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* 操作栏 */
.pet-footer {
  border-top: 1px solid #f0f0f0;
  padding-top: 8px;
}

.pet-stats {
  display: flex;
  gap: 16px;
  color: #909399;
  font-size: 12px;
}

.pet-stats span {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  transition: color 0.2s;
}

.pet-stats span:hover {
  color: #409eff;
}

/* 分页 */
.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

/* 响应式 */
@media (max-width: 900px) {
  .pet-list {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .pet-list-container {
    padding: 10px;
  }

  .search-form {
    flex-direction: column;
  }

  .pet-image-wrapper {
    max-height: 250px;
  }
}
</style>