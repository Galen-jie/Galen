import request from '@/utils/request'

export function getPetList(params) {
  return request({
    url: '/pet/list',
    method: 'get',
    params
  })
}

export function getPetDetail(id) {
  return request({
    url: `/pet/${id}`,
    method: 'get'
  })
}

export function createPet(data) {
  return request({
    url: '/pet',
    method: 'post',
    data
  })
}

export function updatePet(id, data) {
  return request({
    url: `/pet/${id}`,
    method: 'put',
    data
  })
}

export function deletePet(id) {
  return request({
    url: `/pet/${id}`,
    method: 'delete'
  })
}

export function offlinePet(id) {
  return request({
    url: `/pet/${id}/offline`,
    method: 'put'
  })
}

export function getMyPets(params) {
  return request({
    url: '/pet/my',
    method: 'get',
    params
  })
}

export function likePet(id) {
  return request({
    url: `/pet/${id}/like`,
    method: 'post'
  })
}

export function unlikePet(id) {
  return request({
    url: `/pet/${id}/like`,
    method: 'delete'
  })
}