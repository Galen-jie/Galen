<template>
  <div class="profile-container">
    <!-- 用户信息卡片 -->
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="user-card">
          <div class="user-avatar">
            <el-upload
              class="avatar-uploader"
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              :http-request="handleAvatarUpload"
            >
              <img v-if="userStore.userInfo?.avatar" :src="userStore.userInfo.avatar" class="avatar" />
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
          </div>
          <div class="user-info">
            <h3>{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</h3>
            <p>{{ userStore.userInfo?.phone }}</p>
            <el-tag v-if="userStore.userInfo?.role === 1" type="success">救助站/志愿者</el-tag>
            <el-tag v-else>普通用户</el-tag>
          </div>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span>个人信息</span>
            </div>
          </template>

          <el-form :model="updateForm" :rules="rules" ref="formRef" label-width="100px">
            <el-form-item label="用户名">
              <el-input :model-value="userStore.userInfo?.username" disabled />
            </el-form-item>

            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="updateForm.nickname" placeholder="请输入昵称" />
            </el-form-item>

            <el-form-item label="邮箱" prop="email">
              <el-input v-model="updateForm.email" placeholder="请输入邮箱" />
            </el-form-item>

            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="updateForm.gender">
                <el-radio :value="0">保密</el-radio>
                <el-radio :value="1">男</el-radio>
                <el-radio :value="2">女</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleUpdate" :loading="loading">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷入口 -->
    <el-card class="shortcuts-card">
      <template #header>
        <span>我的功能</span>
      </template>
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="shortcut-item" @click="router.push('/pet/my')">
            <div class="shortcut-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
              <el-icon><Document /></el-icon>
            </div>
            <div class="shortcut-info">
              <h4>我的发布</h4>
              <p>查看我发布的宠物救助信息</p>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="shortcut-item" @click="router.push('/adoption/my')">
            <div class="shortcut-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
              <el-icon><List /></el-icon>
            </div>
            <div class="shortcut-info">
              <h4>我的申请</h4>
              <p>查看我的领养申请记录</p>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="shortcut-item" @click="router.push('/seckill/orders')">
            <div class="shortcut-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
              <el-icon><ShoppingCart /></el-icon>
            </div>
            <div class="shortcut-info">
              <h4>秒杀订单</h4>
              <p>查看我的秒杀订单</p>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="shortcut-item" @click="router.push('/cart')">
            <div class="shortcut-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);">
              <el-icon><ShoppingBag /></el-icon>
            </div>
            <div class="shortcut-info">
              <h4>购物车</h4>
              <p>查看我的购物车</p>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 救助站/志愿者专属 -->
      <el-row :gutter="20" v-if="userStore.userInfo?.role === 1" style="margin-top: 20px;">
        <el-col :span="12">
          <div class="shortcut-item" @click="router.push('/adoption/pending')">
            <div class="shortcut-icon" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);">
              <el-icon><Finished /></el-icon>
            </div>
            <div class="shortcut-info">
              <h4>待审核申请</h4>
              <p>审核用户的领养申请</p>
            </div>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="shortcut-item" @click="router.push('/pet/publish')">
            <div class="shortcut-icon" style="background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);">
              <el-icon><Plus /></el-icon>
            </div>
            <div class="shortcut-info">
              <h4>发布宠物</h4>
              <p>发布新的宠物救助信息</p>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { updateUserInfo } from '@/api/user'
import { uploadFile } from '@/api/upload'
import { ElMessage } from 'element-plus'
import { Plus, Document, List, ShoppingCart, ShoppingBag, Finished } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const updateForm = ref({
  nickname: '',
  email: '',
  gender: 0
})

const rules = {
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

onMounted(() => {
  if (userStore.userInfo) {
    updateForm.value = {
      nickname: userStore.userInfo.nickname || '',
      email: userStore.userInfo.email || '',
      gender: userStore.userInfo.gender || 0
    }
  }
})

const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }
  return true
}

const handleAvatarUpload = async (options) => {
  try {
    const res = await uploadFile(options.file, 'avatar')
    if (res.data) {
      await updateUserInfo({ avatar: res.data })
      await userStore.getUserInfo()
      ElMessage.success('头像更新成功')
    }
  } catch (error) {
    console.error('Upload avatar failed:', error)
  }
}

const handleUpdate = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await updateUserInfo(updateForm.value)
        await userStore.getUserInfo()
        ElMessage.success('更新成功')
      } catch (error) {
        console.error('Update failed:', error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.profile-container {
  max-width: var(--content-max-width);
  margin: 0 auto;
  padding: 20px;
}

.user-card {
  text-align: center;
}

.user-avatar {
  margin-bottom: 20px;
}

.avatar-uploader {
  width: 120px;
  height: 120px;
  margin: 0 auto;
  border: 1px dashed #d9d9d9;
  border-radius: 50%;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.avatar {
  width: 120px;
  height: 120px;
  display: block;
  object-fit: cover;
}

.user-info h3 {
  margin: 10px 0;
  font-size: 20px;
}

.user-info p {
  color: #666;
  margin-bottom: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-card {
  min-height: 400px;
}

/* 快捷入口样式 */
.shortcuts-card {
  margin-top: 20px;
}

.shortcut-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.shortcut-item:hover {
  background: #ecf5ff;
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.shortcut-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
  flex-shrink: 0;
}

.shortcut-info h4 {
  margin: 0 0 5px;
  font-size: 16px;
  color: #333;
}

.shortcut-info p {
  margin: 0;
  font-size: 12px;
  color: #999;
}
</style>