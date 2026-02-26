<template>
  <div class="login-page">
    <section class="brand-panel">
      <p class="brand-kicker">Labor System</p>
      <h1>劳务系统管理后台</h1>
      <p class="brand-desc">
        覆盖线索、合同、考勤、结算全链路。请使用平台账号登录后继续操作。
      </p>
    </section>

    <section class="form-panel">
      <el-card class="login-card" shadow="always">
        <template #header>
          <div class="card-header">
            <h2>账号登录</h2>
            <p>支持用户名 + 密码认证</p>
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
            登录
          </el-button>
        </el-form>
      </el-card>
    </section>
  </div>
</template>

<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus'
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { loginByPassword } from '../api/auth'

interface LoginFormModel {
  username: string
  password: string
}

const router = useRouter()
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
    { min: 6, max: 64, message: '密码长度需在 6 到 64 位之间', trigger: 'blur' },
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
    await loginByPassword({ username: form.username, password: form.password })
    await router.push('/dashboard')
  } catch (error) {
    loginError.value = error instanceof Error ? error.message : '登录失败，请稍后重试'
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  background:
    radial-gradient(circle at 15% 25%, rgba(29, 78, 216, 0.24), transparent 46%),
    radial-gradient(circle at 80% 90%, rgba(245, 158, 11, 0.18), transparent 42%),
    linear-gradient(135deg, #f8fafc, #eef2ff);
}

.brand-panel {
  padding: 64px 56px;
  color: #1f2a44;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.brand-kicker {
  margin: 0 0 14px;
  text-transform: uppercase;
  letter-spacing: 0.2em;
  font-size: 12px;
  font-weight: 600;
  color: #1d4ed8;
}

.brand-panel h1 {
  margin: 0;
  font-size: clamp(34px, 4vw, 48px);
  line-height: 1.1;
}

.brand-desc {
  margin: 18px 0 0;
  font-size: 16px;
  line-height: 1.7;
  color: #3f4f6b;
  max-width: 520px;
}

.form-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px;
}

.login-card {
  width: min(440px, 100%);
  border-radius: 16px;
}

.card-header h2 {
  margin: 0;
  font-size: 24px;
}

.card-header p {
  margin: 8px 0 0;
  color: #64748b;
  font-size: 14px;
}

.error-alert {
  margin-bottom: 16px;
}

.submit-button {
  width: 100%;
  height: 44px;
  margin-top: 8px;
  font-weight: 600;
}

@media (max-width: 900px) {
  .login-page {
    grid-template-columns: 1fr;
  }

  .brand-panel {
    padding: 40px 24px 8px;
  }

  .form-panel {
    padding: 16px 16px 32px;
    align-items: flex-start;
  }
}
</style>
