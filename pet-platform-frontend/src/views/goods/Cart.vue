<template>
  <div class="cart-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的购物车</span>
          <el-button type="danger" text @click="handleClearCart" v-if="cartItems.length > 0">
            清空购物车
          </el-button>
        </div>
      </template>

      <div class="cart-container" v-loading="loading">
        <el-empty v-if="!loading && cartItems.length === 0" description="购物车是空的">
          <el-button type="primary" @click="$router.push('/goods')">去购物</el-button>
        </el-empty>

        <template v-else>
          <!-- 购物车列表 -->
          <div class="cart-list">
            <div class="cart-header">
              <el-checkbox v-model="selectAll" @change="handleSelectAll">全选</el-checkbox>
              <span class="header-goods">商品信息</span>
              <span class="header-price">单价</span>
              <span class="header-quantity">数量</span>
              <span class="header-subtotal">小计</span>
              <span class="header-action">操作</span>
            </div>

            <div
              v-for="item in cartItems"
              :key="item.id"
              class="cart-item"
            >
              <el-checkbox v-model="item.selected" @change="updateCartSummary" />
              <div class="item-goods" @click="goToDetail(item.goodsId)">
                <el-image
                  :src="item.image || defaultImage"
                  fit="cover"
                  class="goods-img"
                >
                  <template #error>
                    <div class="image-placeholder">
                      <el-icon><Picture /></el-icon>
                    </div>
                  </template>
                </el-image>
                <div class="goods-info">
                  <h4 class="goods-name">{{ item.name }}</h4>
                  <p class="goods-desc">{{ item.description }}</p>
                </div>
              </div>
              <div class="item-price">¥{{ item.price }}</div>
              <div class="item-quantity">
                <el-input-number
                  v-model="item.quantity"
                  :min="1"
                  :max="item.stock || 99"
                  size="small"
                  @change="(val) => handleQuantityChange(item, val)"
                />
              </div>
              <div class="item-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
              <div class="item-action">
                <el-button type="danger" text @click="handleRemove(item)">
                  删除
                </el-button>
              </div>
            </div>
          </div>

          <!-- 结算栏 -->
          <div class="cart-footer">
            <div class="footer-left">
              <el-checkbox v-model="selectAll" @change="handleSelectAll">全选</el-checkbox>
              <el-button type="danger" text @click="handleBatchRemove" :disabled="!hasSelectedItems">
                删除选中
              </el-button>
            </div>
            <div class="footer-right">
              <div class="summary">
                <span>已选 <b>{{ selectedCount }}</b> 件商品</span>
                <span class="total-price">
                  合计: <b>¥{{ totalPrice.toFixed(2) }}</b>
                </span>
              </div>
              <el-button type="danger" size="large" :disabled="!hasSelectedItems" @click="handleCheckout">
                去结算 ({{ selectedCount }})
              </el-button>
            </div>
          </div>
        </template>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'

const router = useRouter()
const defaultImage = 'https://via.placeholder.com/100x100?text=No+Image'

const loading = ref(false)
const cartItems = ref([])

// 全选
const selectAll = computed({
  get: () => cartItems.value.length > 0 && cartItems.value.every(item => item.selected),
  set: () => {}
})

// 已选数量
const selectedCount = computed(() => {
  return cartItems.value.filter(item => item.selected).reduce((sum, item) => sum + item.quantity, 0)
})

// 总价
const totalPrice = computed(() => {
  return cartItems.value
    .filter(item => item.selected)
    .reduce((sum, item) => sum + item.price * item.quantity, 0)
})

// 是否有选中商品
const hasSelectedItems = computed(() => {
  return cartItems.value.some(item => item.selected)
})

// 加载购物车数据（本地存储模拟）
const loadCart = () => {
  loading.value = true
  try {
    const cart = localStorage.getItem('cart')
    if (cart) {
      cartItems.value = JSON.parse(cart)
    }
  } catch (error) {
    console.error('Load cart failed:', error)
  } finally {
    loading.value = false
  }
}

// 保存购物车
const saveCart = () => {
  localStorage.setItem('cart', JSON.stringify(cartItems.value))
}

// 更新购物车汇总
const updateCartSummary = () => {
  saveCart()
}

// 全选/取消全选
const handleSelectAll = (val) => {
  cartItems.value.forEach(item => {
    item.selected = val
  })
  saveCart()
}

// 数量变化
const handleQuantityChange = (item, val) => {
  saveCart()
}

// 删除单个商品
const handleRemove = (item) => {
  const index = cartItems.value.findIndex(i => i.id === item.id)
  if (index > -1) {
    cartItems.value.splice(index, 1)
    saveCart()
    ElMessage.success('已删除')
  }
}

// 批量删除
const handleBatchRemove = async () => {
  try {
    await ElMessageBox.confirm(
      `确认删除选中的 ${cartItems.value.filter(i => i.selected).length} 件商品？`,
      '删除确认',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    cartItems.value = cartItems.value.filter(item => !item.selected)
    saveCart()
    ElMessage.success('已删除')
  } catch (error) {
    // 用户取消
  }
}

// 清空购物车
const handleClearCart = async () => {
  try {
    await ElMessageBox.confirm(
      '确认清空购物车？',
      '清空确认',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    cartItems.value = []
    saveCart()
    ElMessage.success('购物车已清空')
  } catch (error) {
    // 用户取消
  }
}

// 去结算
const handleCheckout = () => {
  const selectedItems = cartItems.value.filter(item => item.selected)
  if (selectedItems.length === 0) {
    ElMessage.warning('请选择要结算的商品')
    return
  }

  // TODO: 对接结算API
  ElMessage.info('结算功能开发中...')
}

// 跳转商品详情
const goToDetail = (goodsId) => {
  if (goodsId) {
    router.push(`/goods/${goodsId}`)
  }
}

onMounted(() => {
  loadCart()
})
</script>

<style scoped>
.cart-page {
  max-width: var(--content-max-width);
  margin: 0 auto;
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.cart-container {
  min-height: 300px;
}

/* 购物车头部 */
.cart-header {
  display: flex;
  align-items: center;
  padding: 15px 0;
  border-bottom: 2px solid #ebeef5;
  font-size: 14px;
  color: #666;
}

.header-goods {
  flex: 1;
  margin-left: 20px;
}

.header-price,
.header-quantity,
.header-subtotal,
.header-action {
  width: 120px;
  text-align: center;
}

/* 购物车商品项 */
.cart-item {
  display: flex;
  align-items: center;
  padding: 20px 0;
  border-bottom: 1px solid #ebeef5;
}

.item-goods {
  flex: 1;
  display: flex;
  gap: 15px;
  margin-left: 20px;
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

.goods-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.goods-name {
  font-size: 14px;
  color: #333;
  margin: 0 0 10px;
}

.goods-desc {
  font-size: 12px;
  color: #999;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.item-price,
.item-quantity,
.item-subtotal,
.item-action {
  width: 120px;
  text-align: center;
}

.item-price {
  font-size: 14px;
  color: #333;
}

.item-subtotal {
  font-size: 16px;
  font-weight: bold;
  color: #f5576c;
}

/* 结算栏 */
.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0;
  border-top: 1px solid #ebeef5;
  margin-top: 20px;
  position: sticky;
  bottom: 0;
  background: #fff;
}

.footer-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.footer-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.summary {
  display: flex;
  align-items: center;
  gap: 15px;
  font-size: 14px;
  color: #666;
}

.summary b {
  color: #f5576c;
  font-size: 18px;
}

.total-price {
  font-size: 16px;
}

.total-price b {
  font-size: 24px;
}
</style>