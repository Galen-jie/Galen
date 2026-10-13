import request from '@/utils/request'

export function createComment(data) {
  return request({
    url: '/pet/comment',
    method: 'post',
    data
  })
}

export function deleteComment(id) {
  return request({
    url: `/pet/comment/${id}`,
    method: 'delete'
  })
}

export function getCommentList(petId, params) {
  return request({
    url: `/pet/comment/list/${petId}`,
    method: 'get',
    params
  })
}

export function likeComment(id) {
  return request({
    url: `/pet/comment/${id}/like`,
    method: 'post'
  })
}

export function unlikeComment(id) {
  return request({
    url: `/pet/comment/${id}/like`,
    method: 'delete'
  })
}