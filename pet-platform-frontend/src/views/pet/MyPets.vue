<template>
  <div class="my-pets-container">
    <el-card class="header-card">
      <div class="header-content">
        <h2>我发布的宠物</h2>
        <el-button type="primary" @click="$router.push('/pet/publish')">
          <el-icon><Plus /></el-icon> 发布新宠物
        </el-button>
      </div>
    </el-card>

    <el-empty v-if="petList.length === 0 && !loading" description="还没有发布过宠物">
      <el-button type="primary" @click="$router.push('/pet/publish')">立即发布</el-button>
    </el-empty>

    <div class="pet-list" v-loading="loading">
      <el-card v-for="pet in petList" :key="pet.id" class="pet-item">
        <div class="pet-content">
          <el-image
            :src="pet.imageUrls && pet.imageUrls.length > 0 ? pet.imageUrls[0] : defaultImage"
            fit="cover"
            class="pet-image"
          >
            <template #error>
              <div class="image-placeholder">
                <el-icon><Picture /></el-icon>
              </div>
            </template>
          </el-image>

          <div class="pet-info">
            <div class="pet-header">
              <h3 class="pet-name">{{ pet.petName || '未命名' }}</h3>
              <el-tag :type="getStatusTagType(pet.status)" size="large">{{ pet.statusName }}</el-tag>
            </div>

            <div class="pet-meta">
              <el-tag :type="getPetTypeTag(pet.petType)" size="small">{{ pet.petTypeName }}</el-tag>
              <span v-if="pet.petBreed">{{ pet.petBreed }}</span>
              <span>{{ formatDate(pet.createTime) }}</span>
            </div>

            <div class="pet-stats">
              <span><el-icon><View /></el-icon> {{ pet.viewCount || 0 }}</span>
              <span><el-icon><Pointer /></el-icon> {{ pet.likeCount || 0 }}</span>
              <span><el-icon><ChatDotRound /></el-icon> {{ pet.commentCount || 0 }}</span>
            </div>

            <div class="pet-description">{{ pet.description }}</div>
          </div>

          <div class="pet-actions">
            <el-button text @click="viewDetail(pet.id)">
              <el-icon><View /></el-icon> 查看
            </el-button>
            <el-button text @click="editPet(pet)" :disabled="pet.status === 2">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button text @click="handleOffline(pet)" :disabled="pet.status === 3" v-if="pet.status !== 2">
              <el-icon><VideoPause /></el-icon> 下架
            </el-button>
            <el-button text type="danger" @click="handleDelete(pet.id)">
              <el-icon><Delete /></el-icon> 删除
            </el-button>
          </div>
        </div>
      </el-card>
    </div>

    <el-pagination
      v-if="total > 0"
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[10, 20, 30, 50]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="loadMyPets"
      @current-change="loadMyPets"
      class="pagination"
    />

    <!-- 编辑对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑宠物信息" width="700px">
      <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="100px">
        <el-form-item label="宠物名称" prop="petName">
          <el-input v-model="editForm.petName" placeholder="请输入宠物名称" />
        </el-form-item>

        <el-form-item label="宠物类型" prop="petType">
          <el-radio-group v-model="editForm.petType">
            <el-radio :label="1">狗狗</el-radio>
            <el-radio :label="2">猫咪</el-radio>
            <el-radio :label="3">其他</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="品种">
          <el-input v-model="editForm.petBreed" placeholder="请输入品种" />
        </el-form-item>

        <el-form-item label="性别">
          <el-radio-group v-model="editForm.petGender">
            <el-radio :label="0">未知</el-radio>
            <el-radio :label="1">公</el-radio>
            <el-radio :label="2">母</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="年龄">
          <el-input v-model="editForm.petAge" placeholder="如：3个月、2岁" />
        </el-form-item>

        <el-form-item label="毛色">
          <el-input v-model="editForm.petColor" placeholder="如：黄色、黑白相间" />
        </el-form-item>

        <el-form-item label="体重(kg)">
          <el-input-number v-model="editForm.petWeight" :min="0" :max="500" :precision="2" :step="0.1" />
        </el-form-item>

        <el-form-item label="健康状况">
          <el-input v-model="editForm.healthStatus" type="textarea" :rows="2" placeholder="请描述健康状况" />
        </el-form-item>

        <el-form-item label="疫苗接种">
          <el-radio-group v-model="editForm.vaccinationStatus">
            <el-radio :label="0">未接种</el-radio>
            <el-radio :label="1">已接种</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="绝育状态">
          <el-radio-group v-model="editForm.sterilizationStatus">
            <el-radio :label="0">未绝育</el-radio>
            <el-radio :label="1">已绝育</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="详细描述" prop="description">
          <el-input v-model="editForm.description" type="textarea" :rows="4" placeholder="请详细描述宠物情况" maxlength="1000" show-word-limit />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit" :loading="editLoading">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Picture, View, Pointer, ChatDotRound, Edit, Delete, VideoPause } from '@element-plus/icons-vue'
import { getMyPets, updatePet, deletePet, offlinePet } from '@/api/pet'

const router = useRouter()

const loading = ref(false)
const petList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const defaultImage = 'https://via.placeholder.com/200x150?text=No+Image'

const editDialogVisible = ref(false)
const editFormRef = ref()
const editLoading = ref(false)
const currentEditId = ref(null)

const editForm = reactive({
  petName: '',
  petType: null,
  petBreed: '',
  petGender: 0,
  petAge: '',
  petColor: '',
  petWeight: null,
  healthStatus: '',
  vaccinationStatus: 0,
  sterilizationStatus: 0,
  description: ''
})

const editRules = {
  petName: [{ required: true, message: '请输入宠物名称', trigger: 'blur' }],
  petType: [{ required: true, message: '请选择宠物类型', trigger: 'change' }],
  description: [{ required: true, message: '请输入详细描述', trigger: 'blur' }]
}

onMounted(() => {
  loadMyPets()
})

const loadMyPets = async () => {
  loading.value = true
  try {
    const res = await getMyPets({
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    petList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('加载失败:', error)
  } finally {
    loading.value = false
  }
}

const viewDetail = (id) => {
  router.push(`/pet/${id}`)
}

const editPet = (pet) => {
  currentEditId.value = pet.id
  Object.assign(editForm, {
    petName: pet.petName,
    petType: pet.petType,
    petBreed: pet.petBreed,
    petGender: pet.petGender,
    petAge: pet.petAge,
    petColor: pet.petColor,
    petWeight: pet.petWeight,
    healthStatus: pet.healthStatus,
    vaccinationStatus: pet.vaccinationStatus,
    sterilizationStatus: pet.sterilizationStatus,
    description: pet.description
  })
  editDialogVisible.value = true
}

const submitEdit = async () => {
  if (!editFormRef.value) return

  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      editLoading.value = true
      try {
        await updatePet(currentEditId.value, editForm)
        ElMessage.success('更新成功')
        editDialogVisible.value = false
        loadMyPets()
      } catch (error) {
        console.error('更新失败:', error)
      } finally {
        editLoading.value = false
      }
    }
  })
}

const handleOffline = async (pet) => {
  try {
    await ElMessageBox.confirm(
      pet.status === 3 ? '确定要重新上架吗？' : '确定要下架吗？下架后宠物将不在列表中显示',
      '提示',
      { type: 'warning' }
    )
    await offlinePet(pet.id)
    ElMessage.success(pet.status === 3 ? '上架成功' : '下架成功')
    loadMyPets()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
    }
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这只宠物吗？删除后无法恢复', '警告', {
      type: 'warning',
      confirmButtonText: '确定删除',
      confirmButtonClass: 'el-button--danger'
    })
    await deletePet(id)
    ElMessage.success('删除成功')
    loadMyPets()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

const getStatusTagType = (status) => {
  const map = {
    0: 'success',
    1: 'warning',
    2: 'info',
    3: 'danger'
  }
  return map[status] || 'info'
}

const getPetTypeTag = (type) => {
  const map = {
    1: 'warning',
    2: 'success',
    3: 'info'
  }
  return map[type] || 'info'
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}
</script>

<style scoped>
.my-pets-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-content h2 {
  margin: 0;
  color: #303133;
}

.pet-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
  min-height: 200px;
}

.pet-item {
  cursor: pointer;
  transition: all 0.3s;
}

.pet-item:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.pet-content {
  display: flex;
  gap: 20px;
}

.pet-image {
  width: 200px;
  height: 150px;
  border-radius: 8px;
  flex-shrink: 0;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  color: #909399;
  font-size: 48px;
}

.pet-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.pet-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.pet-name {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.pet-meta {
  display: flex;
  gap: 15px;
  margin-bottom: 10px;
  color: #606266;
  font-size: 14px;
}

.pet-stats {
  display: flex;
  gap: 20px;
  color: #909399;
  font-size: 14px;
  margin-bottom: 10px;
}

.pet-stats span {
  display: flex;
  align-items: center;
  gap: 5px;
}

.pet-description {
  color: #606266;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.pet-actions {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 10px;
  min-width: 80px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .pet-content {
    flex-direction: column;
  }

  .pet-image {
    width: 100%;
    height: 200px;
  }

  .pet-actions {
    flex-direction: row;
    justify-content: flex-start;
    flex-wrap: wrap;
  }
}
</style>