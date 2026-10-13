import { createRouter, createWebHistory } from 'vue-router'
import { routes } from './routes'
import { getToken } from '@/utils/auth'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 不需要登录就能访问的白名单页面
const whiteList = ['/login', '/register']

router.beforeEach(async (to, from) => {
  document.title = to.meta.title || '宠物救助平台'

  const token = getToken()
  const userStore = useUserStore()

  console.log('=== 路由守卫 ===')
  console.log('目标路径:', to.path)
  console.log('Token:', token ? '存在' : '不存在')
  console.log('UserInfo:', userStore.userInfo ? '已加载' : '未加载')

  // 已登录
  if (token) {
    // 如果已登录，访问登录/注册页面，跳转到首页
    if (whiteList.includes(to.path)) {
      console.log('已登录，拦截登录页，跳转首页')
      return '/'
    }

    // 如果用户信息未加载，尝试加载（不管访问什么页面）
    if (!userStore.userInfo) {
      console.log('Token存在但用户信息未加载，尝试获取用户信息...')
      try {
        await userStore.getUserInfo()
        console.log('用户信息加载成功:', userStore.userInfo)
      } catch (error) {
        console.error('用户信息加载失败:', error)
        // 加载失败，清除token
        userStore.token = ''
        userStore.userInfo = null
        // 如果访问需要登录的页面，跳转到登录页
        if (to.meta.requireAuth) {
          console.log('需要登录，跳转到登录页')
          return {
            path: '/login',
            query: { redirect: to.fullPath }
          }
        }
        // 否则继续访问（公开页面）
        return
      }
    }

    // 需要登录权限的页面
    if (to.meta.requireAuth) {
      // 检查角色权限
      if (to.meta.requireRole && userStore.userInfo) {
        const userRole = userStore.userInfo.role
        if (userRole !== to.meta.requireRole) {
          console.log('权限不足，跳转首页')
          return '/'
        }
      }
    }
  } else {
    // 未登录
    // 需要登录权限的页面，跳转到登录页
    if (to.meta.requireAuth) {
      console.log('未登录且需要登录，跳转到登录页')
      return {
        path: '/login',
        query: { redirect: to.fullPath }
      }
    }
  }

  console.log('路由守卫通过')
})

export default router