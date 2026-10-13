import request from '@/utils/request'

export function getCategoryTree() {
  return request({
    url: '/category/tree',
    method: 'get'
  })
}

export function getCategoryList(params) {
  return request({
    url: '/category/list',
    method: 'get',
    params
  })
}