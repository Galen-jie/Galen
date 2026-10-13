import { defineStore } from 'pinia'
import { login as loginApi, logout as logoutApi, getUserInfo as getUserInfoApi } from '@/api/user'
import { getToken, setToken, removeToken } from '@/utils/auth'
import router from '@/router'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || '',
    userInfo: null
  }),

  actions: {
    async login(loginForm) {
      try {
        const res = await loginApi(loginForm)
        this.token = res.data
        setToken(res.data)
        // 登录成功后不立即调用 getUserInfo，让路由守卫处理
        return res
      } catch (error) {
        return Promise.reject(error)
      }
    },

    async getUserInfo() {
      try {
        const res = await getUserInfoApi()
        this.userInfo = res.data
        return res
      } catch (error) {
        return Promise.reject(error)
      }
    },

    async logout() {
      try {
        await logoutApi()
      } catch (error) {
        console.error('Logout failed:', error)
      } finally {
        this.token = ''
        this.userInfo = null
        removeToken()
        router.push('/')
      }
    }
  }
})