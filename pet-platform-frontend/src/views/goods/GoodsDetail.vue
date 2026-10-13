<template>
  <div class="goods-detail-page" v-loading="loading">
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
            <h1 class="goods-name">{{ goodsDetail.goodsName || goodsDetail.name }}</h1>

            <div class="goods-price-section">
              <div class="price-info">
                <span class="price-label">价格</span>
                <span class="price-current">¥{{ goodsDetail.goodsPrice || goodsDetail.price }}</span>
              </div>
            </div>

            <div class="goods-meta">
              <div class="meta-item">
                <span class="label">销量</span>
                <span class="value">{{ goodsDetail.sales || 0 }} 件</span>
              </div>
              <div class="meta-item">
                <span class="label">库存</span>
                <span class="value">{{ goodsDetail.stock || 0 }} 件</span>
              </div>
              <div class="meta-item">
                <span class="label">分类</span>
                <span class="value">{{ goodsDetail.categoryName || '未分类' }}</span>
              </div>
            </div>

            <div class="goods-desc" v-if="goodsDetail.goodsDetail || goodsDetail.description">
              <h4>商品简介</h4>
              <p>{{ goodsDetail.goodsDetail || goodsDetail.description }}</p>
            </div>

            <!-- 购买数量 -->
            <div class="quantity-section">
              <span class="label">购买数量</span>
              <el-input-number
                v-model="quantity"
                :min="1"
                :max="goodsDetail.stock || 99"
              />
              <span class="stock-tip">剩余 {{ goodsDetail.stock || 0 }} 件</span>
            </div>

            <!-- 操作按钮 -->
            <div class="action-section">
              <el-button type="danger" size="large" @click="handleAddToCart" :disabled="!goodsDetail.stock">
                <el-icon><ShoppingCart /></el-icon>
                加入购物车
              </el-button>
              <el-button type="primary" size="large" plain @click="handleBuyNow" :disabled="!goodsDetail.stock">
                立即购买
              </el-button>
            </div>

            <!-- 服务保障 -->
            <div class="service-section">
              <h4>服务保障</h4>
              <div class="service-tags">
                <el-tag type="success">正品保证</el-tag>
                <el-tag type="success">七天退换</el-tag>
                <el-tag type="success">快速发货</el-tag>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 商品详情 -->
      <div class="detail-section">
        <el-tabs>
          <el-tab-pane label="商品详情">
            <div class="detail-content" v-html="goodsDetail.goodsDetail || goodsDetail.detail || '暂无详情'"></div>
          </el-tab-pane>
          <el-tab-pane label="商品评价">
            <el-empty description="暂无评价" />
          </el-tab-pane>
        </el-tabs>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getGoodsDetail } from '@/api/goods'
import { getImageUrlWithDefault } from '@/utils/image'
import { ElMessage } from 'element-plus'
import { Picture, ShoppingCart } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const goodsDetail = ref(null)
const quantity = ref(1)

const defaultImage = 'https://via.placeholder.com/400x400?text=No+Image'

const currentImage = computed(() => {
  return getImageUrlWithDefault(goodsDetail.value?.goodsImg || goodsDetail.value?.image, defaultImage)
})

const imageList = computed(() => {
  return [currentImage.value]
})

// 加入购物车
const handleAddToCart = () => {
  if (!goodsDetail.value || !goodsDetail.value.stock) {
    ElMessage.warning('商品暂无库存')
    return
  }

  // 从本地存储获取购物车
  let cart = []
  try {
    const cartStr = localStorage.getItem('cart')
    if (cartStr) {
      cart = JSON.parse(cartStr)
    }
  } catch (error) {
    console.error('Get cart failed:', error)
  }

  // 查找商品是否已存在
  const existIndex = cart.findIndex(item => item.id === goodsDetail.value.id)

  if (existIndex > -1) {
    // 更新数量
    cart[existIndex].quantity += quantity.value
  } else {
    // 添加新商品
    cart.push({
      id: goodsDetail.value.id,
      goodsId: goodsDetail.value.id,
      name: goodsDetail.value.goodsName || goodsDetail.value.name,
      image: goodsDetail.value.goodsImg || goodsDetail.value.image,
      price: goodsDetail.value.goodsPrice || goodsDetail.value.price,
      stock: goodsDetail.value.stock,
      quantity: quantity.value,
      selected: true
    })
  }

  // 保存到本地存储
  localStorage.setItem('cart', JSON.stringify(cart))
  ElMessage.success('已加入购物车')

  // 跳转到购物车
  router.push('/cart')
}

// 立即购买
const handleBuyNow = () => {
  if (!goodsDetail.value || !goodsDetail.value.stock) {
    ElMessage.warning('商品暂无库存')
    return
  }

  // TODO: 实现立即购买逻辑
  ElMessage.info('购买功能开发中...')
}

// 加载商品详情
const fetchDetail = async () => {
  const id = route.params.id
  if (!id) {
    ElMessage.error('商品ID不存在')
    return
  }

  loading.value = true
  try {
    const res = await getGoodsDetail(id)
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
.goods-detail-page {
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
  height: 450px;
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

.goods-name {
  font-size: 22px;
  color: #333;
  margin: 0 0 20px;
  line-height: 1.5;
}

/* 价格区域 */
.goods-price-section {
  background: linear-gradient(135deg, #fff5f5 0%, #ffe0e0 100%);
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
}

.price-info {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.price-label {
  font-size: 14px;
  color: #666;
}

.price-current {
  font-size: 32px;
  font-weight: bold;
  color: #f5576c;
}

.price-original {
  font-size: 16px;
  color: #999;
  text-decoration: line-through;
}

/* 商品元信息 */
.goods-meta {
  display: flex;
  gap: 40px;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.meta-item .label {
  font-size: 12px;
  color: #999;
}

.meta-item .value {
  font-size: 14px;
  color: #333;
}

/* 商品简介 */
.goods-desc {
  margin-bottom: 20px;
}

.goods-desc h4 {
  font-size: 14px;
  color: #333;
  margin: 0 0 10px;
}

.goods-desc p {
  font-size: 14px;
  color: #666;
  margin: 0;
  line-height: 1.8;
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
  display: flex;
  gap: 15px;
  margin-bottom: 30px;
}

.action-section .el-button {
  width: 150px;
  height: 45px;
  font-size: 16px;
}

/* 服务保障 */
.service-section {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 8px;
}

.service-section h4 {
  margin: 0 0 10px;
  font-size: 14px;
  color: #333;
}

.service-tags {
  display: flex;
  gap: 10px;
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

.detail-content img {
  max-width: 100%;
  height: auto;
}
</style>