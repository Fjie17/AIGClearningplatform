<template>
  <div class="ai-match-container">
    <div class="page-header">
      <h2>AI资源重组匹配</h2>
      <el-button type="primary" @click="openUploadModal">
        <el-icon><Upload /></el-icon>
        数据导入
      </el-button>
    </div>

    <div v-if="uploadedFiles.length > 0" class="uploaded-files">
      <h3>已上传文件</h3>
      <el-card class="files-card">
        <el-table :data="uploadedFiles" border>
          <el-table-column prop="name" label="文件名" />
          <el-table-column prop="size" label="大小" />
          <el-table-column prop="time" label="上传时间" />
          <el-table-column label="操作">
            <template #default="scope">
              <el-button size="small" @click="removeFile(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <div v-if="uploadedFiles.length > 0" class="action-buttons">
      <el-button type="success" :loading="cleaning" @click="startClean">
        <el-icon><Wrench /></el-icon>
        自动数据清洗
      </el-button>
      <el-button type="danger" :loading="matching" :disabled="!cleanCompleted" @click="startMatch">
        <el-icon><Monitor /></el-icon>
        AI智能匹配
      </el-button>
    </div>

    <div class="process-areas">
      <div class="area data-clean-area">
        <div class="area-header">
          <el-icon class="area-icon"><Database /></el-icon>
          <h3>数据清洗区域</h3>
        </div>
        <div class="area-content">
          <div v-if="cleaning" class="progress-section">
            <div class="progress-text">{{ cleanStatus }}</div>
            <el-progress :percentage="cleanProgress" :striped="true" :indeterminate="true" />
          </div>
          <div v-else-if="cleanCompleted" class="result-section">
            <div class="result-header">
              清洗完成
              <span class="result-stats">成功: {{ cleanResult.successCount }} | 失败: {{ cleanResult.failedCount }}</span>
            </div>
            <div v-if="cleanResult.errors && cleanResult.errors.length > 0" class="error-list">
              <h4>错误信息</h4>
              <el-scrollbar max-height="200px">
                <ul>
                  <li v-for="(error, index) in cleanResult.errors.slice(0, 10)" :key="index">{{ error }}</li>
                  <li v-if="cleanResult.errors.length > 10">... 还有 {{ cleanResult.errors.length - 10 }} 条错误</li>
                </ul>
              </el-scrollbar>
            </div>
          </div>
          <div v-else class="placeholder">
            <el-icon class="placeholder-icon"><FileText /></el-icon>
            <p>请先上传文件并点击数据清洗</p>
          </div>
        </div>
      </div>

      <div class="arrow-container">
        <div class="arrow">
          <el-icon><ArrowRight /></el-icon>
        </div>
        <div class="arrow-text">处理流程</div>
      </div>

      <div class="area ai-match-area">
        <div class="area-header">
          <el-icon class="area-icon brain-icon"><Cpu /></el-icon>
          <h3>AI匹配区域</h3>
        </div>
        <div class="area-content">
          <div v-if="matching" class="progress-section">
            <div class="progress-text ai-status">{{ aiStatus }}</div>
            <div class="ai-progress">
              <div class="progress-ring">
                <svg width="120" height="120">
                  <circle class="progress-bg" cx="60" cy="60" r="50" />
                  <circle class="progress-bar" cx="60" cy="60" r="50" :style="{ strokeDasharray: progressRingValue }" />
                </svg>
                <div class="progress-center">{{ aiProgress.toFixed(2) }}%</div>
              </div>
            </div>
            <div class="thinking-dots">
              <span class="dot"></span>
              <span class="dot"></span>
              <span class="dot"></span>
            </div>
          </div>
          <div v-else-if="matchCompleted" class="result-section">
            <div class="result-header match-complete">
              <el-icon><CircleCheck /></el-icon>
              AI匹配已完成
            </div>
            <el-card class="match-result-card">
              <div class="match-summary">
                <div class="summary-item">
                  <span class="summary-label">总行数</span>
                  <span class="summary-value">{{ aiResult.totalRows }}</span>
                </div>
                <div class="summary-item">
                  <span class="summary-label">有效行数</span>
                  <span class="summary-value success">{{ aiResult.validRows }}</span>
                </div>
                <div class="summary-item">
                  <span class="summary-label">无效行数</span>
                  <span class="summary-value error">{{ aiResult.invalidRows }}</span>
                </div>
              </div>
            </el-card>
            <div v-if="aiResult.previewData && aiResult.previewData.length > 0" class="preview-section">
              <h4>预览数据</h4>
              <el-table :data="aiResult.previewData" border :max-height="300">
                <el-table-column prop="rowIndex" label="序号" width="60" />
                <el-table-column prop="title" label="标题" show-overflow-tooltip />
                <el-table-column prop="platform" label="平台" width="80" />
                <el-table-column prop="views" label="浏览量" width="100" />
                <el-table-column prop="resourceType" label="类型" width="80" />
              </el-table>
            </div>
          </div>
          <div v-else class="placeholder">
            <el-icon class="placeholder-icon"><Sparkles /></el-icon>
            <p>数据清洗完成后点击AI智能匹配</p>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="uploadModalVisible" title="数据导入" width="500px">
      <div class="upload-form">
        <el-form :model="uploadForm" label-width="100px">
          <el-form-item label="科目ID">
            <el-input v-model="uploadForm.subjectId" placeholder="请输入科目ID" />
          </el-form-item>
          <el-form-item label="平台">
            <el-select v-model="uploadForm.platform" placeholder="请选择平台">
              <el-option label="B站" value="bilibili" />
              <el-option label="抖音" value="douyin" />
              <el-option label="网易云课堂" value="netease" />
              <el-option label="其他" value="other" />
            </el-select>
          </el-form-item>
          <el-form-item label="资源类型">
            <el-select v-model="uploadForm.resourceType" placeholder="请选择资源类型">
              <el-option label="视频" value="video" />
              <el-option label="音频" value="audio" />
              <el-option label="文档" value="document" />
              <el-option label="图片" value="image" />
              <el-option label="其他" value="other" />
            </el-select>
          </el-form-item>
        </el-form>
        <div class="upload-area" @click="triggerUpload" @drop.prevent="handleDrop">
          <el-icon class="upload-icon"><Upload /></el-icon>
          <p>点击或拖拽文件到此处上传</p>
          <p class="upload-tip">支持 Excel、CSV、JSON 格式</p>
          <input ref="fileInput" type="file" multiple class="file-input" @change="handleFileSelect" />
        </div>
        <div v-if="uploading" class="upload-progress">
          <el-progress :percentage="uploadProgress" />
        </div>
      </div>
      <template #footer>
        <el-button @click="uploadModalVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmUpload">确认上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const uploadModalVisible = ref(false)
const fileInput = ref(null)
const uploading = ref(false)
const uploadProgress = ref(0)

const uploadForm = reactive({
  subjectId: '',
  platform: '',
  resourceType: ''
})

const uploadedFiles = ref([])

const cleaning = ref(false)
const cleanProgress = ref(0)
const cleanStatus = ref('')
const cleanCompleted = ref(false)
const cleanResult = reactive({
  errors: [],
  failedCount: 0,
  successCount: 0,
  cacheKey: ''
})

const matching = ref(false)
const aiProgress = ref(0)
const aiStatus = ref('')
const matchCompleted = ref(false)
const aiResult = reactive({
  cacheKey: '',
  invalidReasons: [],
  invalidRows: 0,
  previewData: [],
  totalRows: 0,
  validRows: 0
})

const aiStatusList = ['AI智能匹配中...', 'AI思考中...', '正在分析数据...', '匹配资源中...', '生成结果中...']
let aiStatusIndex = 0
let aiStatusTimer = null

const progressRingValue = computed(() => {
  const circumference = 2 * Math.PI * 50
  const offset = circumference - (aiProgress.value / 100) * circumference
  return `${circumference} ${offset}`
})

const openUploadModal = () => {
  uploadModalVisible.value = true
}

const triggerUpload = () => {
  fileInput.value?.click()
}

const handleDrop = (e) => {
  const files = Array.from(e.dataTransfer.files)
  handleFiles(files)
}

const handleFileSelect = (e) => {
  const files = Array.from(e.target.files)
  handleFiles(files)
}

const handleFiles = (files) => {
  files.forEach(file => {
    const fileInfo = {
      id: Date.now() + Math.random(),
      name: file.name,
      size: formatSize(file.size),
      time: new Date().toLocaleString(),
      file: file
    }
    uploadedFiles.value.push(fileInfo)
  })
}

const formatSize = (bytes) => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
}

const removeFile = (row) => {
  const index = uploadedFiles.value.findIndex(f => f.id === row.id)
  if (index > -1) {
    uploadedFiles.value.splice(index, 1)
    ElMessage.success('文件已删除')
  }
}

const confirmUpload = () => {
  if (uploadedFiles.value.length === 0) {
    ElMessage.warning('请先选择文件')
    return
  }
  uploading.value = true
  uploadProgress.value = 0
  const interval = setInterval(() => {
    uploadProgress.value += 10
    if (uploadProgress.value >= 100) {
      clearInterval(interval)
      uploading.value = false
      uploadModalVisible.value = false
      ElMessage.success('文件上传成功')
    }
  }, 200)
}

const startClean = () => {
  if (uploadedFiles.value.length === 0) {
    ElMessage.warning('请先上传文件')
    return
  }
  
  // 验证必须的参数
  if (!uploadForm.subjectId) {
    ElMessage.warning('请先填写科目ID')
    return
  }
  
  cleaning.value = true
  cleanCompleted.value = false
  cleanProgress.value = 0
  cleanStatus.value = '数据自动清洗中...'

  const interval = setInterval(() => {
    cleanProgress.value = parseFloat((cleanProgress.value + Math.random() * 15).toFixed(2));
    if (cleanProgress.value >= 100) {
      cleanProgress.value = 100
      clearInterval(interval)
    }
  }, 300)

  const formData = new FormData()
  uploadedFiles.value.forEach(item => {
    formData.append('file', item.file)
  })
  if (uploadForm.subjectId) {
    formData.append('subjectId', uploadForm.subjectId)
  }
  if (uploadForm.platform) {
    formData.append('platform', uploadForm.platform)
  }
  if (uploadForm.resourceType) {
    formData.append('resourceType', uploadForm.resourceType)
  }

  request({
    url: '/admin/resources/clean',
    method: 'POST',
    data: formData
  }).then(data => {
    clearInterval(interval)
    cleanProgress.value = 100
    setTimeout(() => {
      cleaning.value = false
      cleanCompleted.value = true
      if (data.code === 200) {
        cleanResult.errors = data.data.errors || []
        cleanResult.failedCount = data.data.failedCount || 0
        cleanResult.successCount = data.data.successCount || 0
        cleanResult.cacheKey = data.data.cacheKey || ''
        ElMessage.success('数据清洗完成')
      } else {
        ElMessage.error(data.message || '数据清洗失败')
      }
    }, 500)
  }).catch(err => {
    console.error('Clean error:', err)
    clearInterval(interval)
    cleaning.value = false
    ElMessage.error('数据清洗失败：' + (err.response?.data?.message || err.message))
  })
}

const startMatch = () => {
  if (!cleanCompleted.value) {
    ElMessage.warning('请先完成数据清洗')
    return
  }
  if (!cleanResult.cacheKey) {
    ElMessage.warning('缓存key不存在，请重新清洗')
    return
  }
  
  // 验证subjectId是否有效
  if (!uploadForm.subjectId) {
    ElMessage.warning('请先填写科目ID')
    return
  }
  
  matching.value = true
  matchCompleted.value = false
  aiProgress.value = 0

  aiStatusIndex = 0
  aiStatus.value = aiStatusList[0]
  aiStatusTimer = setInterval(() => {
    aiStatusIndex = (aiStatusIndex + 1) % aiStatusList.length
    aiStatus.value = aiStatusList[aiStatusIndex]
  }, 2000)

  const interval = setInterval(() => {
    aiProgress.value = parseFloat((aiProgress.value + Math.random() * 10).toFixed(2));
    if (aiProgress.value >= 100) {
      aiProgress.value = 100
      clearInterval(interval)
    }
  }, 400)

  // 构建 x-www-form-urlencoded 格式的数据
  const requestData = new URLSearchParams({
    subjectId: uploadForm.subjectId,
    cacheKey: cleanResult.cacheKey
  }).toString();
  
  request({
    url: '/admin/resources/ai-process',
    method: 'POST',
    data: requestData,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    },
    timeout: 60000 // 设置60秒超时时间
  }).then(data => {
    clearInterval(interval)
    clearInterval(aiStatusTimer)
    aiProgress.value = 100
    setTimeout(() => {
      matching.value = false
      matchCompleted.value = true
      if (data.code === 200) {
        aiResult.cacheKey = data.data.cacheKey || ''
        aiResult.invalidReasons = data.data.invalidReasons || []
        aiResult.invalidRows = data.data.invalidRows || 0
        aiResult.previewData = data.data.previewData || []
        aiResult.totalRows = data.data.totalRows || 0
        aiResult.validRows = data.data.validRows || 0
        ElMessage.success('AI匹配已完成')
      } else {
        ElMessage.error(data.message || 'AI匹配失败')
      }
    }, 800)
  }).catch(err => {
    console.error('AI Process error:', err)
    clearInterval(interval)
    clearInterval(aiStatusTimer)
    matching.value = false
    ElMessage.error('AI匹配失败：' + (err.response?.data?.message || err.message))
  })
}
</script>

<style scoped>
.ai-match-container {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.uploaded-files {
  margin-bottom: 20px;
}

.uploaded-files h3 {
  margin-bottom: 12px;
  font-size: 16px;
  color: #606266;
}

.files-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.action-buttons {
  display: flex;
  gap: 12px;
  margin-bottom: 32px;
}

.process-areas {
  display: flex;
  align-items: stretch;
  gap: 24px;
}

.area {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.area-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.area-icon {
  font-size: 20px;
}

.brain-icon {
  color: #00d4ff;
}

.area-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.area-content {
  padding: 24px;
  min-height: 300px;
}

.progress-section {
  text-align: center;
}

.progress-text {
  margin-bottom: 16px;
  font-size: 16px;
  color: #606266;
}

.ai-status {
  color: #409EFF;
  font-weight: 500;
}

.placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #909399;
}

.placeholder-icon {
  font-size: 48px;
  margin-bottom: 12px;
  opacity: 0.5;
}

.placeholder p {
  margin: 0;
}

.arrow-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 0 20px;
}

.arrow {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 20px;
}

.arrow-text {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

.result-section {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.result-header {
  margin-bottom: 16px;
  font-size: 18px;
  font-weight: 600;
  color: #67c23a;
}

.result-stats {
  margin-left: 16px;
  font-size: 14px;
  font-weight: normal;
  color: #606266;
}

.match-complete {
  display: flex;
  align-items: center;
  gap: 8px;
}

.match-result-card {
  background: linear-gradient(135deg, #e8f5e9 0%, #f3e5f5 100%);
  margin-bottom: 16px;
}

.match-summary {
  display: flex;
  justify-content: space-around;
}

.summary-item {
  text-align: center;
}

.summary-label {
  display: block;
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.summary-value {
  font-size: 28px;
  font-weight: bold;
  color: #67c23a;
}

.summary-value.error {
  color: #f56c6c;
}

.summary-value.success {
  color: #67c23a;
}

.error-list {
  background: #fef0f0;
  border-radius: 8px;
  padding: 16px;
  margin-top: 16px;
}

.error-list h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #f56c6c;
}

.error-list ul {
  margin: 0;
  padding-left: 20px;
}

.error-list li {
  font-size: 13px;
  color: #606266;
  line-height: 1.8;
}

.preview-section {
  margin-top: 16px;
}

.preview-section h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #606266;
}

.upload-form {
  margin-bottom: 16px;
}

.upload-area {
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  padding: 40px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  margin-top: 16px;
}

.upload-area:hover {
  border-color: #409EFF;
  background: #f0f5ff;
}

.upload-icon {
  font-size: 48px;
  color: #409EFF;
  margin-bottom: 12px;
}

.upload-area p {
  margin: 0 0 8px 0;
  color: #606266;
}

.upload-tip {
  font-size: 12px;
  color: #909399 !important;
}

.file-input {
  display: none;
}

.upload-progress {
  margin-top: 16px;
}

.ai-progress {
  display: flex;
  justify-content: center;
  margin: 24px 0;
}

.progress-ring {
  position: relative;
}

.progress-bg {
  fill: none;
  stroke: #e4e7ed;
  stroke-width: 8;
}

.progress-bar {
  fill: none;
  stroke: url(#progressGradient);
  stroke-width: 8;
  stroke-linecap: round;
  transition: stroke-dasharray 0.3s ease;
}

.progress-center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 20px;
  font-weight: bold;
  color: #409EFF;
}

.thinking-dots {
  display: flex;
  justify-content: center;
  gap: 6px;
  margin-top: 16px;
}

.dot {
  width: 8px;
  height: 8px;
  background: #409EFF;
  border-radius: 50%;
  animation: dotPulse 1.4s infinite ease-in-out;
}

.dot:nth-child(1) { animation-delay: 0s; }
.dot:nth-child(2) { animation-delay: 0.2s; }
.dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes dotPulse {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.5; }
  40% { transform: scale(1); opacity: 1; }
}
</style>