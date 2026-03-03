<template>
  <div class="not-found-page" data-testid="not-found-view">
    <div class="not-found-card">
      <h1>404</h1>
      <h2>页面未找到</h2>
      <p>您访问的页面不存在或已被移除</p>
      <div class="actions">
        <el-button type="primary" @click="goHome">返回工作台</el-button>
        <el-button @click="goBack">返回上一页</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

function goHome() {
  router.push('/dashboard')
}

function goBack() {
  if (window.history.length > 1) {
    router.back()
    return
  }
  router.push('/dashboard')
}
</script>

<style scoped>
.not-found-page {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(145deg, #f8fafc, #eef2ff 58%, #f1f5f9);
}

.not-found-card {
  text-align: center;
  max-width: 480px;
  padding: 40px;
}

.not-found-card h1 {
  margin: 0;
  font-size: 120px;
  line-height: 1;
  background: linear-gradient(135deg, #2563eb, #60a5fa);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.not-found-card h2 {
  margin: 16px 0 8px;
  font-size: 24px;
  color: #1f2937;
}

.not-found-card p {
  margin: 0 0 28px;
  color: #6b7280;
  font-size: 15px;
}

.actions {
  display: flex;
  justify-content: center;
  gap: 12px;
}
</style>
