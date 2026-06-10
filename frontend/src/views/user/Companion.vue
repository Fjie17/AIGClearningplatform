<template>
  <div class="companion-container">
    <div class="grid-2">
      <div class="card">
        <div class="card-header">
          <h3>📊 今日学习总结</h3>
        </div>
        <div class="summary-content">
          <div class="summary-item">
            <div class="summary-label">📅 学习时长</div>
            <div class="summary-value">125 分钟</div>
          </div>
          <div class="summary-item">
            <div class="summary-label">✅ 完成任务</div>
            <div class="summary-value">3/4 项</div>
          </div>
          <div class="summary-item">
            <div class="summary-label">📈 考点掌握率</div>
            <div class="summary-value">+5%</div>
          </div>
          <div class="progress-section">
            <div class="progress-label">今日目标进度</div>
            <el-progress :percentage="75" />
          </div>
        </div>
      </div>
      
      <div class="card">
        <div class="card-header">
          <h3>🎯 AI学习纠偏</h3>
        </div>
        <div class="feedback-content">
          <p>你今天在无穷小比阶上错了2次，概念不够扎实。明天建议先回顾等价无穷小替换规则的思维导图。加油！</p>
          <div class="suggestion-tags">
            <el-tag type="warning">概念巩固</el-tag>
            <el-tag type="info">复习计划</el-tag>
          </div>
        </div>
      </div>
      
      <div class="card">
        <div class="card-header">
          <h3>📋 明日学习计划</h3>
        </div>
        <div class="plan-content">
          <div class="plan-item">
            <el-icon><VideoPlay /></el-icon>
            <span>📺 导数概念入门 (30min)</span>
          </div>
          <div class="plan-item">
            <el-icon><Edit /></el-icon>
            <span>✏️ 导数定义选择题 (5道)</span>
          </div>
          <div class="plan-item">
            <el-icon><Memo /></el-icon>
            <span>🧠 AI归纳：导数与极限的关系</span>
          </div>
          <el-button type="primary" style="margin-top: 15px;">开始规划</el-button>
        </div>
      </div>
      
      <div class="card">
        <div class="card-header">
          <h3>⏰ 学习提醒设置</h3>
        </div>
        <div class="reminder-settings">
          <div class="setting-item">
            <div class="setting-label">每日提醒时间</div>
            <el-select v-model="reminderTime" placeholder="选择提醒时间">
              <el-option label="19:30" value="19:30" />
              <el-option label="20:00" value="20:00" />
              <el-option label="21:00" value="21:00" />
              <el-option label="22:00" value="22:00" />
            </el-select>
          </div>
          <div class="setting-item">
            <div class="setting-label">通知方式</div>
            <div class="notification-options">
              <el-checkbox v-model="notificationOptions.site">站内通知</el-checkbox>
              <el-checkbox v-model="notificationOptions.email">邮件通知</el-checkbox>
              <el-checkbox v-model="notificationOptions.sms">短信提醒</el-checkbox>
            </div>
          </div>
          <el-button type="primary" style="margin-top: 15px;">保存设置</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { VideoPlay, Edit, Memo } from '@element-plus/icons-vue'
import { ref } from 'vue'

const reminderTime = ref('19:30')
const notificationOptions = ref({
  site: true,
  email: false,
  sms: false
})
</script>

<style scoped>
.companion-container {
  padding: 20px;
}

.grid-2 {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  border: 1px solid #ebeef5;
}

.card-header {
  margin-bottom: 16px;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.summary-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.summary-label {
  color: #606266;
}

.summary-value {
  font-weight: 500;
  color: #303133;
}

.progress-section {
  margin-top: 10px;
}

.progress-label {
  margin-bottom: 8px;
  color: #606266;
  font-size: 14px;
}

.feedback-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.suggestion-tags {
  display: flex;
  gap: 8px;
}

.plan-content {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.plan-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
}

.reminder-settings {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.setting-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.setting-label {
  font-weight: 500;
  color: #606266;
}

.notification-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

@media (max-width: 768px) {
  .grid-2 {
    grid-template-columns: 1fr;
  }
}
</style>