<template>
  <div class="user-profile-container">
    <div class="card">
      <!-- 头部区域：科目选择和操作按钮 -->
      <div class="card-header">
        <div class="header-left">
          <h3>🧠 我的学习画像</h3>
          <div class="subject-select">
            <span class="select-label">选择科目：</span>
            <el-select 
              v-model="selectedSubject" 
              placeholder="请选择科目"
              class="subject-dropdown"
              @change="handleSubjectChange"
            >
              <el-option 
                v-for="subject in subjects" 
                :key="subject.id" 
                :label="subject.name" 
                :value="subject.id"
              />
            </el-select>
          </div>
        </div>
        <div class="header-right">
          <el-button type="primary" @click="testModal">
            📝 画像测试
          </el-button>
          <el-button type="success" @click="updateProfile">
            🔄 更新画像
          </el-button>
        </div>
      </div>

      <!-- 画像内容区域 -->
      <div v-if="profileData" class="profile-content">
        <div class="profile-overview">
          <div class="overview-item">
            <div class="overview-label">当前水平</div>
            <el-progress :percentage="levelPercentage" :color="customColorMethod" />
            <div class="level-text">{{ levelText }}</div>
          </div>
          <div class="overview-item">
            <div class="overview-label">学习节奏</div>
            <div class="overview-value">{{ speedText }}</div>
          </div>
          <div class="overview-item">
            <div class="overview-label">学习偏好</div>
            <div class="overview-value">{{ profileData.preference }}</div>
          </div>
          <div class="overview-item">
            <div class="overview-label">自律程度</div>
            <div class="overview-value">{{ disciplineText }}</div>
          </div>
          <div class="overview-item">
            <div class="overview-label">学格类型</div>
            <div class="overview-value lqai-code">{{ profileData.lqai }} ({{ profileData.lqaiCode }})</div>
          </div>
        </div>
        <div class="radar-chart">
          <div class="chart-content">
            <div class="lqai-badge">
              <span class="badge-icon">🏆</span>
              <span class="badge-text">{{ profileData.lqai }}</span>
            </div>
            <div class="lqai-description">
              {{ profileData.profile }}
            </div>
          </div>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-else class="loading-state">
        <el-spinner type="dots" />
        <p>加载中...</p>
      </div>

      <!-- 诊断建议 -->
      <div v-if="profileData" class="diagnosis-result">
        💡 {{ diagnosisSuggestion }}
      </div>
    </div>
  </div>

  <!-- 画像测试对话框 -->
  <el-dialog 
    title="📝 学习画像测试" 
    :visible="assessmentVisible" 
    width="700px"
    :close-on-click-modal="false"
    @close="assessmentVisible = false"
    :modal="true"
    :append-to-body="true"
  >
    <div v-if="questions.length > 0" class="assessment-content">
      <el-form ref="assessmentFormRef" :model="assessmentForm" label-width="100%">
        <div 
          v-for="(question, index) in questions" 
          :key="question.id" 
          class="question-item"
        >
          <div class="question-header">
            <span class="question-number">第 {{ index + 1 }} 题</span>
            <span class="question-type">{{ question.questionType === 'single_choice' ? '单选题' : '问答题' }}</span>
          </div>
          <div class="question-text">{{ question.questionText }}</div>
          
          <!-- 单选题 -->
          <div v-if="question.questionType === 'single_choice'" class="options-list">
            <el-radio-group 
              v-model="assessmentForm[question.questionCode]" 
              class="radio-group"
            >
              <el-radio 
                v-for="option in question.options" 
                :key="option.code" 
                :label="option.code"
              >
                <span class="option-code">{{ option.code }}.</span>
                <span class="option-text">{{ option.text }}</span>
              </el-radio>
            </el-radio-group>
          </div>
          
          <!-- 问答题 -->
          <div v-else class="text-input-wrapper">
            <el-input 
              v-model="assessmentForm[question.questionCode]"
              type="textarea"
              :maxlength="20"
              placeholder="请输入您的回答（10-20字）"
              :rows="2"
            />
          </div>
        </div>
      </el-form>
    </div>

    <template #footer>
      <el-button @click="assessmentVisible = false">取消</el-button>
      <el-button 
        type="primary" 
        :loading="submitting" 
        @click="submitAssessment"
      >
        提交测试
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElDialog } from 'element-plus'
import request from '@/utils/request'

// 科目列表
const subjects = ref([])
const selectedSubject = ref(1)

// 画像数据
const profileData = ref(null)
const loading = ref(false)

// 测试相关
const assessmentVisible = ref(false)
const questions = ref([])
const submitting = ref(false)
const assessmentFormRef = ref(null)
const assessmentForm = reactive({})

// 获取用户ID
const getUserId = () => {
  const userInfoStr = localStorage.getItem('userInfo')
  if (userInfoStr) {
    try {
      const userInfo = JSON.parse(userInfoStr)
      return userInfo.userId
    } catch (e) {
      console.error('解析用户信息失败', e)
      return 1
    }
  }
  return 1
}

// 当前水平百分比
const levelPercentage = computed(() => {
  if (!profileData.value) return 0
  return (profileData.value.currentLevel / 5) * 100
})

// 水平文字描述
const levelText = computed(() => {
  if (!profileData.value) return ''
  const level = profileData.value.currentLevel
  if (level <= 1) return '入门'
  if (level <= 2) return '初级'
  if (level <= 3) return '中级'
  if (level <= 4) return '高级'
  return '精通'
})

// 学习节奏描述
const speedText = computed(() => {
  if (!profileData.value) return ''
  const speed = profileData.value.learningSpeed
  if (speed <= 1) return '极慢'
  if (speed <= 2) return '慢速'
  if (speed <= 3) return '中等'
  if (speed <= 4) return '快速'
  return '极速'
})

// 自律程度描述
const disciplineText = computed(() => {
  if (!profileData.value) return ''
  const discipline = profileData.value.selfDiscipline
  if (discipline <= 1) return '极差'
  if (discipline <= 2) return '较差'
  if (discipline <= 3) return '中等'
  if (discipline <= 4) return '较好'
  return '优秀'
})

// 诊断建议
const diagnosisSuggestion = computed(() => {
  if (!profileData.value) return ''
  const level = profileData.value.currentLevel
  const speed = profileData.value.learningSpeed
  const discipline = profileData.value.selfDiscipline
  const preference = profileData.value.preference
  
  let suggestions = []
  
  if (level <= 2) {
    suggestions.push('建议从基础知识点开始学习')
  }
  if (speed <= 2) {
    suggestions.push('学习节奏较慢，建议制定合理的学习计划')
  }
  if (speed >= 4) {
    suggestions.push('学习效率较高，可适当增加学习强度')
  }
  if (discipline <= 2) {
    suggestions.push('自律程度有待提高，建议使用打卡功能')
  }
  if (discipline >= 4) {
    suggestions.push('自律性很强，继续保持')
  }
  
  if (preference.includes('视频')) {
    suggestions.push('建议优先观看视频课程')
  }
  if (preference.includes('文本') || preference.includes('实践')) {
    suggestions.push('建议多做练习题巩固知识')
  }
  
  return suggestions.length > 0 ? suggestions.join('；') : '暂无特殊建议'
})

// 自定义进度条颜色
const customColorMethod = (percentage) => {
  if (percentage < 30) {
    return '#f56c6c'
  } else if (percentage < 70) {
    return '#e6a23c'
  } else {
    return '#67c23a'
  }
}

// 加载科目列表
const loadSubjects = async () => {
  try {
    const response = await request({
      url: '/admin/subjects/options',
      method: 'get'
    })
    if (response.code === 200) {
      subjects.value = response.data
      if (subjects.value.length > 0 && !selectedSubject.value) {
        selectedSubject.value = subjects.value[0].id
      }
    }
  } catch (error) {
    console.error('获取科目列表失败', error)
  }
}

// 加载画像信息
const loadProfile = async () => {
  loading.value = true
  try {
    const response = await request({
      url: '/profile/profile',
      method: 'get',
      params: {
        userId: getUserId(),
        subjectId: selectedSubject.value
      }
    })
    if (response.code === 200) {
      profileData.value = response.data
    } else {
      ElMessage.error(response.message || '获取画像信息失败')
    }
  } catch (error) {
    console.error('获取画像信息失败', error)
    ElMessage.error('获取画像信息失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 切换科目
const handleSubjectChange = () => {
  loadProfile()
}

// 打开测试对话框
const openAssessmentModal = async () => {
  console.log('点击了画像测试按钮')
  console.log('当前选中科目ID:', selectedSubject.value)
  
  try {
    const response = await request({
      url: '/profile/assessment-questions',
      method: 'get',
      params: {
        subjectId: selectedSubject.value
      }
    })
    console.log('测试题接口响应:', response)
    
    if (response && response.code === 200) {
      if (response.data && response.data.length > 0) {
        questions.value = response.data.map(q => ({
          ...q,
          options: q.optionsJson ? JSON.parse(q.optionsJson) : []
        }))
        console.log('解析后的测试题:', questions.value)
        
        // 初始化表单
        questions.value.forEach(q => {
          assessmentForm[q.questionCode] = ''
        })
        
        assessmentVisible.value = true
        console.log('设置 assessmentVisible 为 true')
      } else {
        ElMessage.warning('暂无测试题')
        console.log('测试题数据为空')
      }
    } else {
      ElMessage.error(response?.message || '获取测试题失败')
      console.log('接口返回失败:', response)
    }
  } catch (error) {
    console.error('获取测试题失败', error)
    ElMessage.error('获取测试题失败，请稍后重试')
  }
}

// 提交测试
const submitAssessment = async () => {
  // 验证必填项（Q1-Q4）
  const requiredQuestions = questions.value.filter(q => q.questionCode !== 'Q5')
  for (const q of requiredQuestions) {
    if (!assessmentForm[q.questionCode]) {
      ElMessage.warning(`请回答第${questions.value.indexOf(q) + 1}题`)
      return
    }
  }
  
  submitting.value = true
  try {
    const response = await request({
      url: '/profile/submit',
      method: 'post',
      data: {
        userId: getUserId(),
        subjectId: selectedSubject.value,
        ...assessmentForm
      }
    })
    if (response.code === 200) {
      ElMessage.success('测试提交成功')
      assessmentVisible.value = false
      loadProfile()
    } else {
      ElMessage.error(response.message || '提交测试失败')
    }
  } catch (error) {
    console.error('提交测试失败', error)
    ElMessage.error('提交测试失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

// 更新画像
const updateProfile = async () => {
  try {
    const response = await request({
      url: '/profile/determine',
      method: 'post',
      data: {
        userId: getUserId(),
        subjectId: selectedSubject.value
      }
    })
    if (response.code === 200) {
      ElMessage.success('画像更新成功')
      loadProfile()
    } else {
      ElMessage.error(response.message || '更新画像失败')
    }
  } catch (error) {
    console.error('更新画像失败', error)
    ElMessage.error('更新画像失败，请稍后重试')
  }
}

// 初始化
onMounted(() => {
  loadSubjects().then(() => {
    loadProfile()
  })
})
</script>

<style scoped>
.user-profile-container {
  padding: 20px;
}

.card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  border: 1px solid #ebeef5;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 15px;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.header-left h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.subject-select {
  display: flex;
  align-items: center;
  gap: 8px;
}

.select-label {
  font-size: 14px;
  color: #606266;
}

.subject-dropdown {
  width: 200px;
}

.header-right {
  display: flex;
  gap: 10px;
}

.profile-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.profile-overview {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.overview-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
}

.overview-label {
  font-weight: 500;
  color: #606266;
  font-size: 13px;
}

.overview-value {
  color: #303133;
  font-size: 14px;
}

.lqai-code {
  font-weight: 600;
  color: #667eea;
}

.level-text {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.radar-chart {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 280px;
  border: 1px dashed #dcdfe6;
  border-radius: 8px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
}

.chart-content {
  text-align: center;
  padding: 20px;
}

.lqai-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 15px;
}

.badge-icon {
  font-size: 32px;
}

.badge-text {
  font-size: 20px;
  font-weight: 600;
  color: #667eea;
}

.lqai-description {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  max-height: 120px;
  overflow-y: auto;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px;
  gap: 15px;
}

.loading-state p {
  color: #909399;
}

.diagnosis-result {
  margin-top: 20px;
  padding: 15px;
  background: #ecf5ff;
  border-radius: 8px;
  color: #409eff;
  border-left: 4px solid #409eff;
  font-size: 14px;
  line-height: 1.6;
}

/* 测试对话框样式 */
.assessment-content {
  max-height: 500px;
  overflow-y: auto;
  padding-right: 10px;
}

.question-item {
  margin-bottom: 25px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.question-item:last-child {
  border-bottom: none;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.question-number {
  font-weight: 600;
  color: #667eea;
}

.question-type {
  font-size: 12px;
  color: #909399;
  background: #f5f7fa;
  padding: 2px 8px;
  border-radius: 4px;
}

.question-text {
  font-size: 14px;
  color: #303133;
  margin-bottom: 15px;
  line-height: 1.5;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.radio-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

:deep(.el-radio) {
  margin-right: 0;
}

.option-code {
  font-weight: 500;
  margin-right: 8px;
}

.option-text {
  font-size: 13px;
  color: #606266;
}

.text-input-wrapper {
  margin-top: 5px;
}

:deep(.el-radio__input.is-checked+.el-radio__label) {
  color: #667eea;
  font-weight: 500;
}

:deep(.el-radio__input.is-checked .el-radio__inner) {
  border-color: #667eea;
  background: #667eea;
}

/* 响应式布局 */
@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .header-right {
    width: 100%;
    justify-content: flex-start;
  }
  
  .profile-content {
    grid-template-columns: 1fr;
  }
}
</style>