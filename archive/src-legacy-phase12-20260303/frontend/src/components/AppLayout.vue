<template>
  <div class="app-layout" data-testid="app-layout">
    <aside class="app-sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="brand-block">
        <span class="brand-logo" />
        <span v-if="!sidebarCollapsed" class="brand-name">恒信劳务管理系统</span>
      </div>

      <nav class="nav-menu">
        <section class="menu-group">
          <p v-if="!sidebarCollapsed" class="menu-group-title">主导航</p>
          <router-link
            v-for="item in mainMenu"
            :key="item.path"
            class="menu-item"
            :class="{ disabled: item.disabled }"
            :to="item.disabled ? '' : item.path"
            active-class="active"
          >
            <span class="menu-icon">{{ item.icon }}</span>
            <span v-if="!sidebarCollapsed" class="menu-label">{{ item.label }}</span>
          </router-link>
        </section>

        <section class="menu-group">
          <p v-if="!sidebarCollapsed" class="menu-group-title">系统管理</p>
          <router-link
            v-for="item in systemMenu"
            :key="item.path"
            class="menu-item"
            :to="item.path"
            active-class="active"
          >
            <span class="menu-icon">{{ item.icon }}</span>
            <span v-if="!sidebarCollapsed" class="menu-label">{{ item.label }}</span>
          </router-link>
        </section>
      </nav>

      <button class="collapse-btn" @click="sidebarCollapsed = !sidebarCollapsed">
        {{ sidebarCollapsed ? '→' : '←' }}
      </button>
    </aside>

    <div class="app-main">
      <header class="app-header" data-testid="app-header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentBreadcrumb">{{ currentBreadcrumb }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <span class="notify-dot" />
          <div class="user-avatar">{{ userName.slice(0, 1) }}</div>
          <div class="user-meta">
            <strong>{{ userName }}</strong>
            <span>{{ userRole }}</span>
          </div>
          <el-button link type="danger" data-testid="logout-button" @click="handleLogout">退出</el-button>
        </div>
      </header>

      <main class="app-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const sidebarCollapsed = ref(false)
const userName = '张明'
const userRole = '管理员'

const mainMenu = [
  { path: '/dashboard', label: '工作台', icon: '📊', disabled: false },
  { path: '/crm/employer-units', label: '用工单位管理', icon: '🏢', disabled: false },
  { path: '/crm/leads', label: '客户线索管理', icon: '📋', disabled: false },
  { path: '/contracts/labor-contracts', label: '合同管理', icon: '📝', disabled: false },
]

const systemMenu = [
  { path: '/system/roles', label: '系统设置', icon: '⚙️' },
  { path: '/system/audit-logs', label: '审计日志', icon: '📜' },
]

const breadcrumbMap: Record<string, string> = {
  '/dashboard': '',
  '/crm/employer-units': '用工单位管理',
  '/crm/leads': '客户线索管理',
  '/contracts/labor-contracts': '合同管理',
  '/system/roles': '系统设置',
  '/system/audit-logs': '审计日志',
}

const currentBreadcrumb = computed(() => {
  const path = route.path
  if (breadcrumbMap[path] !== undefined) return breadcrumbMap[path]
  if (path.startsWith('/crm/leads/')) return '线索详情'
  if (path.startsWith('/contracts/labor-contracts/')) return '合同详情'
  return ''
})

function handleLogout() {
  authStore.clearSession()
  router.push('/login')
}
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
  display: flex;
  font-family: 'Source Han Sans SC', 'Noto Sans SC', 'PingFang SC', sans-serif;
  background: #f3f5f9;
  color: #111827;
}

.app-sidebar {
  width: 228px;
  border-right: 1px solid #e5e7eb;
  background: #fff;
  padding: 18px 12px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  transition: width 0.25s ease;
  position: relative;
}

.app-sidebar.collapsed {
  width: 68px;
}

.brand-block {
  height: 54px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 8px;
  overflow: hidden;
}

.brand-logo {
  width: 28px;
  height: 28px;
  min-width: 28px;
  border-radius: 8px;
  background:
    linear-gradient(140deg, rgba(17, 24, 39, 0.88), rgba(30, 58, 138, 0.9)),
    radial-gradient(circle at 20% 20%, #60a5fa, transparent 52%);
}

.brand-name {
  font-size: 17px;
  font-weight: 700;
  letter-spacing: 0.2px;
  white-space: nowrap;
}

.nav-menu {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.menu-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.menu-group-title {
  margin: 0;
  padding: 4px 10px;
  font-size: 12px;
  color: #9ca3af;
  letter-spacing: 0.5px;
}

.menu-item {
  height: 44px;
  padding: 0 14px;
  border-radius: 10px;
  color: #475569;
  display: flex;
  align-items: center;
  font-size: 14px;
  text-decoration: none;
  gap: 10px;
  transition: background 0.15s;
}

.menu-item:hover { background: #eff6ff; color: #2563eb; }
.menu-item.active { background: #eff6ff; color: #2563eb; font-weight: 600; }
.menu-item.disabled { cursor: default; opacity: 0.5; pointer-events: none; }

.menu-icon {
  font-size: 16px;
  min-width: 22px;
  text-align: center;
}

.collapse-btn {
  width: 100%;
  border: 0;
  border-top: 1px solid #f3f4f6;
  background: transparent;
  padding: 10px 0;
  color: #9ca3af;
  cursor: pointer;
  font-size: 14px;
}

.collapse-btn:hover { color: #2563eb; }

.app-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.app-header {
  height: 58px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  padding: 0 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.notify-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #ef4444;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  color: #1d4ed8;
  background: linear-gradient(135deg, #dbeafe, #fef3c7);
  font-size: 13px;
}

.user-meta {
  display: flex;
  flex-direction: column;
  line-height: 1.1;
}

.user-meta strong { font-size: 13px; color: #111827; }
.user-meta span { margin-top: 3px; font-size: 11px; color: #6b7280; }

.app-content {
  flex: 1;
  overflow-y: auto;
}

@media (max-width: 980px) {
  .app-layout { flex-direction: column; }
  .app-sidebar {
    width: 100% !important;
    border-right: 0;
    border-bottom: 1px solid #e5e7eb;
    flex-direction: row;
    flex-wrap: wrap;
    align-items: center;
    gap: 8px 12px;
    padding: 12px;
  }
  .brand-block { width: auto; }
  .nav-menu { flex-direction: row; flex-wrap: wrap; gap: 4px; }
  .menu-group { flex-direction: row; flex-wrap: wrap; gap: 4px; }
  .menu-group-title { display: none; }
  .collapse-btn { display: none; }
  .app-header { height: auto; padding: 10px 16px; }
}
</style>
