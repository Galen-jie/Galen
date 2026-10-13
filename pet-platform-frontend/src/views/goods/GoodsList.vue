<template>
  <div class="goods-list-page">
    <!-- 搜索和筛选 -->
    <el-card class="filter-card">
      <el-form :inline="true">
        <el-form-item label="商品名称">
          <el-input v-model="searchKeyword" placeholder="搜索商品" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="商品分类">
          <el-select v-model="selectedCategory" placeholder="全部分类" clearable @change="handleCategoryChange">
            <el-option label="全部分类" value="" />
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 排序栏 -->
    <div class="sort-bar">
      <el-radio-group v-model="sortBy" size="small" @change="handleSortChange">
        <el-radio-button value="default">综合</el-radio-button>
        <el-radio-button value="sales">销量</el-radio-button>
        <el-radio-button value="price-asc">价格升序</el-radio-button>
        <el-radio-button value="price-desc">价格降序</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 商品列表 -->
    <div class="goods-container" v-loading="loading">
      <el-empty v-if="!loading && goodsList.length === 0" description="暂无商品" />

      <el-row :gutter="20" v-else>
        <el-col :span="6" v-for="goods in goodsList" :key="goods.id">
          <el-card class="goods-card" shadow="hover" @click="goToDetail(goods.id)">
            <el-image :src="getGoodsImage(goods)" fit="cover" class="goods-img">
              <template #error>
                <div class="image-placeholder">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <div class="goods-info">
              <h4 class="goods-name">{{ getGoodsName(goods) }}</h4>
              <p class="goods-desc">{{ goods.goodsDetail || goods.description }}</p>
              <div class="goods-price">
                <span class="price-current">¥{{ getGoodsPrice(goods) }}</span>
              </div>
              <div class="goods-meta">
                <span class="goods-sales">已售 {{ goods.sales || 0 }}</span>
                <span class="goods-stock">库存 {{ goods.stock || 0 }}</span>
              </div>
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
          layout="prev, pager, next, total"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getGoodsList } from '@/api/goods'
import { getCategoryList } from '@/api/category'
import { getImageUrlWithDefault } from '@/utils/image'
import { ElMessage } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'

const router = useRouter()
const defaultImage = 'https://via.placeholder.com/300x200?text=No+Image'

// 获取商品图片URL
const getGoodsImage = (goods) => {
  return getImageUrlWithDefault(goods.goodsImg || goods.image)
}

// 获取商品名称
const getGoodsName = (goods) => {
  return goods.goodsName || goods.name || '未知商品'
}

// 获取商品价格
const getGoodsPrice = (goods) => {
  return goods.goodsPrice || goods.price || 0
}

const loading = ref(false)
const goodsList = ref([])
const categories = ref([])
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

const searchKeyword = ref('')
const selectedCategory = ref('')
const sortBy = ref('default')

// 加载分类列表
const loadCategories = async () => {
  try {
    const res = await getCategoryList()
    if (res.data) {
      categories.value = res.data
    }
  } catch (error) {
    console.error('Load categories failed:', error)
  }
}

// 加载商品列表
const loadGoods = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }

    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }

    if (selectedCategory.value) {
      params.categoryId = selectedCategory.value
    }

    // 排序
    if (sortBy.value === 'sales') {
      params.sort = 'sales'
      params.order = 'desc'
    } else if (sortBy.value === 'price-asc') {
      params.sort = 'price'
      params.order = 'asc'
    } else if (sortBy.value === 'price-desc') {
      params.sort = 'price'
      params.order = 'desc'
    }

    const res = await getGoodsList(params)
    if (res.data) {
      goodsList.value = res.data.records || res.data || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('Load goods failed:', error)
    ElMessage.error('获取商品列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadGoods()
}

const handleReset = () => {
  searchKeyword.value = ''
  selectedCategory.value = ''
  sortBy.value = 'default'
  currentPage.value = 1
  loadGoods()
}

const handleCategoryChange = () => {
  currentPage.value = 1
  loadGoods()
}

const handleSortChange = () => {
  currentPage.value = 1
  loadGoods()
}

const handlePageChange = () => {
  loadGoods()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const goToDetail = (id) => {
  router.push(`/goods/${id}`)
}

onMounted(() => {
  loadCategories()
  loadGoods()
})
</script>

<style scoped>
.goods-list-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.sort-bar {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}

.goods-container {
  min-height: 500px;
}

.goods-card {
  cursor: pointer;
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.3s;
}

.goods-card:hover {
  transform: translateY(-5px);
}

.goods-img {
  height: 200px;
  width: 100%;
}

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

.goods-info {
  padding: 15px;
}

.goods-name {
  font-size: 14px;
  color: #333;
  margin: 0 0 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.goods-desc {
  font-size: 12px;
  color: #999;
  margin: 0 0 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  min-height: 36px;
}

.goods-price {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 8px;
}

.price-current {
  font-size: 20px;
  font-weight: bold;
  color: #f5576c;
}

.price-original {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
}

.goods-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #999;
}

.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}
</style>