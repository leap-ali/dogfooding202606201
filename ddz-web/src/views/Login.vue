<template>
  <div class="login-page page-container flex-center">
    <div class="login-box card-base">
      <div class="login-title">
        <h1>三人斗地主</h1>
        <p>在线对战平台</p>
      </div>

      <el-tabs v-model="activeTab" class="login-tabs">
        <el-tab-pane label="账号登录" name="login">
          <el-form :model="loginForm" label-width="80px" @submit.prevent="handleLogin">
            <el-form-item label="用户名">
              <el-input v-model="loginForm.username" placeholder="请输入用户名" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" class="login-btn" @click="handleLogin" :loading="loading">登录</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="注册账号" name="register">
          <el-form :model="registerForm" label-width="80px">
            <el-form-item label="用户名">
              <el-input v-model="registerForm.username" placeholder="请输入用户名(3-20位)" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="registerForm.password" type="password" placeholder="请输入密码(6-20位)" show-password />
            </el-form-item>
            <el-form-item label="确认密码">
              <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请再次输入密码" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" class="login-btn" @click="handleRegister" :loading="loading">注册</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <div class="guest-login">
        <el-button type="success" plain @click="handleGuestLogin" :loading="guestLoading">
          <el-icon><User /></el-icon>
          游客快速登录
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('login')
const loading = ref(false)
const guestLoading = ref(false)

const loginForm = ref({
  username: '',
  password: ''
})

const registerForm = ref({
  username: '',
  password: '',
  confirmPassword: ''
})

async function handleLogin() {
  if (!loginForm.value.username || !loginForm.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    await userStore.login(loginForm.value.username, loginForm.value.password)
    ElMessage.success('登录成功')
    router.push('/lobby')
  } catch (e) {
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  if (!registerForm.value.username || !registerForm.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  if (registerForm.value.password !== registerForm.value.confirmPassword) {
    ElMessage.warning('两次密码输入不一致')
    return
  }
  loading.value = true
  try {
    await userStore.register(registerForm.value.username, registerForm.value.password)
    ElMessage.success('注册成功')
    router.push('/lobby')
  } catch (e) {
  } finally {
    loading.value = false
  }
}

async function handleGuestLogin() {
  guestLoading.value = true
  try {
    await userStore.guestLogin()
    ElMessage.success('登录成功')
    router.push('/lobby')
  } catch (e) {
  } finally {
    guestLoading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-page {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);

  .login-box {
    width: 420px;
    padding: 40px;
    background: rgba(255, 255, 255, 0.95);
    color: #333;
    border-radius: 12px;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
  }

  .login-title {
    text-align: center;
    margin-bottom: 30px;

    h1 {
      font-size: 32px;
      color: #409EFF;
      margin: 0;
      letter-spacing: 4px;
    }

    p {
      color: #909399;
      margin: 8px 0 0 0;
      font-size: 14px;
    }
  }

  .login-tabs {
    :deep(.el-tabs__item) {
      font-size: 16px;
    }
  }

  .login-btn {
    width: 100%;
  }

  .guest-login {
    text-align: center;
    margin-top: 20px;
    padding-top: 20px;
    border-top: 1px solid #eee;
  }
}
</style>
