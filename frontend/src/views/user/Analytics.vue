<template>
  <div class="analytics-container">
    <div class="card">
      <div class="card-header">
        <h3>📈 学习数据分析</h3>
      </div>
      <div class="analytics-content">
        <div class="chart-grid">
          <div class="chart-item">
            <div class="chart-title">知识掌握雷达图</div>
            <div class="chart-placeholder">
              <p>知识掌握雷达图</p>
              <p>待开发...</p>
            </div>
          </div>
          <div class="chart-item">
            <div class="chart-title">学习趋势图</div>
            <div class="chart-placeholder">
              <p>学习趋势图</p>
              <p>待开发...</p>
            </div>
          </div>
        </div>
        
        <div class="completion-section">
          <div class="section-title">📚 专题完成度</div>
          <div class="progress-list">
            <div class="progress-item">
              <div class="progress-label">高等数学</div>
              <div class="progress-bar">
                <el-progress :percentage="80" />
                <div class="progress-details">极限100%, 导数60%, 积分70%</div>
              </div>
            </div>
            <div class="progress-item">
              <div class="progress-label">线性代数</div>
              <div class="progress-bar">
                <el-progress :percentage="55" />
              </div>
            </div>
            <div class="progress-item">
              <div class="progress-label">概率论</div>
              <div class="progress-bar">
                <el-progress :percentage="45" />
              </div>
            </div>
          </div>
        </div>
        
        <div class="insight-section">
          <div class="insight-title">💡 学习洞察</div>
          <div class="insight-content">
            强项：极限计算(90分)，弱项：无穷级数(32分)。建议优先攻克级数专题。
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.analytics-container {
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
  margin-bottom: 20px;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.analytics-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.chart-item {
  background: #fafafa;
  border-radius: 8px;
  padding: 16px;
  text-align: center;
}

.chart-title {
  font-weight: 500;
  margin-bottom: 10px;
  color: #606266;
}

.chart-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 200px;
  border: 1px dashed #dcdfe6;
  border-radius: 8px;
  color: #909399;
}

.completion-section {
  margin-top: 20px;
}

.section-title {
  font-weight: 600;
  margin-bottom: 15px;
  color: #303133;
}

.progress-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.progress-item {
  display: flex;
  align-items: center;
  gap: 20px;
}

.progress-label {
  min-width: 100px;
  font-weight: 500;
}

.progress-bar {
  flex: 1;
}

.progress-details {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.insight-section {
  margin-top: 20px;
  padding: 16px;
  background: #ecf5ff;
  border-radius: 8px;
  border-left: 4px solid #409eff;
}

.insight-title {
  font-weight: 600;
  margin-bottom: 8px;
  color: #409eff;
}

.insight-content {
  color: #606266;
}

@media (max-width: 768px) {
  .chart-grid {
    grid-template-columns: 1fr;
  }
  
  .progress-item {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>