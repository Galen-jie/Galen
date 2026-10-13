<template>
  <div class="my-applications-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的领养申请</span>
          <el-radio-group v-model="statusFilter" size="small" @change="handleFilterChange">
            <el-radio-button value="">全部</el-radio-button>
            <el-radio-button value="0">待审核</el-radio-button>
            <el-radio-button value="1">已通过</el-radio-button>
            <el-radio-button value="2">已拒绝</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <div class="applications-container" v-loading="loading">
        <el-empty v-if="!loading && applications.length === 0" description="暂无申请记录">
          <el-button type="primary" @click="$router.push('/pet')">去领养宠物</el-button>
        </el-empty>

        <div class="application-list" v-else>
          <div
            v-for="app in applications"
            :key="app.id"
            class="application-item"
            @click="showDetail(app)"
          >
            <div class="app-header">
              <div class="app-no">申请编号: {{ app.applicationNo }}</div>
              <el-tag :type="getStatusType(app.status)">{{ app.statusName }}</el-tag>
            </div>

            <div class="app-body">
              <el-image
                :src="getPetImage(app)"
                fit="cover"
                class="pet-image"
              >
                <template #error>
                  <div class="image-placeholder">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>

              <div class="app-content">
                <div class="pet-info">
                  <h4 class="pet-name">{{ app.petName }}</h4>
                  <p class="apply-time">申请时间: {{ formatTime(app.createTime) }}</p>
                </div>

                <div class="applicant-info">
                  <div class="info-row">
                    <span class="label">姓名:</span>
                    <span class="value">{{ app.applicantName }}</span>
                  </div>
                  <div class="info-row">
                    <span class="label">电话:</span>
                    <span class="value">{{ maskPhone(app.applicantPhone) }}</span>
                  </div>
                  <div class="info-row">
                    <span class="label">地址:</span>
                    <span class="value">{{ app.applicantAddress }}</span>
                  </div>
                </div>

                <div class="app-reason">
                  <span class="label">领养理由:</span>
                  <p>{{ app.reason }}</p>
                </div>
              </div>
            </div>

            <!-- 拒绝理由 -->
            <div class="reject-reason" v-if="app.status === 2 && app.rejectReason">
              <el-alert type="error" :closable="false">
                <template #title>
                  <span>拒绝理由: {{ app.rejectReason }}</span>
                </template>
              </el-alert>
            </div>

            <!-- 审核信息 -->
            <div class="review-info" v-if="app.status === 1">
              <el-alert type="success" :closable="false">
                <template #title>
                  <span>审核人: {{ app.reviewerName }}</span>
                  <span style="margin-left: 20px;">审核时间: {{ formatTime(app.reviewTime) }}</span>
                </template>
              </el-alert>
            </div>

            <div class="app-footer">
              <el-button
                v-if="app.status === 0"
                type="danger"
                text
                @click.stop="handleCancel(app)"
              >
                取消申请
              </el-button>
              <el-button
                v-if="app.status === 1"
                type="primary"
                text
                @click.stop="$router.push(`/pet/${app.petId}`)"
              >
                查看宠物
              </el-button>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div class="pagination" v-if="total > pageSize">
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            @current-change="fetchApplications"
          />
        </div>
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="申请详情" width="600px">
      <el-descriptions :column="2" border v-if="currentApp">
        <el-descriptions-item label="申请编号">{{ currentApp.applicationNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentApp.status)">{{ currentApp.statusName }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="宠物名称">{{ currentApp.petName }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ currentApp.applicantName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentApp.applicantPhone }}</el-descriptions-item>
        <el-descriptions-item label="职业">{{ currentApp.applicantJob || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="地址" :span="2">{{ currentApp.applicantAddress }}</el-descriptions-item>
        <el-descriptions-item label="居住情况" :span="2">{{ currentApp.livingSituation || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="养宠经验" :span="2">{{ currentApp.experience || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="领养理由" :span="2">{{ currentApp.reason }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ formatTime(currentApp.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="审核时间">{{ currentApp.reviewTime ? formatTime(currentApp.reviewTime) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="拒绝理由" :span="2" v-if="currentApp.status === 2">
          <span style="color: #f56c6c;">{{ currentApp.rejectReason }}</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyAdoptions, cancelAdoption } from '@/api/adoption'
import { getImageUrlWithDefault } from '@/utils/image'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'

const loading = ref(false)
const applications = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const statusFilter = ref('')

const detailVisible = ref(false)
const currentApp = ref(null)

const defaultImage = 'https://via.placeholder.com/100x100?text=No+Image'

// 获取宠物图片URL
const getPetImage = (app) => {
  return getImageUrlWithDefault(app.petImage, defaultImage)
}

const getStatusType = (status) => {
  const map = {
    0: 'warning',
    1: 'success',
    2: 'danger',
    3: 'info'
  }
  return map[status] || 'info'
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const maskPhone = (phone) => {
  if (!phone) return ''
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

const showDetail = (app) => {
  currentApp.value = app
  detailVisible.value = true
}

const handleFilterChange = () => {
  currentPage.value = 1
  fetchApplications()
}

const handleCancel = async (app) => {
  try {
    await ElMessageBox.confirm(
      '确定要取消该领养申请吗？',
      '取消确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await cancelAdoption(app.id)
    ElMessage.success('申请已取消')
    fetchApplications()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Cancel failed:', error)
      ElMessage.error('取消失败')
    }
  }
}

const fetchApplications = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }

    const res = await getMyAdoptions(params)
    if (res.data) {
      applications.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('Fetch applications failed:', error)
    ElMessage.error('获取申请列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchApplications()
})
</script>

<style scoped>
.my-applications-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.applications-container {
  min-height: 300px;
}

.application-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.application-item {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.application-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: #409eff;
}

.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px dashed #ebeef5;
}

.app-no {
  font-size: 14px;
  color: #666;
}

.app-body {
  display: flex;
  gap: 20px;
}

.pet-image {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  flex-shrink: 0;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #c0c4cc;
  font-size: 30px;
}

.app-content {
  flex: 1;
}

.pet-info {
  margin-bottom: 15px;
}

.pet-name {
  font-size: 18px;
  color: #333;
  margin: 0 0 5px;
}

.apply-time {
  font-size: 12px;
  color: #999;
  margin: 0;
}

.applicant-info {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 10px;
}

.info-row {
  font-size: 14px;
}

.info-row .label {
  color: #999;
}

.info-row .value {
  color: #333;
  margin-left: 5px;
}

.app-reason {
  margin-top: 10px;
}

.app-reason .label {
  font-size: 14px;
  color: #999;
}

.app-reason p {
  font-size: 14px;
  color: #666;
  margin: 5px 0 0;
  line-height: 1.6;
}

.reject-reason,
.review-info {
  margin-top: 15px;
}

.app-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px dashed #ebeef5;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>