<template>
  <div class="login-page">
    <section class="brand-panel">
      <p class="brand-kicker">Labor System</p>
      <h1>Labor Management Console</h1>
      <p class="brand-desc">
        Manage leads, contracts, attendance, and settlement in one workflow.
      </p>
    </section>

    <section class="form-panel">
      <el-card class="login-card" shadow="always">
        <template #header>
          <div class="card-header">
            <h2>Sign In</h2>
            <p>Username and password authentication</p>
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
          <el-form-item label="Username" prop="username">
            <el-input
              v-model.trim="form.username"
              placeholder="Enter username"
              autocomplete="username"
              data-testid="username-input"
            />
          </el-form-item>

          <el-form-item label="Password" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="Enter password"
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
            Sign In
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
  username: [{ required: true, message: 'Username is required', trigger: 'blur' }],
  password: [
    { required: true, message: 'Password is required', trigger: 'blur' },
    { min: 6, max: 64, message: 'Password must be 6-64 chars', trigger: 'blur' },
  ],
}

async function submit() {
  loginError.value = ''
  if (!formRef.value) {
    return
  }

  if (!form.username.trim() || !form.password) {
    loginError.value = 'Please enter username and password'
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
    loginError.value = error instanceof Error ? error.message : 'Login failed'
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
