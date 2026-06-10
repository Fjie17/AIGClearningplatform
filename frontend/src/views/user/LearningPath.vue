<template>
  <div class="learning-path-container">
    <div class="card">
      <div class="card-header">
        <h3>🗺️ 个性化学习路径 · 考研冲刺</h3>
        <span>整体进度 ▰▰▰▰▰▰▱▱▱▱ 40%</span>
      </div>
      <div class="path-timeline">
        <div 
          v-for="(item, index) in learningPathItems" 
          :key="index" 
          class="timeline-item"
          :class="item.status"
        >
          <div class="timeline-marker" :style="{ borderColor: getStatusColor(item.status) }">
            <div class="marker-content" :style="{ backgroundColor: getStatusColor(item.status) }">
              {{ getMarkerIcon(item.status) }}
            </div>
          </div>
          <div class="timeline-content">
            <div class="timeline-header">
              <span class="day">{{ item.day }}</span>
              <span class="date">{{ item.date }}</span>
              <el-tag :type="getStatusTagType(item.status)">
                {{ getStatusText(item.status) }}
              </el-tag>
            </div>
            <div class="task-list">
              <div v-for="(task, taskIndex) in item.tasks" :key="taskIndex" class="task-item">
                <el-icon><CircleCheck v-if="item.status === 'completed'" /><Clock v-else /></el-icon>
                {{ task }}
              </div>
            </div>
            <div class="timeline-actions" v-if="item.status === 'inprogress'">
              <el-button type="primary">去完成</el-button>
            </div>
          </div>
        </div>
      </div>
      <div class="path-controls">
        <el-button>↩️ 回退任务</el-button>
        <el-button>⏭️ 跳过</el-button>
        <el-button type="primary">✅ 完成任务</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { CircleCheck, Clock } from '@element-plus/icons-vue'

const learningPathItems = [
  { day: "Day 1", date: "4月7日 周一", tasks: ["观看B站：汤家凤-极限精讲", "完成基础概念选择题"], status: "completed" },
  { day: "Day 2", date: "4月8日 周二", tasks: ["观看B站：武忠祥每日一题", "完成极限计算变式题"], status: "inprogress" },
  { day: "Day 3", date: "4月9日 周三", tasks: ["导数定义学习 + 基础计算练习"], status: "locked" }
]

const getStatusColor = (status) => {
  switch(status) {
    case 'completed': return '#52c41a';
    case 'inprogress': return '#1677ff';
    case 'locked': return '#dcdfe6';
    default: return '#dcdfe6';
  }
}

const getMarkerIcon = (status) => {
  switch(status) {
    case 'completed': return '✓';
    case 'inprogress': return '•';
    case 'locked': return '🔒';
    default: return '•';
  }
}

const getStatusTagType = (status) => {
  switch(status) {
    case 'completed': return 'success';
    case 'inprogress': return 'warning';
    case 'locked': return 'info';
    default: return 'info';
  }
}

const getStatusText = (status) => {
  switch(status) {
    case 'completed': return '已完成';
    case 'inprogress': return '进行中';
    case 'locked': return '待解锁';
    default: return '';
  }
}
</script>

<style scoped>
.learning-path-container {
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

.path-timeline {
  position: relative;
  padding-left: 30px;
}

.timeline-item {
  position: relative;
  margin-bottom: 24px;
  padding-left: 20px;
}

.timeline-item:not(:last-child)::before {
  content: '';
  position: absolute;
  left: 0;
  top: 20px;
  bottom: -20px;
  width: 2px;
  background: #ebeef5;
}

.timeline-marker {
  position: absolute;
  left: -30px;
  top: 0;
  width: 20px;
  height: 20px;
  border: 2px solid #dcdfe6;
  border-radius: 50%;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
}

.marker-content {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 10px;
}

.timeline-content {
  background: #fafafa;
  border-radius: 8px;
  padding: 16px;
}

.timeline-header {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 8px;
  font-weight: 600;
}

.task-list {
  margin-bottom: 12px;
}

.task-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
  font-size: 14px;
}

.timeline-actions {
  display: flex;
  justify-content: flex-end;
}

.path-controls {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}

.completed {
  /* 已完成样式 */
}

.inprogress {
  /* 进行中样式 */
}

.locked {
  /* 锁定样式 */
}
</style>