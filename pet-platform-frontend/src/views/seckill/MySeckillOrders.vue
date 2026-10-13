<template>
  <div class="my-orders-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的秒杀订单</span>
          <el-radio-group v-model="statusFilter" size="small" @change="handleFilterChange">
            <el-radio-button value="">全部订单</el-radio-button>
            <el-radio-button value="0">待支付</el-radio-button>
            <el-radio-button value="1">已支付</el-radio-button>
            <el-radio-button value="2">已取消</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <div class="orders-container" v-loading="loading">
        <el-empty v-if="!loading && orders.length === 0" description="暂无订单" />

        <div class="order-list" v-else>
          <div
            v-for="order in orders"
            :key="order.id"
            class="order-item"
          >
            <div class="order-header">
              <div class="order-info">
                <span class="order-no">订单号: {{ order.orderNo }}</span>
                <span class="order-time">{{ formatTime(order.createTime) }}</span>
              </div>
              <el-tag :type="getStatusType(order.status)">
                {{ getStatusText(order.status) }}
              </el-tag>
            </div>

            <div class="order-body">
              <div class="goods-info" @click="goToDetail(order.seckillId)">
                <el-image
                  :src="order.goodsImage || defaultImage"
                  fit="cover"
                  class="goods-img"
                >
                  <template #error>
                    <div class="image-placeholder">
                      <el-icon><Picture /></el-icon>
                    </div>
                  </template>
                </el-image>
                <div class="goods-detail">
                  <h4 class="goods-name">{{ order.goodsName }}</h4>
                  <p class="goods-price">
                    <span class="price-current">¥{{ order.seckillPrice }}</span>
                    <span class="price-original">¥{{ order.price }}</span>
                  </p>
                  <p class="goods-quantity">x {{ order.quantity }}</p>
                </div>
              </div>

              <div class="order-summary">
                <div class="summary-row">
                  <span class="label">商品总价</span>
                  <span class="value">¥{{ order.seckillPrice * order.quantity }}</span>
                </div>
                <div class="summary-row total">
                  <span class="label">实付款</span>
                  <span class="value">¥{{ order.payAmount || order.seckillPrice * order.quantity }}</span>
                </div>
              </div>

              <div class="order-actions">
                <template v-if="order.status === 0">
                  <div class="countdown-tip" v-if="order.remainingTime > 0">
                    <el-icon><Clock /></el-icon>
                    剩余支付时间:
                    <CountdownTimer :endTime="getPaymentDeadline(order)" />
                  </div>
                  <el-button type="danger" size="small" @click="handlePay(order)">
                    立即支付
                  </el-button>
                  <el-button size="small" @click="handleCancel(order)">
                    取消订单
                  </el-button>
                </template>
                <template v-else-if="order.status === 1">
                  <el-button type="primary" size="small" plain @click="goToDetail(order.seckillId)">
                    再次购买
                  </el-button>
                </template>
                <template v-else>
                  <el-button type="info" size="small" plain @click="handleDelete(order)">
                    删除订单
                  </el-button>
                </template>
              </div>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div class="pagination" v-if="total > pageSize">
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            @current-change="fetchOrders"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMySeckillOrders, paySeckillOrder, cancelSeckillOrder } from '@/api/seckill'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture, Clock } from '@element-plus/icons-vue'
import CountdownTimer from '@/components/common/CountdownTimer.vue'

const router = useRouter()
const defaultImage = 'https://via.placeholder.com/100x100?text=No+Image'

const loading = ref(false)
const orders = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const statusFilter = ref('')

const getStatusType = (status) => {
  const map = {
    0: 'warning',
    1: 'success',
    2: 'info',
    3: 'danger'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    0: '待支付',
    1: '已支付',
    2: '已取消',
    3: '已退款'
  }
  return map[status] || '未知'
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const getPaymentDeadline = (order) => {
  // 订单创建后30分钟
  const createTime = new Date(order.createTime).getTime()
  return new Date(createTime + 30 * 60 * 1000)
}

const goToDetail = (seckillId) => {
  if (seckillId) {
    router.push(`/seckill/${seckillId}`)
  }
}

const handleFilterChange = () => {
  currentPage.value = 1
  fetchOrders()
}

const handlePay = async (order) => {
  try {
    await ElMessageBox.confirm(
      `确认支付 ¥${order.payAmount || order.seckillPrice * order.quantity}？`,
      '支付确认',
      {
        confirmButtonText: '确认支付',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const res = await paySeckillOrder(order.id)
    if (res.code === 200) {
      ElMessage.success('支付成功')
      fetchOrders()
    } else {
      ElMessage.error(res.message || '支付失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Pay failed:', error)
      ElMessage.error('支付失败')
    }
  }
}

const handleCancel = async (order) => {
  try {
    await ElMessageBox.confirm(
      '确认取消该订单？',
      '取消订单',
      {
        confirmButtonText: '确认',
        cancelButtonText: '不取消',
        type: 'warning'
      }
    )

    const res = await cancelSeckillOrder(order.id)
    if (res.code === 200) {
      ElMessage.success('订单已取消')
      fetchOrders()
    } else {
      ElMessage.error(res.message || '取消失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Cancel failed:', error)
      ElMessage.error('取消失败')
    }
  }
}

const handleDelete = async (order) => {
  try {
    await ElMessageBox.confirm(
      '确认删除该订单？',
      '删除订单',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // TODO: 调用删除订单API
    ElMessage.info('删除功能开发中...')
  } catch (error) {
    // 用户取消
  }
}

const fetchOrders = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }

    if (statusFilter.value !== '') {
      params.status = parseInt(statusFilter.value)
    }

    const res = await getMySeckillOrders(params)
    if (res.data) {
      orders.value = res.data.records || res.data || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('Fetch orders failed:', error)
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.my-orders-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.orders-container {
  min-height: 300px;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.order-item {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
}

.order-info {
  display: flex;
  gap: 20px;
}

.order-no {
  font-size: 14px;
  color: #333;
}

.order-time {
  font-size: 12px;
  color: #999;
}

.order-body {
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 20px;
}

.goods-info {
  display: flex;
  gap: 15px;
  flex: 1;
  cursor: pointer;
}

.goods-img {
  width: 100px;
  height: 100px;
  border-radius: 4px;
  flex-shrink: 0;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #c0c4cc;
  font-size: 30px;
}

.goods-detail {
  flex: 1;
}

.goods-name {
  font-size: 14px;
  color: #333;
  margin: 0 0 10px;
}

.goods-price {
  margin: 0 0 5px;
}

.price-current {
  font-size: 16px;
  font-weight: bold;
  color: #f5576c;
}

.price-original {
  font-size: 12px;
  color: #999;
  text-decoration: line-through;
  margin-left: 8px;
}

.goods-quantity {
  font-size: 12px;
  color: #999;
  margin: 0;
}

.order-summary {
  width: 150px;
  text-align: right;
}

.summary-row {
  margin-bottom: 5px;
}

.summary-row .label {
  font-size: 12px;
  color: #999;
}

.summary-row .value {
  font-size: 14px;
  color: #333;
  margin-left: 10px;
}

.summary-row.total .value {
  font-size: 18px;
  font-weight: bold;
  color: #f5576c;
}

.order-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
}

.countdown-tip {
  font-size: 12px;
  color: #e6a23c;
  display: flex;
  align-items: center;
  gap: 5px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>