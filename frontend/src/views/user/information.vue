<template>
  <div class="information-container">
    <div class="card">
      <div class="card-header">
        <h3>👤 个人资料</h3>
      </div>
      <div class="profile-form">
        <el-form 
          ref="profileFormRef" 
          :model="profileForm" 
          :rules="profileRules" 
          label-width="100px"
          size="large"
        >
          <el-form-item label="用户名">
            <el-input v-model="profileForm.username" disabled />
          </el-form-item>
          
          <el-form-item label="邮箱">
            <el-input v-model="profileForm.email" disabled />
          </el-form-item>
          
          <el-divider />
          
          <el-form-item label="新密码" prop="newPassword">
            <el-input 
              v-model="profileForm.newPassword" 
              type="password"
              placeholder="请输入新密码"
              show-password
            />
          </el-form-item>
          
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input 
              v-model="profileForm.confirmPassword" 
              type="password"
              placeholder="请再次输入新密码"
              show-password
            />
          </el-form-item>
          
          <el-form-item label="邮箱验证码" prop="emailCode">
            <div class="code-input-wrapper">
              <el-input 
                v-model="profileForm.emailCode" 
                placeholder="请输入验证码"
                maxlength="6"
              />
              <el-button 
                :disabled="countdown > 0"
                @click="sendVerificationCode"
                class="code-button"
              >
                {{ countdown > 0 ? `${countdown}秒后重发` : '获取验证码' }}
              </el-button>
            </div>
          </el-form-item>
          
          <el-form-item>
            <el-button 
              type="primary" 
              @click="submitForm"
              :loading="submitting"
              size="large"
              style="width: 200px;"
            >
              保存修改
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { resetPassword, sendCode, getCurrentUser } from '@/api/auth'

const profileFormRef = ref()
const submitting = ref(false)
const countdown = ref(0)

const profileForm = reactive({
  username: '',
  email: '',
  newPassword: '',
  confirmPassword: '',
  emailCode: ''
})

// 表单验证规则
const profileRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== profileForm.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  emailCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' }
  ]
}

// 初始化用户信息
onMounted(async () => {
  await loadUserInfo()
})

const loadUserInfo = async () => {
  try {
    const response = await getCurrentUser();
    if (response.success) {
      profileForm.username = response.username || ''
      profileForm.email = response.email || ''
    } else {
      throw new Error(response.message || '获取用户信息失败')
    }
  } catch (error) {
    console.error('获取用户信息失败', error)
    ElMessage.error(error.message || '获取用户信息失败')
  }
}

const sendVerificationCode = async () => {
  if (!profileForm.email) {
    ElMessage.warning('邮箱信息未获取，请刷新页面')
    return
  }
  
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(profileForm.email)) {
    ElMessage.warning('邮箱格式不正确')
    return
  }

  try {
    await sendCode({ email: profileForm.email })
    ElMessage.success('验证码已发送至您的邮箱，请查收')
    
    // 开始倒计时
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    ElMessage.error(error.message || '发送验证码失败')
  }
}

const submitForm = async () => {
  if (!profileFormRef.value) return
  
  // 手动验证密码和验证码
  if (!profileForm.newPassword) {
    ElMessage.warning('请输入新密码')
    return
  }
  
  if (profileForm.newPassword !== profileForm.confirmPassword) {
    ElMessage.warning('两次输入密码不一致')
    return
  }
  
  if (profileForm.newPassword.length < 6) {
    ElMessage.warning('密码长度不能少于6位')
    return
  }
  
  if (!profileForm.emailCode) {
    ElMessage.warning('请输入验证码')
    return
  }
  
  if (profileForm.emailCode.length !== 6) {
    ElMessage.warning('验证码为6位数字')
    return
  }
  
  submitting.value = true
  try {
    const resetData = {
      email: profileForm.email, // 使用当前用户的邮箱
      emailCode: profileForm.emailCode,
      password: profileForm.newPassword,
      confirmPassword: profileForm.confirmPassword
    }
    
    await resetPassword(resetData)
    ElMessage.success('密码修改成功')
    
    // 清空密码相关字段
    profileForm.newPassword = ''
    profileForm.confirmPassword = ''
    profileForm.emailCode = ''
  } catch (error) {
    console.error('修改密码失败', error)
    ElMessage.error(error.message || '修改密码失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.information-container {
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

.profile-form {
  max-width: 600px;
}

.code-input-wrapper {
  display: flex;
  gap: 10px;
}

.code-button {
  width: auto;
  min-width: 120px;
}

:deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #e4e7ed inset;
  transition: all 0.3s;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #667eea inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2) inset;
}

:deep(.el-divider) {
  margin: 24px 0;
}
</style>