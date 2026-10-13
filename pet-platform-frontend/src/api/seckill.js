import request from '@/utils/request'

export function getSeckillGoodsList(params) {
  return request({
    url: '/seckill/goods/list',
    method: 'get',
    params
  })
}

export function getActiveSeckillGoods(params) {
  return request({
    url: '/seckill/goods/active',
    method: 'get',
    params
  })
}

export function getSeckillGoodsDetail(id) {
  return request({
    url: `/seckill/goods/${id}`,
    method: 'get'
  })
}

export function createSeckillOrder(data) {
  return request({
    url: '/seckill/order',
    method: 'post',
    data
  })
}

export function getMySeckillOrders(params) {
  return request({
    url: '/seckill/order/my',
    method: 'get',
    params
  })
}

export function getSeckillOrderDetail(id) {
  return request({
    url: `/seckill/order/${id}`,
    method: 'get'
  })
}

export function paySeckillOrder(orderId) {
  return request({
    url: `/seckill/order/${orderId}/pay`,
    method: 'post'
  })
}

export function cancelSeckillOrder(orderId) {
  return request({
    url: `/seckill/order/${orderId}/cancel`,
    method: 'post'
  })
}