<template>
  <div class="pet-publish-container">
    <el-card class="publish-card">
      <template #header>
        <div class="card-header">
          <span>发布宠物救助信息</span>
        </div>
      </template>

      <el-form :model="petForm" :rules="petRules" ref="petFormRef" label-width="120px" class="pet-form">
        <!-- 宠物类型 -->
        <el-form-item label="宠物类型" prop="petType">
          <el-radio-group v-model="petForm.petType">
            <el-radio :label="1">狗狗</el-radio>
            <el-radio :label="2">猫咪</el-radio>
            <el-radio :label="3">其他</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 宠物名称 -->
        <el-form-item label="宠物名称" prop="petName">
          <el-input v-model="petForm.petName" placeholder="给宠物起个名字吧" maxlength="50" />
        </el-form-item>

        <!-- 品种 -->
        <el-form-item label="品种">
          <el-input v-model="petForm.petBreed" placeholder="请输入品种（如：金毛、英短等）" maxlength="100" />
        </el-form-item>

        <!-- 性别 -->
        <el-form-item label="性别">
          <el-radio-group v-model="petForm.petGender">
            <el-radio :label="0">未知</el-radio>
            <el-radio :label="1">公</el-radio>
            <el-radio :label="2">母</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 年龄 -->
        <el-form-item label="年龄">
          <el-input v-model="petForm.petAge" placeholder="如：3个月、2岁" maxlength="20" />
        </el-form-item>

        <!-- 毛色 -->
        <el-form-item label="毛色">
          <el-input v-model="petForm.petColor" placeholder="如：黄色、黑白相间" maxlength="50" />
        </el-form-item>

        <!-- 体重 -->
        <el-form-item label="体重(kg)">
          <el-input-number v-model="petForm.petWeight" :min="0" :max="500" :precision="2" :step="0.1" />
        </el-form-item>

        <!-- 健康状况 -->
        <el-form-item label="健康状况">
          <el-input v-model="petForm.healthStatus" type="textarea" :rows="2" placeholder="请描述宠物的健康状况" maxlength="500" show-word-limit />
        </el-form-item>

        <!-- 疫苗接种 -->
        <el-form-item label="疫苗接种">
          <el-radio-group v-model="petForm.vaccinationStatus">
            <el-radio :label="0">未接种</el-radio>
            <el-radio :label="1">已接种</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 绝育状态 -->
        <el-form-item label="绝育状态">
          <el-radio-group v-model="petForm.sterilizationStatus">
            <el-radio :label="0">未绝育</el-radio>
            <el-radio :label="1">已绝育</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 救助地点 -->
        <el-form-item label="救助地点">
          <el-input v-model="petForm.rescueLocation" placeholder="请输入救助地点" maxlength="255" />
        </el-form-item>

        <!-- 救助时间 -->
        <el-form-item label="救助时间">
          <el-date-picker v-model="petForm.rescueTime" type="datetime" placeholder="选择救助时间" format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>

        <!-- 详细描述 -->
        <el-form-item label="详细描述" prop="description">
          <el-input v-model="petForm.description" type="textarea" :rows="4" placeholder="请详细描述宠物的情况，包括性格特点、生活习惯等" maxlength="1000" show-word-limit />
        </el-form-item>

        <!-- 图片上传 -->
        <el-form-item label="宠物照片">
          <el-upload
            :file-list="imageFileList"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :on-success="handleImageSuccess"
            :on-error="handleUploadError"
            :on-remove="handleImageRemove"
            :before-upload="beforeImageUpload"
            list-type="picture-card"
            :limit="9"
            :on-exceed="handleExceed"
            accept="image/*"
          >
            <el-icon><Plus /></el-icon>
            <template #tip>
              <div class="upload-tip">
                最多上传9张图片，支持 JPG、PNG 格式，单张不超过 5MB
              </div>
            </template>
          </el-upload>
        </el-form-item>

        <!-- 视频上传 -->
        <el-form-item label="宠物视频">
          <el-upload
            :action="uploadUrl"
            :headers="uploadHeaders"
            :on-success="handleVideoSuccess"
            :on-error="handleUploadError"
            :before-upload="beforeVideoUpload"
            :limit="1"
            :on-exceed="handleVideoExceed"
            accept="video/*"
          >
            <el-button type="primary">
              <el-icon><VideoCamera /></el-icon> 上传视频
            </el-button>
            <template #tip>
              <div class="upload-tip">
                最多上传1个视频，支持 MP4、AVI 格式，不超过 100MB
              </div>
            </template>
          </el-upload>
          <div v-if="petForm.videoUrl" class="video-preview">
            <video :src="petForm.videoUrl" controls style="max-width: 300px; max-height: 200px" />
            <el-button type="danger" size="small" @click="removeVideo" style="margin-left: 10px">删除</el-button>
          </div>
        </el-form-item>

        <!-- 提交按钮 -->
        <el-form-item>
          <el-button type="primary" size="large" @click="submitForm" :loading="submitLoading" style="width: 200px">
            发布
          </el-button>
          <el-button size="large" @click="resetForm" style="width: 200px">
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, VideoCamera } from '@element-plus/icons-vue'
import { createPet } from '@/api/pet'
import { useUserStore } from '@/stores/user'
import { getToken } from '@/utils/auth'

const router = useRouter()
const userStore = useUserStore()

const petFormRef = ref()
const submitLoading = ref(false)
const imageFileList = ref([])

const petForm = reactive({
  petType: null,
  petName: '',
  petBreed: '',
  petGender: 0,
  petAge: '',
  petColor: '',
  petWeight: null,
  healthStatus: '',
  vaccinationStatus: 0,
  sterilizationStatus: 0,
  rescueLocation: '',
  rescueTime: null,
  description: '',
  imageUrls: [],
  videoUrl: ''
})

const petRules = {
  petType: [{ required: true, message: '请选择宠物类型', trigger: 'change' }],
  petName: [{ required: true, message: '请输入宠物名称', trigger: 'blur' }],
  description: [{ required: true, message: '请输入详细描述', trigger: 'blur' }]
}

const uploadUrl = computed(() => {
  const baseURL = import.meta.env.VITE_API_BASE_URL || ''
  return `${baseURL}/file/upload`
})

const uploadHeaders = computed(() => {
  const token = getToken()
  return {
    token: token || ''
  }
})

const beforeImageUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

const handleImageSuccess = (response, uploadFile, uploadFiles) => {
  if (response.code === 200) {
    const imageUrl = response.data
    if (imageUrl && imageUrl.trim() !== '') {
      petForm.imageUrls.push(imageUrl)
      ElMessage.success('图片上传成功')
    } else {
      console.error('响应数据为空或无效:', response)
      ElMessage.error('上传失败：未获取到图片URL')
    }
  } else {
    console.error('上传失败，响应码:', response.code, '消息:', response.message)
    ElMessage.error(response.message || '上传失败')
  }
}

const handleImageRemove = (uploadFile, uploadFiles) => {
  const index = petForm.imageUrls.findIndex(url => {
    return uploadFile.response && uploadFile.response.data === url
  })
  if (index !== -1) {
    petForm.imageUrls.splice(index, 1)
  }
}

const beforeVideoUpload = (file) => {
  const isVideo = file.type.startsWith('video/')
  const isLt100M = file.size / 1024 / 1024 < 100

  if (!isVideo) {
    ElMessage.error('只能上传视频文件!')
    return false
  }
  if (!isLt100M) {
    ElMessage.error('视频大小不能超过 100MB!')
    return false
  }
  return true
}

const handleVideoSuccess = (response) => {
  if (response.code === 200) {
    petForm.videoUrl = response.data
    ElMessage.success('视频上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handleUploadError = (error, uploadFile, uploadFiles) => {
  console.error('=== 图片上传失败 ===')
  console.error('错误:', error)
  console.error('上传文件:', uploadFile)
  console.error('所有文件:', uploadFiles)
  ElMessage.error('上传失败，请稍后重试')
}

const handleExceed = () => {
  ElMessage.warning('最多只能上传9张图片')
}

const handleVideoExceed = () => {
  ElMessage.warning('最多只能上传1个视频')
}

const removeVideo = () => {
  petForm.videoUrl = ''
}

const submitForm = async () => {
  if (!petFormRef.value) return

  await petFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const validImageUrls = petForm.imageUrls.filter(url => url && url.trim() !== '')
        const submitData = {
          ...petForm,
          imageUrls: validImageUrls
        }

        await createPet(submitData)
        ElMessage.success('发布成功')
        router.push('/pet/my')
      } catch (error) {
        console.error('发布失败:', error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const resetForm = () => {
  petFormRef.value?.resetFields()
  imageFileList.value = []
  petForm.imageUrls = []
  petForm.videoUrl = ''
}
</script>

<style scoped>
.pet-publish-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.publish-card {
  margin-top: 20px;
}

.card-header {
  font-size: 20px;
  font-weight: 600;
}

.pet-form {
  margin-top: 20px;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.video-preview {
  display: flex;
  align-items: center;
  margin-top: 10px;
}

@media (max-width: 768px) {
  .pet-publish-container {
    padding: 10px;
  }

  :deep(.el-form-item__label) {
    text-align: left;
    width: 100% !important;
    margin-bottom: 10px;
  }

  :deep(.el-form-item__content) {
    margin-left: 0 !important;
  }
}
</style>