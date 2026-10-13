<template>
  <div class="header">
    <div class="header-container">
      <div class="logo" @click="router.push('/')">
        <span class="logo-text">🐾 宠物救助平台</span>
      </div>

      <div class="nav-menu">
        <router-link to="/" class="nav-item">首页</router-link>
        <router-link to="/pet" class="nav-item">宠物救助</router-link>
        <router-link to="/goods" class="nav-item">宠物商城</router-link>
        <router-link to="/seckill" class="nav-item">秒杀专区</router-link>
      </div>

      <div class="user-area">
        <template v-if="userStore.userInfo">
          <el-dropdown @command="handleCommand" trigger="click">
            <span class="user-info">
              <el-avatar :size="32" :src="userStore.userInfo.avatar">
                {{ userStore.userInfo.nickname?.charAt(0) || userStore.userInfo.username?.charAt(0) }}
              </el-avatar>
              <span class="username">{{ userStore.userInfo.nickname || userStore.userInfo.username }}</span>
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item command="myPets">
                  <el-icon><Document /></el-icon>我的发布
                </el-dropdown-item>
                <el-dropdown-item command="myApplications">
                  <el-icon><List /></el-icon>我的申请
                </el-dropdown-item>
                <el-dropdown-item command="seckillOrders">
                  <el-icon><ShoppingCart /></el-icon>秒杀订单
                </el-dropdown-item>
                <el-dropdown-item command="cart">
                  <el-icon><ShoppingBag /></el-icon>购物车
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon>账号设置
                </el-dropdown-item>
                <el-dropdown-item v-if="userStore.userInfo?.role === 1" command="pendingReviews">
                  <el-icon><Finished /></el-icon>待审核申请
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button type="primary" @click="handleLogin">登录</el-button>
          <el-button @click="handleRegister">注册</el-button>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ArrowDown, User, Document, List, ShoppingCart, ShoppingBag, Setting, SwitchButton, Finished } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const handleLogin = () => {
  router.push('/login')
}

const handleRegister = () => {
  router.push('/register')
}

const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/user/profile')
      break
    case 'myPets':
      router.push('/pet/my')
      break
    case 'myApplications':
      router.push('/adoption/my')
      break
    case 'seckillOrders':
      router.push('/seckill/orders')
      break
    case 'cart':
      router.push('/cart')
      break
    case 'settings':
      router.push('/user/settings')
      break
    case 'pendingReviews':
      router.push('/adoption/pending')
      break
    case 'logout':
      userStore.logout()
      break
  }
}
</script>

<style scoped>
.header {
  width: 100%;
  height: 60px;
}

.header-container {
  max-width: 1200px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.logo {
  cursor: pointer;
  display: flex;
  align-items: center;
}

.logo-text {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}

.nav-menu {
  display: flex;
  gap: 30px;
}

.nav-item {
  color: #333;
  text-decoration: none;
  font-size: 16px;
  transition: color 0.3s;
}

.nav-item:hover,
.nav-item.router-link-active {
  color: #409eff;
}

.user-area {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 8px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.username {
  color: #333;
}

.el-icon--right {
  color: #909399;
  transition: transform 0.3s;
}

:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>