<template>
  <div class="settings-container">
    <el-card>
      <template #header>
        <span>账号设置</span>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="修改密码" name="password">
          <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="120px" style="max-width: 500px;">
            <el-form-item label="当前密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入当前密码" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handlePasswordChange" :loading="loading">修改密码</el-button>
              <el-button @click="resetPasswordForm">重置</el-button>
            </el-form-item>
          </el-form>

          <el-alert
            title="密码要求"
            type="info"
            :closable="false"
            style="margin-top: 20px; max-width: 500px;"
          >
            <ul style="margin: 0; padding-left: 20px;">
              <li>密码长度为8-20位</li>
              <li>必须包含大写字母</li>
              <li>必须包含小写字母</li>
              <li>必须包含数字</li>
            </ul>
          </el-alert>
        </el-tab-pane>

        <el-tab-pane label="账号安全" name="security">
          <div class="security-item">
            <div class="security-info">
              <h4>手机号</h4>
              <p>{{ maskPhone(userStore.userInfo?.phone) }}</p>
            </div>
            <el-button text type="primary" disabled>修改</el-button>
          </div>
          <div class="security-item">
            <div class="security-info">
              <h4>邮箱</h4>
              <p>{{ userStore.userInfo?.email || '未绑定' }}</p>
            </div>
            <el-button text type="primary" disabled>{{ userStore.userInfo?.email ? '修改' : '绑定' }}</el-button>
          </div>
          <div class="security-item">
            <div class="security-info">
              <h4>账号状态</h4>
              <p>
                <el-tag type="success" v-if="userStore.userInfo?.status === 1">正常</el-tag>
                <el-tag type="danger" v-else>已禁用</el-tag>
              </p>
            </div>
          </div>
          <div class="security-item">
            <div class="security-info">
              <h4>用户角色</h4>
              <p>
                <el-tag type="success" v-if="userStore.userInfo?.role === 1">救助站/志愿者</el-tag>
                <el-tag v-else>普通用户</el-tag>
              </p>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="其他设置" name="other">
          <div class="other-setting">
            <h4>退出登录</h4>
            <p>退出当前账号，需要重新登录</p>
            <el-button type="danger" @click="handleLogout">退出登录</el-button>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { updatePassword, logout } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('password')
const loading = ref(false)
const passwordFormRef = ref()
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else if (value.length < 8 || value.length > 20) {
    callback(new Error('密码长度为8-20位'))
  } else if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/.test(value)) {
    callback(new Error('密码必须包含大小写字母和数字'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== passwordForm.value.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const maskPhone = (phone) => {
  if (!phone) return ''
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

const resetPasswordForm = () => {
  passwordForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
  passwordFormRef.value?.resetFields()
}

const handlePasswordChange = async () => {
  if (!passwordFormRef.value) return

  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await updatePassword({
          oldPassword: passwordForm.value.oldPassword,
          newPassword: passwordForm.value.newPassword
        })
        ElMessage.success('密码修改成功，请重新登录')
        // 清除用户信息并跳转到登录页
        userStore.$patch({
          token: '',
          userInfo: null
        })
        localStorage.removeItem('pet_token')
        router.push('/login')
      } catch (error) {
        console.error('Password change failed:', error)
        if (error.response?.data?.message) {
          ElMessage.error(error.response.data.message)
        } else {
          ElMessage.error('密码修改失败')
        }
      } finally {
        loading.value = false
      }
    }
  })
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要退出登录吗？',
      '退出确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await logout()
    userStore.$patch({
      token: '',
      userInfo: null
    })
    localStorage.removeItem('pet_token')
    router.push('/login')
    ElMessage.success('已退出登录')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Logout failed:', error)
    }
  }
}
</script>

<style scoped>
.settings-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0;
  border-bottom: 1px solid #eee;
}

.security-item:last-child {
  border-bottom: none;
}

.security-info h4 {
  margin: 0 0 5px 0;
  font-size: 16px;
}

.security-info p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.other-setting {
  padding: 20px 0;
}

.other-setting h4 {
  margin: 0 0 10px;
  font-size: 16px;
}

.other-setting p {
  margin: 0 0 15px;
  color: #666;
  font-size: 14px;
}
</style>