import axios from 'axios'
import { getToken } from '@/utils/auth'
import { ElMessage } from 'element-plus'

const assistantRequest = axios.create({
  baseURL: import.meta.env.VITE_ASSISTANT_BASE_URL,
  timeout: 30000
})

assistantRequest.interceptors.request.use(
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

assistantRequest.interceptors.response.use(
  response => {
    const res = response.data

    if (res.code !== 200) {
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

export default assistantRequest
