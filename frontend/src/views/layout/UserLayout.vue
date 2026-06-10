<template>
  <el-container class="layout-container">
    <el-aside width="200px">
      <el-menu
        router
        :default-active="$route.path"
        class="el-menu-vertical"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <div class="logo">AIGC 学习平台</div>
        <el-menu-item index="/user/profile">
          <el-icon><User /></el-icon>
          <span>用户画像</span>
        </el-menu-item>
        <el-menu-item index="/user/aiteacher">
          <el-icon><ChatLineRound /></el-icon>
          <span>AI教师</span>
        </el-menu-item>
        <el-menu-item index="/user/learningpath">
          <el-icon><Position /></el-icon>
          <span>学习路径</span>
        </el-menu-item>
        <el-menu-item index="/user/companion">
          <el-icon><ChatDotRound /></el-icon>
          <span>AI陪伴</span>
        </el-menu-item>
        <el-menu-item index="/user/analytics">
          <el-icon><DataAnalysis /></el-icon>
          <span>学习分析</span>
        </el-menu-item>
        <el-menu-item index="/user/information">
          <el-icon><User /></el-icon>
          <span>个人信息</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="layout-header">
        <div class="header-left">个人学习中心</div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="el-dropdown-link">
              {{ userInfo.username }} <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                <el-dropdown-item command="information">修改信息</el-dropdown-item>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, ChatLineRound, Position, ChatDotRound, DataAnalysis, ArrowDown } from '@element-plus/icons-vue'

const router = useRouter()
const userInfo = ref({ username: '用户' })

onMounted(() => {
  const storedUserInfo = localStorage.getItem('userInfo')
  if (storedUserInfo) {
    try {
      const parsed = JSON.parse(storedUserInfo)
      userInfo.value = parsed
    } catch (e) {
      console.error('解析用户信息失败', e)
    }
  }
})

const handleCommand = (command) => {
  if (command === 'logout') {
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    ElMessage.success('已退出登录')
    router.push('/login')
  } else if (command === 'profile') {
    router.push('/user/profile')
  } else if (command === 'information') {
    router.push('/user/information')
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}
.el-menu-vertical {
  height: 100%;
  border-right: none;
}
.logo {
  height: 60px;
  line-height: 60px;
  color: #fff;
  text-align: center;
  font-weight: bold;
  background: #2b2f3a;
}
.layout-header {
  background-color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e6e6e6;
}
.header-left {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}
.el-dropdown-link {
  cursor: pointer;
  color: #409EFF;
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>