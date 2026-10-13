import axios from 'axios'
import { getToken, removeToken } from '@/utils/auth'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000
})

request.interceptors.request.use(
  config => {
    const token = getToken()
    if (token) {
      config.headers['token'] = token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  response => {
    const res = response.data

    if (res.code !== 200) {
      // 不在登录、注册等场景显示 401 错误
      const isAuthCheck = response.config.url?.includes('/user/info')
      if (res.code === 401) {
        removeToken()
        // 如果不是用户信息检查，才跳转到登录页
        if (!isAuthCheck) {
          ElMessage.error(res.message || '请先登录')
          router.push({
            path: '/login',
            query: { redirect: router.currentRoute.value.fullPath }
          })
        }
        return Promise.reject(new Error(res.message || '未登录'))
      }

      // 其他错误正常显示
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }

    return res
  },
  error => {
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request