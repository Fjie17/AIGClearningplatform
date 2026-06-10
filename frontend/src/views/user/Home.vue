<template>
  <div class="user-home">
    <div class="welcome-container">
      <h1>欢迎来到 AIGC 个人学习中心</h1>
      <p>开启您的智能学习之旅</p>
      <div class="quick-links">
        <el-button type="primary" @click="goToProfile">开始学习</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goToProfile = () => {
  router.push('/user/profile')
}
</script>

<style scoped>
.user-home {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 80vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.welcome-container {
  text-align: center;
  padding: 40px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.welcome-container h1 {
  color: #303133;
  margin-bottom: 16px;
}

.welcome-container p {
  color: #606266;
  margin-bottom: 24px;
  font-size: 16px;
}

.quick-links {
  display: flex;
  justify-content: center;
  gap: 12px;
}
</style>