<template>
  <div class="login-page">
    <aside class="brand-panel">
      <div class="brand-header">
        <span class="brand-logo" />
        <div>
          <h1>恒信劳务管理系统</h1>
          <p>Labor Management Platform</p>
        </div>
      </div>

      <div class="brand-body">
        <h2>合同、线索、用工与结算的一体化工作台</h2>
        <ul>
          <li>统一管理客户线索与合同进度</li>
          <li>覆盖用工单位与员工协同流程</li>
          <li>关键数据支持全链路审计追溯</li>
        </ul>
      </div>
    </aside>

    <section class="form-panel">
      <el-card class="login-card" shadow="always">
        <template #header>
          <div class="card-header">
            <h3>账号登录</h3>
            <p>请输入管理员账号与密码</p>
          </div>
        </template>

        <el-alert
          v-if="loginError"
          type="error"
          :title="loginError"
          show-icon
          :closable="false"
          class="error-alert"
          data-testid="login-error"
        />

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model.trim="form.username"
              placeholder="请输入用户名"
              autocomplete="username"
              data-testid="username-input"
            />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              autocomplete="current-password"
              show-password
              data-testid="password-input"
              @keyup.enter="submit"
            />
          </el-form-item>

          <el-button
            type="primary"
            class="submit-button"
            :loading="submitting"
            data-testid="login-submit"
            @click="submit"
          >
            登录系统
          </el-button>
        </el-form>
      </el-card>
    </section>
  </div>
</template>

<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus'
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { loginByPassword } from '../api/auth'
import { useAuthStore } from '../store/auth'

interface LoginFormModel {
  username: string
  password: string
}

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
authStore.hydrate()

const formRef = ref<FormInstance>()
const submitting = ref(false)
const loginError = ref('')

const form = reactive<LoginFormModel>({
  username: '',
  password: '',
})

const rules: FormRules<LoginFormModel> = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 64, message: '密码长度需在 6-64 位之间', trigger: 'blur' },
  ],
}

async function submit() {
  loginError.value = ''
  if (!formRef.value) {
    return
  }

  if (!form.username.trim() || !form.password) {
    loginError.value = '请输入用户名和密码'
    return
  }

  try {
    await formRef.value.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    const token = await loginByPassword({ username: form.username, password: form.password })
    authStore.setSession(token)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/dashboard'
    await router.push(redirect)
  } catch (error) {
    loginError.value = error instanceof Error ? error.message : '登录失败'
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.18fr 1fr;
  background: #f3f5f9;
}

.brand-panel {
  background:
    linear-gradient(150deg, rgba(30, 58, 138, 0.92), rgba(37, 99, 235, 0.82)),
    radial-gradient(circle at 16% 20%, rgba(255, 255, 255, 0.18), transparent 40%);
  color: #eff6ff;
  padding: 56px 48px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.brand-header {
  display: flex;
  align-items: center;
  gap: 14px;
}

.brand-logo {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.95), rgba(191, 219, 254, 0.95)),
    radial-gradient(circle at 24% 24%, rgba(37, 99, 235, 0.3), transparent 60%);
}

.brand-header h1 {
  margin: 0;
  font-size: 28px;
  letter-spacing: 0.4px;
}

.brand-header p {
  margin: 6px 0 0;
  font-size: 13px;
  color: #cbd5e1;
}

.brand-body h2 {
  margin: 0;
  max-width: 540px;
  font-size: 34px;
  line-height: 1.25;
}

.brand-body ul {
  margin: 28px 0 0;
  padding: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 12px;
  color: #dbeafe;
  font-size: 16px;
}

.brand-body li {
  position: relative;
  padding-left: 20px;
}

.brand-body li::before {
  content: '';
  position: absolute;
  left: 0;
  top: 9px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #93c5fd;
}

.form-panel {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 28px;
  background: linear-gradient(145deg, #f8fafc, #eef2ff 58%, #f1f5f9);
}

.login-card {
  width: min(460px, 100%);
  border-radius: 16px;
  border: 1px solid #e2e8f0;
}

.card-header h3 {
  margin: 0;
  font-size: 24px;
  color: #111827;
}

.card-header p {
  margin: 8px 0 0;
  color: #64748b;
  font-size: 14px;
}

.error-alert {
  margin-bottom: 14px;
}

.submit-button {
  width: 100%;
  height: 44px;
  margin-top: 8px;
  font-weight: 600;
}

@media (max-width: 980px) {
  .login-page {
    grid-template-columns: 1fr;
  }

  .brand-panel {
    padding: 30px 22px;
    gap: 22px;
  }

  .brand-body h2 {
    font-size: 28px;
  }

  .form-panel {
    padding: 20px 14px 30px;
  }
}
</style>
