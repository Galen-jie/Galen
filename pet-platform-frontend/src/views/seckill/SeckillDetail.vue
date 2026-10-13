<template>
  <div class="seckill-detail-page" v-loading="loading">
    <el-empty v-if="!loading && !goodsDetail" description="商品不存在" />

    <template v-else-if="goodsDetail">
      <el-row :gutter="30">
        <!-- 商品图片 -->
        <el-col :span="12">
          <div class="goods-images">
            <el-image
              :src="currentImage"
              fit="contain"
              class="main-image"
              :preview-src-list="imageList"
            >
              <template #error>
                <div class="image-placeholder">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </div>
        </el-col>

        <!-- 商品信息 -->
        <el-col :span="12">
          <div class="goods-info">
            <div class="goods-header">
              <el-tag :type="statusTag.type" size="large">{{ statusTag.text }}</el-tag>
              <h1 class="goods-name">{{ goodsDetail.goodsName }}</h1>
            </div>

            <!-- 秒杀价格 -->
            <div class="price-section">
              <div class="seckill-price-info">
                <span class="label">秒杀价</span>
                <span class="seckill-price">¥{{ goodsDetail.seckillPrice }}</span>
                <span class="original-price">原价 ¥{{ goodsDetail.goodsPrice || goodsDetail.price }}</span>
              </div>
              <div class="discount-tag">
                {{ discount }}折
              </div>
            </div>

            <!-- 活动时间 -->
            <div class="time-section">
              <div class="time-item">
                <span class="label">开始时间</span>
                <span class="value">{{ formatDateTime(goodsDetail.startTime) }}</span>
              </div>
              <div class="time-item">
                <span class="label">结束时间</span>
                <span class="value">{{ formatDateTime(goodsDetail.endTime) }}</span>
              </div>
            </div>

            <!-- 倒计时 -->
            <div class="countdown-section" v-if="goodsDetail.status === 1">
              <span class="countdown-label">距结束</span>
              <CountdownTimer :endTime="goodsDetail.endTime" />
            </div>
            <div class="countdown-section pending" v-else-if="goodsDetail.status === 0">
              <span class="countdown-label">距开始</span>
              <CountdownTimer :endTime="goodsDetail.startTime" />
            </div>

            <!-- 库存信息 -->
            <div class="stock-section">
              <div class="stock-row">
                <span class="label">库存</span>
                <span class="value">
                  {{ goodsDetail.stockCount - (goodsDetail.soldCount || 0) }} 件
                </span>
              </div>
              <div class="stock-row">
                <span class="label">已售</span>
                <span class="value">{{ goodsDetail.soldCount || 0 }} 件</span>
              </div>
              <el-progress
                :percentage="soldPercentage"
                :stroke-width="10"
                :color="progressColor"
              />
            </div>

            <!-- 购买数量 -->
            <div class="quantity-section">
              <span class="label">购买数量</span>
              <el-input-number
                v-model="quantity"
                :min="1"
                :max="maxQuantity"
                :disabled="!canBuy"
              />
              <span class="stock-tip">剩余 {{ remainStock }} 件</span>
            </div>

            <!-- 操作按钮 -->
            <div class="action-section">
              <el-button
                type="danger"
                size="large"
                :disabled="!canBuy"
                :loading="submitting"
                @click="handleSeckill"
              >
                {{ buttonText }}
              </el-button>
            </div>

            <!-- 提示信息 -->
            <div class="tips-section">
              <h4>秒杀说明</h4>
              <ul>
                <li>秒杀商品限时限量，先到先得</li>
                <li>秒杀成功后请在30分钟内完成支付</li>
                <li>未支付的订单将自动取消</li>
                <li>每个用户限购 {{ goodsDetail.limitCount || 1 }} 件</li>
              </ul>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 商品详情 -->
      <div class="detail-section">
        <el-tabs>
          <el-tab-pane label="商品详情">
            <div class="detail-content" v-html="goodsDetail.detail || '暂无详情'"></div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getSeckillGoodsDetail, createSeckillOrder } from '@/api/seckill'
import { getImageUrlWithDefault } from '@/utils/image'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import CountdownTimer from '@/components/common/CountdownTimer.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const submitting = ref(false)
const goodsDetail = ref(null)
const quantity = ref(1)

const defaultImage = 'https://via.placeholder.com/400x400?text=No+Image'

const currentImage = computed(() => {
  return getImageUrlWithDefault(goodsDetail.value?.goodsImg || goodsDetail.value?.image, defaultImage)
})

const imageList = computed(() => {
  return [currentImage.value]
})

const statusTag = computed(() => {
  const map = {
    0: { type: 'warning', text: '即将开始' },
    1: { type: 'danger', text: '抢购中' },
    2: { type: 'info', text: '已结束' }
  }
  return map[goodsDetail.value?.status] || { type: 'info', text: '未知' }
})

const discount = computed(() => {
  const price = goodsDetail.value?.goodsPrice || goodsDetail.value?.price
  if (!price || !goodsDetail.value?.seckillPrice) return ''
  return (goodsDetail.value.seckillPrice / price * 10).toFixed(1)
})

const progressColor = computed(() => {
  const map = {
    0: '#e6a23c',
    1: '#f5576c',
    2: '#909399'
  }
  return map[goodsDetail.value?.status] || '#909399'
})

const soldPercentage = computed(() => {
  if (!goodsDetail.value?.stockCount) return 0
  const sold = goodsDetail.value.soldCount || 0
  return Math.min(100, Math.round((sold / goodsDetail.value.stockCount) * 100))
})

const remainStock = computed(() => {
  if (!goodsDetail.value) return 0
  return goodsDetail.value.stockCount - (goodsDetail.value.soldCount || 0)
})

const maxQuantity = computed(() => {
  const limit = goodsDetail.value?.limitCount || 1
  return Math.min(limit, remainStock.value)
})

const canBuy = computed(() => {
  return goodsDetail.value?.status === 1 && remainStock.value > 0
})

const buttonText = computed(() => {
  if (goodsDetail.value?.status === 0) return '即将开始'
  if (goodsDetail.value?.status === 2) return '已结束'
  if (remainStock.value <= 0) return '已售罄'
  return '立即抢购'
})

const formatDateTime = (time) => {
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

const handleSeckill = async () => {
  if (!canBuy.value) {
    ElMessage.warning('当前无法购买')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认抢购 ${goodsDetail.value.goodsName} x ${quantity.value}？`,
      '确认抢购',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    submitting.value = true
    const res = await createSeckillOrder({
      seckillId: goodsDetail.value.id,
      goodsId: goodsDetail.value.goodsId,
      quantity: quantity.value
    })

    if (res.code === 200) {
      ElMessage.success('抢购成功，请尽快支付！')
      router.push('/seckill/orders')
    } else {
      ElMessage.error(res.message || '抢购失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Seckill failed:', error)
      ElMessage.error('抢购失败，请稍后重试')
    }
  } finally {
    submitting.value = false
  }
}

const fetchDetail = async () => {
  const id = route.params.id
  if (!id) {
    ElMessage.error('商品ID不存在')
    return
  }

  loading.value = true
  try {
    const res = await getSeckillGoodsDetail(id)
    if (res.data) {
      goodsDetail.value = res.data
    } else {
      ElMessage.error('商品不存在')
    }
  } catch (error) {
    console.error('Fetch detail failed:', error)
    ElMessage.error('获取商品详情失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchDetail()
})
</script>

<style scoped>
.seckill-detail-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  min-height: 500px;
}

/* 商品图片 */
.goods-images {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}

.main-image {
  width: 100%;
  height: 400px;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #c0c4cc;
  font-size: 60px;
}

/* 商品信息 */
.goods-info {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}

.goods-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
}

.goods-name {
  font-size: 24px;
  color: #333;
  margin: 0;
  flex: 1;
}

/* 价格区域 */
.price-section {
  background: linear-gradient(135deg, #fff5f5 0%, #ffe0e0 100%);
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.seckill-price-info {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.seckill-price-info .label {
  font-size: 14px;
  color: #f5576c;
}

.seckill-price {
  font-size: 36px;
  font-weight: bold;
  color: #f5576c;
}

.original-price {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
}

.discount-tag {
  background: #f5576c;
  color: #fff;
  padding: 5px 15px;
  border-radius: 20px;
  font-weight: bold;
}

/* 时间信息 */
.time-section {
  margin-bottom: 20px;
}

.time-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px dashed #eee;
}

.time-item .label {
  width: 80px;
  color: #999;
  font-size: 14px;
}

.time-item .value {
  color: #333;
  font-size: 14px;
}

/* 倒计时 */
.countdown-section {
  background: #fef0f0;
  padding: 15px 20px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
}

.countdown-section.pending {
  background: #fdf6ec;
}

.countdown-label {
  font-size: 14px;
  color: #f5576c;
}

.countdown-section.pending .countdown-label {
  color: #e6a23c;
}

/* 库存信息 */
.stock-section {
  margin-bottom: 20px;
}

.stock-row {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.stock-row .label {
  width: 60px;
  color: #999;
  font-size: 14px;
}

.stock-row .value {
  color: #333;
  font-size: 14px;
}

/* 数量选择 */
.quantity-section {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  gap: 15px;
}

.quantity-section .label {
  width: 80px;
  color: #999;
  font-size: 14px;
}

.stock-tip {
  font-size: 12px;
  color: #999;
}

/* 操作按钮 */
.action-section {
  margin-bottom: 30px;
}

.action-section .el-button {
  width: 200px;
  height: 50px;
  font-size: 18px;
}

/* 提示信息 */
.tips-section {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 8px;
}

.tips-section h4 {
  margin: 0 0 10px;
  font-size: 14px;
  color: #333;
}

.tips-section ul {
  margin: 0;
  padding-left: 20px;
}

.tips-section li {
  font-size: 12px;
  color: #666;
  line-height: 2;
}

/* 商品详情 */
.detail-section {
  margin-top: 30px;
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}

.detail-content {
  padding: 20px 0;
  font-size: 14px;
  color: #333;
  line-height: 1.8;
}
</style>