<template>
  <div class="pending-reviews-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>待审核申请</span>
          <el-button type="primary" @click="fetchApplications">
            <el-icon><Refresh /></el-icon> 刷新
          </el-button>
        </div>
      </template>

      <div class="reviews-container" v-loading="loading">
        <el-empty v-if="!loading && applications.length === 0" description="暂无待审核申请" />

        <div class="application-list" v-else>
          <div
            v-for="app in applications"
            :key="app.id"
            class="application-item"
          >
            <div class="app-header">
              <div class="app-info">
                <span class="app-no">申请编号: {{ app.applicationNo }}</span>
                <span class="app-time">申请时间: {{ formatTime(app.createTime) }}</span>
              </div>
              <el-tag type="warning">待审核</el-tag>
            </div>

            <div class="app-body">
              <div class="pet-section">
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
                <div class="pet-info">
                  <h4>{{ app.petName }}</h4>
                  <el-button type="primary" text size="small" @click="$router.push(`/pet/${app.petId}`)">
                    查看宠物详情
                  </el-button>
                </div>
              </div>

              <el-divider direction="vertical" />

              <div class="applicant-section">
                <h4>申请人信息</h4>
                <el-descriptions :column="2" size="small">
                  <el-descriptions-item label="姓名">{{ app.applicantName }}</el-descriptions-item>
                  <el-descriptions-item label="电话">{{ app.applicantPhone }}</el-descriptions-item>
                  <el-descriptions-item label="职业">{{ app.applicantJob || '未填写' }}</el-descriptions-item>
                  <el-descriptions-item label="地址">{{ app.applicantAddress }}</el-descriptions-item>
                </el-descriptions>

                <div class="extra-info">
                  <div class="info-item" v-if="app.livingSituation">
                    <span class="label">居住情况:</span>
                    <span class="value">{{ app.livingSituation }}</span>
                  </div>
                  <div class="info-item" v-if="app.experience">
                    <span class="label">养宠经验:</span>
                    <span class="value">{{ app.experience }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">领养理由:</span>
                    <span class="value">{{ app.reason }}</span>
                  </div>
                </div>
              </div>
            </div>

            <div class="app-actions">
              <el-button type="success" @click="handleApprove(app)">
                <el-icon><Check /></el-icon> 通过
              </el-button>
              <el-button type="danger" @click="showRejectDialog(app)">
                <el-icon><Close /></el-icon> 拒绝
              </el-button>
              <el-button @click="showDetailDialog(app)">
                <el-icon><View /></el-icon> 查看详情
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
            layout="prev, pager, next, total"
            @current-change="fetchApplications"
          />
        </div>
      </div>
    </el-card>

    <!-- 拒绝对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝理由" width="500px">
      <el-form :model="rejectForm" :rules="rejectRules" ref="rejectFormRef" label-width="100px">
        <el-form-item label="拒绝理由" prop="rejectReason">
          <el-input
            v-model="rejectForm.rejectReason"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝理由"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleReject" :loading="rejectLoading">确认拒绝</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="申请详情" width="700px" v-if="currentApp">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="申请编号">{{ currentApp.applicationNo }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ formatTime(currentApp.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="宠物名称">{{ currentApp.petName }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ currentApp.applicantName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentApp.applicantPhone }}</el-descriptions-item>
        <el-descriptions-item label="职业">{{ currentApp.applicantJob || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="地址" :span="2">{{ currentApp.applicantAddress }}</el-descriptions-item>
        <el-descriptions-item label="居住情况" :span="2">{{ currentApp.livingSituation || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="养宠经验" :span="2">{{ currentApp.experience || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="领养理由" :span="2">{{ currentApp.reason }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getPendingAdoptions, reviewAdoption } from '@/api/adoption'
import { getImageUrlWithDefault } from '@/utils/image'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture, Refresh, Check, Close, View } from '@element-plus/icons-vue'

const loading = ref(false)
const applications = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 获取宠物图片URL
const getPetImage = (app) => {
  return getImageUrlWithDefault(app.petImage)
}

const rejectDialogVisible = ref(false)
const rejectLoading = ref(false)
const rejectFormRef = ref()
const rejectForm = reactive({
  applicationId: null,
  status: 2,
  rejectReason: ''
})

const rejectRules = {
  rejectReason: [
    { required: true, message: '请输入拒绝理由', trigger: 'blur' }
  ]
}

const detailVisible = ref(false)
const currentApp = ref(null)

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

const showRejectDialog = (app) => {
  rejectForm.applicationId = app.id
  rejectForm.rejectReason = ''
  rejectDialogVisible.value = true
}

const showDetailDialog = (app) => {
  currentApp.value = app
  detailVisible.value = true
}

const handleApprove = async (app) => {
  try {
    await ElMessageBox.confirm(
      `确定通过 ${app.applicantName} 的领养申请吗？`,
      '审核确认',
      {
        confirmButtonText: '通过',
        cancelButtonText: '取消',
        type: 'success'
      }
    )

    await reviewAdoption({
      applicationId: app.id,
      status: 1
    })

    ElMessage.success('已通过申请')
    fetchApplications()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Approve failed:', error)
      ElMessage.error('操作失败')
    }
  }
}

const handleReject = async () => {
  if (!rejectFormRef.value) return

  await rejectFormRef.value.validate(async (valid) => {
    if (valid) {
      rejectLoading.value = true
      try {
        await reviewAdoption({
          applicationId: rejectForm.applicationId,
          status: 2,
          rejectReason: rejectForm.rejectReason
        })

        ElMessage.success('已拒绝申请')
        rejectDialogVisible.value = false
        fetchApplications()
      } catch (error) {
        console.error('Reject failed:', error)
        ElMessage.error('操作失败')
      } finally {
        rejectLoading.value = false
      }
    }
  })
}

const fetchApplications = async () => {
  loading.value = true
  try {
    const res = await getPendingAdoptions({
      pageNum: currentPage.value,
      pageSize: pageSize.value
    })

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
.pending-reviews-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.reviews-container {
  min-height: 300px;
}

.application-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.application-item {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 20px;
  background: #fff;
}

.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.app-info {
  display: flex;
  gap: 20px;
}

.app-no {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.app-time {
  font-size: 12px;
  color: #999;
}

.app-body {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.pet-section {
  display: flex;
  gap: 15px;
  flex-shrink: 0;
}

.pet-image {
  width: 100px;
  height: 100px;
  border-radius: 8px;
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

.pet-info h4 {
  margin: 0 0 10px;
  font-size: 16px;
  color: #333;
}

.applicant-section {
  flex: 1;
}

.applicant-section h4 {
  margin: 0 0 15px;
  font-size: 16px;
  color: #333;
}

.extra-info {
  margin-top: 15px;
}

.info-item {
  margin-bottom: 10px;
}

.info-item .label {
  font-size: 14px;
  color: #999;
}

.info-item .value {
  font-size: 14px;
  color: #333;
  margin-left: 10px;
}

.app-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>