/**
 * 图片URL处理工具
 */

// API基础URL
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

/**
 * 获取完整的图片URL
 * @param {string} path 图片路径
 * @returns {string} 完整的图片URL
 */
export function getImageUrl(path) {
  if (!path) {
    return ''
  }

  // 如果已经是完整的URL（http开头），直接返回
  if (path.startsWith('http://') || path.startsWith('https://')) {
    return path
  }

  // 如果是相对路径，拼接服务器地址
  if (path.startsWith('/')) {
    return API_BASE_URL + path
  }

  // 其他情况也拼接
  return API_BASE_URL + '/' + path
}

/**
 * 默认占位图
 */
export const DEFAULT_IMAGE = 'https://via.placeholder.com/300x200?text=No+Image'

/**
 * 获取图片URL（带默认图）
 * @param {string} path 图片路径
 * @param {string} defaultImg 默认图片
 * @returns {string}
 */
export function getImageUrlWithDefault(path, defaultImg = DEFAULT_IMAGE) {
  if (!path) {
    return defaultImg
  }
  return getImageUrl(path)
}