<template>
  <div class="ai-teacher-container">
    <div class="card">
      <div class="card-header">
        <h3>🤖 AI教师 · 应试引导者</h3>
        <span class="badge">不直接给答案，引导思考</span>
      </div>
      <div class="ai-teacher-content">
        <div class="input-section">
          <el-input 
            v-model="questionInput" 
            :rows="4" 
            type="textarea" 
            placeholder="粘贴题目、输入知识点或B站链接..."
          />
          <div class="action-buttons">
            <el-button @click="analyzeQuestion">📎 归纳</el-button>
            <el-button @click="findKeyPoints">⚠️ 重难点</el-button>
            <el-button @click="generateSolution">💡 解题思路</el-button>
            <el-button @click="generateVariants">🔄 变式题</el-button>
            <el-button @click="thinkGuide">🧠 思维引导</el-button>
          </div>
        </div>
        <div class="result-section">
          <div class="result-card">
            <div class="result-title">【考点归纳】</div>
            <div>一元函数极限的四则运算法则与等价无穷小代换。</div>
            
            <div class="result-title" style="margin-top: 12px;">⚠️ 重点与易错</div>
            <div>等价无穷小代换：乘除可用，加减慎用！</div>
            
            <div class="result-title" style="margin-top: 12px;">📝 解题步骤</div>
            <div>1. 判断0/0型 2. 洛必达或泰勒展开 3. 得出结果 1/2</div>
            
            <div class="result-title" style="margin-top: 12px;">🔄 变式题目</div>
            <div>lim(x→0) (1-cosx)/(x·sinx) → 生成同类题</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const questionInput = ref('')

const analyzeQuestion = () => {
  // 模拟AI分析
  console.log('分析题目...')
}

const findKeyPoints = () => {
  // 模拟查找重难点
  console.log('查找重难点...')
}

const generateSolution = () => {
  // 模拟生成解题思路
  console.log('生成解题思路...')
}

const generateVariants = () => {
  // 模拟生成变式题
  console.log('生成变式题...')
}

const thinkGuide = () => {
  // 模拟思维引导
  console.log('思维引导...')
}
</script>

<style scoped>
.ai-teacher-container {
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
  align-items: center;
  margin-bottom: 20px;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.badge {
  font-size: 12px;
  background: #ecf5ff;
  padding: 4px 8px;
  border-radius: 20px;
}

.ai-teacher-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.input-section .el-textarea {
  margin-bottom: 12px;
}

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.result-section {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 16px;
}

.result-card {
  padding: 12px;
}

.result-title {
  font-weight: 600;
  color: #303133;
}
</style>