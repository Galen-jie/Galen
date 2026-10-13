import request from '@/utils/request'

export function createAdoption(data) {
  return request({
    url: '/adoption',
    method: 'post',
    data
  })
}

export function cancelAdoption(id) {
  return request({
    url: `/adoption/${id}/cancel`,
    method: 'put'
  })
}

export function reviewAdoption(data) {
  return request({
    url: '/adoption/review',
    method: 'put',
    data
  })
}

export function getAdoptionDetail(id) {
  return request({
    url: `/adoption/${id}`,
    method: 'get'
  })
}

export function getMyAdoptions(params) {
  return request({
    url: '/adoption/my',
    method: 'get',
    params
  })
}

export function getPendingAdoptions(params) {
  return request({
    url: '/adoption/pending',
    method: 'get',
    params
  })
}

export function getAdoptionsByPetId(petId, params) {
  return request({
    url: `/adoption/pet/${petId}`,
    method: 'get',
    params
  })
}