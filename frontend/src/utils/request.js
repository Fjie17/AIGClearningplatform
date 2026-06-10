import axios from 'axios'
import { ElMessage } from 'element-plus'

const service = axios.create({
  baseURL: '/api', 
  timeout: 30000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    
    // 只有在没有设置 Content-Type 且不是 FormData 时，才设置默认的 application/json
    if (!config.headers['Content-Type'] && !(config.data instanceof FormData)) {
      config.headers['Content-Type'] = 'application/json'
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    
    // 如果是 Blob 类型响应（文件下载），直接返回响应对象
    if (response.data instanceof Blob) {
      return response
    }
    
    const res = response.data
    
    // 如果是空对象或字符串响应
    if (typeof res === 'string') {
      return res
    }
    
    // 如果响应数据为空对象，返回成功
    if (res && Object.keys(res).length === 0) {
      return { success: true }
    }
    
    return res
  },
  error => {
    const { response } = error
    
    if (response) {
      // 尝试从响应中获取错误信息
      const errorMsg = response.data?.message || response.data || '请求失败'
      
      switch (response.status) {
        case 400:
          ElMessage.error(errorMsg || '请求参数错误')
          break
        case 401:
          localStorage.removeItem('token')
          localStorage.removeItem('userInfo')
          window.location.href = '/login'
          ElMessage.error('登录已过期，请重新登录')
          break
        case 403:
          ElMessage.error('没有权限访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器错误')
          break
        default:
          ElMessage.error(errorMsg)
      }
    } else {
      ElMessage.error('网络错误，请检查网络连接')
    }
    
    return Promise.reject(error)
  }
)

export default service