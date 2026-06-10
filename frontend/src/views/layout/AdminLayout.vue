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
        <div class="logo">AIGC 管理系统</div>
        <el-menu-item index="/admin/subject">
          <el-icon><Menu /></el-icon>
          <span>科目管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/knowledge">
          <el-icon><Reading /></el-icon>
          <span>知识点管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/resource">
          <el-icon><Files /></el-icon>
          <span>资源管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/ai-match">
          <el-icon><Cpu /></el-icon>
          <span>AI资源重组匹配</span>
        </el-menu-item>
        <el-menu-item index="/admin/assessment-question">
          <el-icon><User /></el-icon>
          <span>画像测试题管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="layout-header">
        <div class="header-left">后台管理系统</div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="el-dropdown-link">
              管理员 <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
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
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Menu, Reading, Files, Cpu, User } from '@element-plus/icons-vue'

const router = useRouter()

const handleCommand = (command) => {
  if (command === 'logout') {
    localStorage.removeItem('token')
    ElMessage.success('已退出登录')
    router.push('/login')
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
</style>