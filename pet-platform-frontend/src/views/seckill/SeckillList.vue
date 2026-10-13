<template>
  <div class="seckill-list-page">
    <!-- 时间段选择 -->
    <div class="time-slots">
      <div
        v-for="slot in timeSlots"
        :key="slot.value"
        class="time-slot"
        :class="{ active: activeSlot === slot.value }"
        @click="activeSlot = slot.value"
      >
        <div class="slot-time">{{ slot.label }}</div>
        <div class="slot-status">{{ slot.statusText }}</div>
      </div>
    </div>

    <!-- 秒杀商品列表 -->
    <div class="seckill-goods-container" v-loading="loading">
      <el-empty v-if="!loading && seckillGoods.length === 0" description="暂无秒杀活动" />

      <el-row :gutter="20" v-else>
        <el-col :span="6" v-for="item in seckillGoods" :key="item.id">
          <el-card class="seckill-card" shadow="hover" @click="goToDetail(item.id)">
            <div class="seckill-badge" :class="getStatusClass(item.status)">
              {{ getStatusText(item.status) }}
            </div>

            <el-image :src="getGoodsImage(item)" fit="cover" class="seckill-img">
              <template #error>
                <div class="image-placeholder">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>

            <div class="seckill-info">
              <h4 class="seckill-title">{{ item.goodsName || item.name }}</h4>

              <div class="seckill-time" v-if="item.status === 0">
                <el-icon><Clock /></el-icon>
                <span>{{ formatTime(item.startTime) }} 开始</span>
              </div>

              <div class="seckill-countdown" v-if="item.status === 1">
                <CountdownTimer :endTime="item.endTime" showLabel />
              </div>

              <div class="seckill-price">
                <span class="price-current">¥{{ item.seckillPrice }}</span>
                <span class="price-original">¥{{ item.goodsPrice || item.price }}</span>
                <span class="price-discount">{{ getDiscount(item) }}折</span>
              </div>

              <div class="seckill-stock">
                <div class="stock-info">
                  <span>库存: {{ item.stockCount - (item.soldCount || 0) }}/{{ item.stockCount }}</span>
                </div>
                <el-progress
                  :percentage="getSoldPercentage(item)"
                  :stroke-width="6"
                  :color="progressColors[item.status] || '#909399'"
                />
              </div>

              <el-button
                :type="getButtonType(item.status)"
                :disabled="!canBuy(item)"
                class="seckill-btn"
                @click.stop="handleSeckill(item)"
              >
                {{ getButtonText(item.status) }}
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 分页 -->
      <div class="pagination" v-if="total > pageSize">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getSeckillGoodsList, getActiveSeckillGoods } from '@/api/seckill'
import { getImageUrlWithDefault } from '@/utils/image'
import { ElMessage } from 'element-plus'
import { Picture, Clock } from '@element-plus/icons-vue'
import CountdownTimer from '@/components/common/CountdownTimer.vue'

const router = useRouter()
const defaultImage = 'https://via.placeholder.com/300x200?text=No+Image'

const loading = ref(false)
const seckillGoods = ref([])
const currentPage = ref(1)
const pageSize = ref(8)
const total = ref(0)
const activeSlot = ref('all')

// 获取商品图片
const getGoodsImage = (item) => {
  return getImageUrlWithDefault(item.goodsImg || item.image, defaultImage)
}

const timeSlots = ref([
  { value: 'all', label: '全部', statusText: '', status: null },
  { value: 'pending', label: '即将开始', statusText: '预告', status: 0 },
  { value: 'active', label: '正在秒杀', statusText: '抢购中', status: 1 },
  { value: 'ended', label: '已结束', statusText: '已结束', status: 2 }
])

const progressColors = {
  0: '#e6a23c',
  1: '#f5576c',
  2: '#909399'
}

const getStatusClass = (status) => {
  const map = {
    0: 'status-pending',
    1: 'status-active',
    2: 'status-ended'
  }
  return map[status] || ''
}

const getStatusText = (status) => {
  const map = {
    0: '即将开始',
    1: '抢购中',
    2: '已结束'
  }
  return map[status] || ''
}

const getButtonType = (status) => {
  const map = {
    0: 'warning',
    1: 'danger',
    2: 'info'
  }
  return map[status] || 'info'
}

const getButtonText = (status) => {
  const map = {
    0: '即将开始',
    1: '立即抢购',
    2: '已结束'
  }
  return map[status] || ''
}

const canBuy = (item) => {
  return item.status === 1 && item.stockCount - (item.soldCount || 0) > 0
}

const getDiscount = (item) => {
  if (!item.price || !item.seckillPrice) return ''
  return (item.seckillPrice / item.price * 10).toFixed(1)
}

const getSoldPercentage = (item) => {
  if (!item.stockCount) return 0
  const sold = item.soldCount || 0
  return Math.min(100, Math.round((sold / item.stockCount) * 100))
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${hours}:${minutes}`
}

const goToDetail = (id) => {
  router.push(`/seckill/${id}`)
}

const handleSeckill = (item) => {
  if (item.status !== 1) {
    ElMessage.warning('秒杀活动暂未开始')
    return
  }
  goToDetail(item.id)
}

const handlePageChange = () => {
  fetchSeckillGoods()
}

const fetchSeckillGoods = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }

    // 根据选择的时间段筛选
    if (activeSlot.value !== 'all') {
      params.status = timeSlots.value.find(s => s.value === activeSlot.value)?.status
    }

    let res
    if (activeSlot.value === 'active') {
      res = await getActiveSeckillGoods(params)
    } else {
      res = await getSeckillGoodsList(params)
    }

    if (res.data) {
      seckillGoods.value = res.data.records || res.data || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('Fetch seckill goods failed:', error)
    ElMessage.error('获取秒杀商品失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchSeckillGoods()
})
</script>

<style scoped>
.seckill-list-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

/* 时间段选择 */
.time-slots {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
  background: #fff;
  padding: 15px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.time-slot {
  flex: 1;
  padding: 15px;
  border-radius: 8px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  background: #f5f7fa;
  border: 2px solid transparent;
}

.time-slot:hover {
  background: #ecf5ff;
}

.time-slot.active {
  background: #fef0f0;
  border-color: #f5576c;
}

.slot-time {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.slot-status {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

.time-slot.active .slot-time {
  color: #f5576c;
}

/* 秒杀卡片 */
.seckill-card {
  cursor: pointer;
  position: relative;
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
}

.seckill-badge {
  position: absolute;
  top: 0;
  left: 0;
  padding: 5px 15px;
  color: #fff;
  font-size: 12px;
  z-index: 2;
  border-radius: 0 0 8px 0;
}

.status-pending {
  background: #e6a23c;
}

.status-active {
  background: #f5576c;
  animation: pulse 2s infinite;
}

.status-ended {
  background: #909399;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.8;
  }
}

.seckill-img {
  height: 180px;
  width: 100%;
}

.seckill-info {
  padding: 15px;
}

.seckill-title {
  font-size: 14px;
  color: #333;
  margin: 0 0 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.seckill-time,
.seckill-countdown {
  font-size: 12px;
  color: #e6a23c;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.seckill-countdown {
  color: #f5576c;
}

.seckill-price {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 10px;
}

.price-current {
  font-size: 22px;
  font-weight: bold;
  color: #f5576c;
}

.price-original {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
}

.price-discount {
  font-size: 12px;
  color: #fff;
  background: #f5576c;
  padding: 2px 6px;
  border-radius: 4px;
}

.seckill-stock {
  margin-bottom: 15px;
}

.stock-info {
  font-size: 12px;
  color: #999;
  margin-bottom: 5px;
}

.seckill-btn {
  width: 100%;
}

/* 图片占位符 */
.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #c0c4cc;
  font-size: 40px;
}

/* 分页 */
.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}
</style>