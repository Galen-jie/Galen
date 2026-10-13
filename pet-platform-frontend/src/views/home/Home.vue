<template>
  <div class="home">
    <!-- 轮播图区域 -->
    <el-carousel height="400px" class="banner-carousel">
      <el-carousel-item v-for="item in banners" :key="item.id">
        <div class="banner-item" :style="{ background: item.bgColor }">
          <div class="banner-content">
            <h2>{{ item.title }}</h2>
            <p>{{ item.desc }}</p>
            <el-button type="primary" size="large" @click="$router.push(item.link)">
              {{ item.btnText }}
            </el-button>
          </div>
        </div>
      </el-carousel-item>
    </el-carousel>

    <!-- 统计数据 -->
    <div class="stats-section">
      <el-row :gutter="20">
        <el-col :span="6" v-for="stat in stats" :key="stat.label">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-icon" :style="{ background: stat.color }">
              <component :is="stat.icon" />
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stat.value }}</div>
              <div class="stat-label">{{ stat.label }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 秒杀专区 -->
    <section class="section" v-if="seckillGoods.length > 0">
      <div class="section-header">
        <h2>
          <el-icon class="flash-icon"><Lightning /></el-icon>
          限时秒杀
        </h2>
        <el-button text type="primary" @click="$router.push('/seckill')">
          查看更多 <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
      <div class="seckill-list">
        <el-row :gutter="20">
          <el-col :span="6" v-for="item in seckillGoods" :key="item.id">
            <el-card class="seckill-card" shadow="hover" @click="$router.push(`/seckill/${item.id}`)">
              <div class="seckill-status" :class="getStatusClass(item.status)">
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
                <h4 class="seckill-title">{{ getGoodsName(item) }}</h4>
                <div class="seckill-price">
                  <span class="seckill-price-current">¥{{ item.seckillPrice }}</span>
                  <span class="seckill-price-original">¥{{ getGoodsPrice(item) }}</span>
                </div>
                <div class="seckill-progress">
                  <el-progress
                    :percentage="getSoldPercentage(item)"
                    :stroke-width="8"
                    :color="progressColor"
                  />
                  <span class="seckill-stock">已售 {{ item.soldCount || 0 }} 件</span>
                </div>
                <div class="seckill-countdown" v-if="item.status === 1">
                  <CountdownTimer :endTime="item.endTime" />
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </section>

    <!-- 最新宠物 -->
    <section class="section">
      <div class="section-header">
        <h2>
          <el-icon><Avatar /></el-icon>
          等待领养
        </h2>
        <el-button text type="primary" @click="$router.push('/pet')">
          查看更多 <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
      <div class="pet-list">
        <el-row :gutter="20">
          <el-col :span="6" v-for="pet in pets" :key="pet.id">
            <el-card class="pet-card" shadow="hover" @click="$router.push(`/pet/${pet.id}`)">
              <el-image :src="getPetImage(pet)" fit="cover" class="pet-img">
                <template #error>
                  <div class="image-placeholder">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <div class="pet-info">
                <h4 class="pet-name">{{ pet.name }}</h4>
                <div class="pet-tags">
                  <el-tag size="small" :type="pet.gender === '公' ? 'primary' : 'danger'">
                    {{ pet.gender }}
                  </el-tag>
                  <el-tag size="small" type="info">{{ pet.age }}</el-tag>
                  <el-tag size="small">{{ pet.breed }}</el-tag>
                </div>
                <p class="pet-desc">{{ pet.description }}</p>
                <div class="pet-location">
                  <el-icon><Location /></el-icon>
                  {{ pet.location || '未知' }}
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </section>

    <!-- 热门商品 -->
    <section class="section">
      <div class="section-header">
        <h2>
          <el-icon><ShoppingBag /></el-icon>
          热门商品
        </h2>
        <el-button text type="primary" @click="$router.push('/goods')">
          查看更多 <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
      <div class="goods-list">
        <el-row :gutter="20">
          <el-col :span="6" v-for="goods in goodsList" :key="goods.id">
            <el-card class="goods-card" shadow="hover" @click="$router.push(`/goods/${goods.id}`)">
              <el-image :src="getGoodsImage(goods)" fit="cover" class="goods-img">
                <template #error>
                  <div class="image-placeholder">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <div class="goods-info">
                <h4 class="goods-name">{{ getGoodsName(goods) }}</h4>
                <div class="goods-price">
                  <span class="price-current">¥{{ getGoodsPrice(goods) }}</span>
                </div>
                <div class="goods-sales">已售 {{ goods.sales || 0 }} 件</div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, markRaw, computed } from 'vue'
import { getPetList } from '@/api/pet'
import { getGoodsList } from '@/api/goods'
import { getActiveSeckillGoods } from '@/api/seckill'
import { getImageUrlWithDefault, DEFAULT_IMAGE } from '@/utils/image'
import { Lightning, ArrowRight, Avatar, ShoppingBag, Location, Picture } from '@element-plus/icons-vue'
import CountdownTimer from '@/components/common/CountdownTimer.vue'

const banners = ref([
  {
    id: 1,
    title: '给流浪动物一个温暖的家',
    desc: '每一只小动物都值得被温柔以待',
    btnText: '立即领养',
    link: '/pet',
    bgColor: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
  },
  {
    id: 2,
    title: '限时秒杀 超值优惠',
    desc: '宠物用品低至3折起',
    btnText: '抢购中',
    link: '/seckill',
    bgColor: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
  },
  {
    id: 3,
    title: '优质宠物用品',
    desc: '精选好物 品质保障',
    btnText: '去逛逛',
    link: '/goods',
    bgColor: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
  }
])

const stats = ref([
  { label: '救助宠物', value: '1,200+', icon: markRaw(Avatar), color: '#667eea' },
  { label: '成功领养', value: '856', icon: markRaw(Avatar), color: '#f5576c' },
  { label: '爱心用户', value: '5,000+', icon: markRaw(Avatar), color: '#4facfe' },
  { label: '爱心商品', value: '300+', icon: markRaw(ShoppingBag), color: '#43e97b' }
])

const pets = ref([])
const goodsList = ref([])
const seckillGoods = ref([])
const loading = ref(false)

const progressColor = '#f5576c'

// 获取商品图片URL（兼容后端字段名）
const getGoodsImage = (goods) => {
  return getImageUrlWithDefault(goods.goodsImg || goods.image)
}

// 获取商品名称（兼容后端字段名）
const getGoodsName = (goods) => {
  return goods.goodsName || goods.name || '未知商品'
}

// 获取商品价格（兼容后端字段名）
const getGoodsPrice = (goods) => {
  return goods.goodsPrice || goods.price || 0
}

// 获取宠物图片URL
const getPetImage = (pet) => {
  // PetRescueVO 返回 imageUrls (List<String>)
  let images = pet.imageUrls || pet.images
  if (typeof images === 'string') {
    try {
      images = JSON.parse(images)
    } catch {
      images = images.split(',')
    }
  }
  const firstImage = Array.isArray(images) ? images[0] : images
  return getImageUrlWithDefault(firstImage)
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
    1: '进行中',
    2: '已结束'
  }
  return map[status] || ''
}

const getSoldPercentage = (item) => {
  if (!item.stockCount) return 0
  const sold = item.soldCount || 0
  return Math.round((sold / item.stockCount) * 100)
}

const fetchData = async () => {
  loading.value = true
  try {
    // 获取宠物列表
    const petRes = await getPetList({ pageNum: 1, pageSize: 4 })
    if (petRes.data?.records) {
      pets.value = petRes.data.records
    }

    // 获取商品列表
    const goodsRes = await getGoodsList({ pageNum: 1, pageSize: 4 })
    if (goodsRes.data?.records) {
      goodsList.value = goodsRes.data.records
    }

    // 获取秒杀商品（使用列表接口获取所有状态的商品）
    const seckillRes = await getSeckillGoodsList({ pageNum: 1, pageSize: 4 })
    if (seckillRes.data?.records) {
      seckillGoods.value = seckillRes.data.records
    }
  } catch (error) {
    console.error('Fetch data failed:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.home {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.banner-carousel {
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 30px;
}

.banner-item {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.banner-content {
  text-align: center;
  color: #fff;
}

.banner-content h2 {
  font-size: 36px;
  margin-bottom: 15px;
}

.banner-content p {
  font-size: 18px;
  margin-bottom: 25px;
  opacity: 0.9;
}

.stats-section {
  margin-bottom: 40px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 15px;
}

.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 15px;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
  margin-right: 15px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 5px;
}

.section {
  margin-bottom: 40px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h2 {
  font-size: 22px;
  color: #333;
  display: flex;
  align-items: center;
  gap: 8px;
}

.flash-icon {
  color: #f5576c;
}

/* 秒杀卡片 */
.seckill-card {
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.seckill-status {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  color: #fff;
  z-index: 1;
}

.status-pending {
  background: #e6a23c;
}

.status-active {
  background: #f5576c;
}

.status-ended {
  background: #909399;
}

.seckill-img {
  height: 150px;
  width: 100%;
}

.seckill-info {
  padding: 10px 0;
}

.seckill-title {
  font-size: 14px;
  color: #333;
  margin: 0 0 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.seckill-price {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 10px;
}

.seckill-price-current {
  font-size: 20px;
  font-weight: bold;
  color: #f5576c;
}

.seckill-price-original {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
}

.seckill-progress {
  margin-bottom: 8px;
}

.seckill-stock {
  font-size: 12px;
  color: #999;
}

.seckill-countdown {
  font-size: 12px;
  color: #f5576c;
  margin-top: 5px;
}

/* 宠物卡片 */
.pet-card {
  cursor: pointer;
}

.pet-img {
  height: 180px;
  width: 100%;
}

.pet-info {
  padding: 10px 0;
}

.pet-name {
  font-size: 16px;
  color: #333;
  margin: 0 0 8px;
}

.pet-tags {
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}

.pet-desc {
  font-size: 13px;
  color: #666;
  margin: 0 0 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.pet-location {
  font-size: 12px;
  color: #999;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 商品卡片 */
.goods-card {
  cursor: pointer;
}

.goods-img {
  height: 180px;
  width: 100%;
}

.goods-info {
  padding: 10px 0;
}

.goods-name {
  font-size: 14px;
  color: #333;
  margin: 0 0 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.goods-price {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 5px;
}

.price-current {
  font-size: 18px;
  font-weight: bold;
  color: #f5576c;
}

.price-original {
  font-size: 13px;
  color: #999;
  text-decoration: line-through;
}

.goods-sales {
  font-size: 12px;
  color: #999;
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
</style>