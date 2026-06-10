<template>
  <div class="login-container">
    <!-- 背景装饰 -->
    <div class="background-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>

    <!-- 主卡片 -->
    <el-card class="login-card" :body-style="{ padding: '0' }">
      <div class="card-wrapper">
        <!-- 左侧品牌区域 -->
        <div class="brand-section">
          <div class="brand-content">
            <div class="brand-icon">
              <svg viewBox="0 0 24 24" width="48" height="48" fill="currentColor">
                <path d="M12 2L2 7v10l10 5 10-5V7l-10-5z"/>
              </svg>
            </div>
            <h1 class="brand-title">AIGC学习平台</h1>
            <p class="brand-slogan">开启AI创作之旅</p>
            <div class="feature-list">
              <div class="feature-item">
                <el-icon><MagicStick /></el-icon>
                <span>智能生成</span>
              </div>
              <div class="feature-item">
                <el-icon><Timer /></el-icon>
                <span>高效学习</span>
              </div>
              <div class="feature-item">
                <el-icon><UserFilled /></el-icon>
                <span>专业指导</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧表单区域 -->
        <div class="form-section">
          <div class="form-header">
            <h2 class="form-title">
              {{ mode === 'login' ? '欢迎回来' : mode === 'register' ? '创建账号' : '重置密码' }}
            </h2>
            <p class="form-subtitle">
              {{ mode === 'login' ? '登录您的账号继续学习' : mode === 'register' ? '填写信息完成注册' : '通过邮箱重置密码' }}
            </p>
          </div>

          <el-form 
            ref="formRef"
            :model="form" 
            :rules="rules"
            class="login-form"
            @keyup.enter="handleSubmit"
          >
            <!-- 用户名 -->
            <el-form-item prop="username" v-if="mode === 'login' || mode === 'register'">
              <el-input 
                v-model="form.username" 
                placeholder="用户名"
                size="large"
                :prefix-icon="User"
                clearable
              />
            </el-form-item>

            <!-- 密码 -->
            <el-form-item prop="password" v-if="mode === 'login' || mode === 'register' || mode === 'forgot'">
              <el-input 
                v-model="form.password" 
                type="password"
                placeholder="密码"
                size="large"
                :prefix-icon="Lock"
                show-password
              />
            </el-form-item>

            <!-- 确认密码 -->
            <el-form-item prop="confirmPassword" v-if="mode === 'register' || mode === 'forgot'">
              <el-input 
                v-model="form.confirmPassword" 
                type="password"
                placeholder="确认密码"
                size="large"
                :prefix-icon="Lock"
                show-password
              />
            </el-form-item>

            <!-- 邮箱 -->
            <el-form-item prop="email" v-if="mode === 'register' || mode === 'forgot'">
              <el-input 
                v-model="form.email" 
                placeholder="邮箱"
                size="large"
                :prefix-icon="Message"
                clearable
              >
                <template #append>
                  <el-button 
                    :loading="codeSending"
                    :disabled="countdown > 0"
                    @click="handleSendCode"
                  >
                    {{ countdown > 0 ? `${countdown}秒` : '发送验证码' }}
                  </el-button>
                </template>
              </el-input>
            </el-form-item>

            <!-- 验证码 -->
            <el-form-item prop="emailCode" v-if="mode === 'register' || mode === 'forgot'">
              <el-input 
                v-model="form.emailCode" 
                placeholder="验证码"
                size="large"
                :prefix-icon="Key"
                clearable
              />
            </el-form-item>

            <!-- 登录时的记住密码 -->
            <div class="form-extra" v-if="mode === 'login'">
              <el-checkbox v-model="rememberMe">记住密码</el-checkbox>
              <el-link type="primary" @click="mode = 'forgot'">忘记密码？</el-link>
            </div>

            <!-- 提交按钮 -->
            <el-button 
              type="primary" 
              size="large"
              class="submit-btn"
              :loading="loading"
              @click="handleSubmit"
            >
              {{ submitBtnText }}
            </el-button>

            <!-- 切换模式 -->
            <div class="mode-switch">
              <span v-if="mode === 'login'">
                还没有账号？
                <el-link type="primary" @click="switchMode('register')">立即注册</el-link>
              </span>
              <span v-else>
                已有账号？
                <el-link type="primary" @click="switchMode('login')">返回登录</el-link>
              </span>
            </div>
          </el-form>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Message, Key, MagicStick, Timer, UserFilled } from '@element-plus/icons-vue'
import { login, register, sendCode, resetPassword } from '@/api/auth'

const router = useRouter()
const formRef = ref()

// 模式：login, register, forgot
const mode = ref('login')
const loading = ref(false)
const codeSending = ref(false)
const countdown = ref(0)
const rememberMe = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  emailCode: ''
})

// 提交按钮文本
const submitBtnText = computed(() => {
  const texts = {
    login: '登录',
    register: '注册',
    forgot: '重置密码'
  }
  return texts[mode.value] || '提交'
})

// 表单验证规则
const rules = computed(() => {
  const baseRules = {
    username: [
      { required: true, message: '请输入用户名', trigger: 'blur' },
      { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
    ],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' },
      { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
    ],
    confirmPassword: [
      { required: true, message: '请确认密码', trigger: 'blur' },
      {
        validator: (rule, value, callback) => {
          if (value !== form.password) {
            callback(new Error('两次输入密码不一致'))
          } else {
            callback()
          }
        },
        trigger: 'blur'
      }
    ],
    email: [
      { required: true, message: '请输入邮箱', trigger: 'blur' },
      { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
    ],
    emailCode: [
      { required: true, message: '请输入验证码', trigger: 'blur' },
      { len: 6, message: '验证码为6位数字', trigger: 'blur' }
    ]
  }

  if (mode.value === 'login') {
    return {
      username: baseRules.username,
      password: baseRules.password
    }
  } else if (mode.value === 'register') {
    return {
      username: baseRules.username,
      password: baseRules.password,
      confirmPassword: baseRules.confirmPassword,
      email: baseRules.email,
      emailCode: baseRules.emailCode
    }
  } else if (mode.value === 'forgot') {
    return {
      password: baseRules.password,
      confirmPassword: baseRules.confirmPassword,
      email: baseRules.email,
      emailCode: baseRules.emailCode
    }
  }
  return {}
})

// 切换模式
const switchMode = (newMode) => {
  mode.value = newMode
  // 重置表单
  Object.keys(form).forEach(key => form[key] = '')
  
  formRef.value?.clearValidate()
}

// 监听模式变化并清除密码字段
watch(mode, (newMode) => {
  if(newMode === 'forgot') {
    form.password = ''
    form.confirmPassword = ''
  }
})

// 发送验证码
const handleSendCode = async () => {
  if (!form.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }
  
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(form.email)) {
    ElMessage.warning('请输入正确的邮箱格式')
    return
  }

  codeSending.value = true
  try {
    await sendCode({ email: form.email })
    ElMessage.success('验证码已发送，请查收邮箱')
    
    // 倒计时
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    ElMessage.error(error.message || '发送失败')
  } finally {
    codeSending.value = false
  }
}

// 处理登录
const handleLogin = async () => {
  const loginData = {
    username: form.username,
    password: form.password
  }
  const res = await login(loginData)
  localStorage.setItem('token', res.token)
  localStorage.setItem('userInfo', JSON.stringify({
    userId: res.userId,
    username: res.username,
    role: res.role
  }))
  
  if (rememberMe.value) {
    localStorage.setItem('rememberedUser', JSON.stringify({
      username: form.username,
      password: form.password
    }))
  } else {
    localStorage.removeItem('rememberedUser')
  }
  
  ElMessage.success('登录成功')
  
  // 根据用户角色跳转到不同页面
  if (res.role === 'ADMIN') {
    router.push('/admin/subject')
  } else {
    // 默认跳转到用户首页
    router.push('/user/profile')
  }
}

// 处理注册
const handleRegister = async () => {
  const registerData = {
    username: form.username,
    password: form.password,
    confirmPassword: form.confirmPassword,
    email: form.email,
    emailCode: form.emailCode
  }
  await register(registerData)
  ElMessage.success('注册成功，请登录')
  switchMode('login')
}

// 处理重置密码
const handleResetPassword = async () => {
  const resetData = {
    email: form.email,
      emailCode: form.emailCode,
      password: form.password,
      confirmPassword: form.confirmPassword
  }
  await resetPassword(resetData)
  ElMessage.success('密码重置成功，请登录')
  switchMode('login')
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      if (mode.value === 'login') {
        await handleLogin()
      } else if (mode.value === 'register') {
        await handleRegister()
      } else if (mode.value === 'forgot') {
        await handleResetPassword()
      }
    } catch (error) {
      ElMessage.error(error.message || '操作失败')
    } finally {
      loading.value = false
    }
  })
}

// 加载记住的用户信息
const loadRememberedUser = () => {
  const remembered = localStorage.getItem('rememberedUser')
  if (remembered) {
    try {
      const user = JSON.parse(remembered)
      form.username = user.username || ''
      form.password = user.password || ''
      rememberMe.value = true
    } catch (e) {
      console.error('解析记住用户信息失败', e)
    }
  }
}

loadRememberedUser()
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

/* 背景装饰 */
.background-decoration {
  position: absolute;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 20s infinite ease-in-out;
}

.circle-1 {
  width: 300px;
  height: 300px;
  top: -150px;
  left: -150px;
}

.circle-2 {
  width: 500px;
  height: 500px;
  bottom: -250px;
  right: -250px;
  animation-delay: -5s;
}

.circle-3 {
  width: 200px;
  height: 200px;
  top: 50%;
  left: 50%;
  animation-delay: -10s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  33% {
    transform: translateY(-30px) rotate(120deg);
  }
  66% {
    transform: translateY(30px) rotate(240deg);
  }
}

/* 主卡片 */
.login-card {
  width: 1000px;
  max-width: 95%;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.95);
}

.card-wrapper {
  display: flex;
  min-height: 560px;
}

/* 左侧品牌区域 */
.brand-section {
  width: 40%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 30px;
  display: flex;
  align-items: center;
  color: white;
  position: relative;
  overflow: hidden;
}

.brand-section::before {
  content: '';
  position: absolute;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
  top: -50%;
  left: -50%;
  animation: pulse 8s infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 0.5;
  }
  50% {
    transform: scale(1.2);
    opacity: 0.8;
  }
}

.brand-content {
  position: relative;
  z-index: 1;
}

.brand-icon {
  margin-bottom: 24px;
  animation: slideIn 0.6s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.brand-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 8px 0;
  animation: slideIn 0.6s ease-out 0.1s both;
}

.brand-slogan {
  font-size: 16px;
  opacity: 0.9;
  margin: 0 0 40px 0;
  animation: slideIn 0.6s ease-out 0.2s both;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 16px;
  animation: slideIn 0.6s ease-out 0.3s both;
}

.feature-item:nth-child(2) {
  animation-delay: 0.4s;
}

.feature-item:nth-child(3) {
  animation-delay: 0.5s;
}

.feature-item .el-icon {
  font-size: 24px;
  opacity: 0.9;
}

/* 右侧表单区域 */
.form-section {
  width: 60%;
  padding: 48px 40px;
  background: white;
}

.form-header {
  margin-bottom: 32px;
}

.form-title {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin: 0 0 8px 0;
}

.form-subtitle {
  font-size: 14px;
  color: #888;
  margin: 0;
}

.login-form {
  width: 100%;
}

.login-form :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #e4e7ed inset;
  transition: all 0.3s;
}

.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #667eea inset;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2) inset;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 22px;
}

.login-form :deep(.el-input-group__append) {
  padding: 0;
  background: none;
  border: none;
}

.login-form :deep(.el-input-group__append .el-button) {
  height: 100%;
  border-radius: 0 8px 8px 0;
  border: 1px solid #e4e7ed;
  border-left: none;
  background: #f5f7fa;
}

.form-extra {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s;
  margin-bottom: 20px;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
}

.submit-btn:active {
  transform: translateY(0);
}

.mode-switch {
  text-align: center;
  font-size: 14px;
  color: #666;
}

.mode-switch .el-link {
  font-weight: 500;
  margin-left: 4px;
}

/* 响应式 */
@media (max-width: 768px) {
  .login-card {
    max-width: 400px;
    margin: 20px;
  }
  
  .brand-section {
    display: none;
  }
  
  .form-section {
    width: 100%;
    padding: 40px 24px;
  }
}
</style>