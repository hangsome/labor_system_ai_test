<template>
  <div class="system-role-shell" data-testid="system-role-shell">
    <el-container class="shell-container">
      <el-aside class="menu-panel" width="240px">
        <h2 class="menu-title">System Management</h2>
        <el-menu default-active="roles" class="system-menu">
          <el-menu-item
            v-for="item in menuItems"
            :key="item.index"
            :index="item.index"
            :disabled="!item.enabled"
            :class="{ 'menu-item-disabled': !item.enabled }"
            data-testid="system-menu-item"
          >
            <router-link v-if="item.enabled" class="menu-link" :to="item.route">{{ item.label }}</router-link>
            <span v-else class="menu-disabled-text" data-testid="system-menu-disabled">
              {{ item.label }} (coming soon)
            </span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-main class="content-panel">
        <el-card shadow="never" data-testid="role-page-card">
          <template #header>
            <div class="header-block">
              <h1>Role Management</h1>
              <p>Phase 01 shell for role, menu, and data-scope configuration.</p>
            </div>
          </template>
          <el-empty description="Role list and permission matrix will be implemented in next tasks." />
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
interface SystemMenuItem {
  index: string
  label: string
  route: string
  enabled: boolean
}

const menuItems: SystemMenuItem[] = [
  { index: 'roles', label: 'Role Management', route: '/system/roles', enabled: true },
  { index: 'audit', label: 'Audit Logs', route: '/system/audit-logs', enabled: true },
  { index: 'scope', label: 'Data Scope Policy', route: '/system/roles', enabled: false },
]
</script>

<style scoped>
.system-role-shell {
  min-height: 100vh;
  padding: 24px;
  background: linear-gradient(140deg, #f8fafc, #eef2ff 45%, #f1f5f9);
}

.shell-container {
  min-height: calc(100vh - 48px);
  border-radius: 12px;
  overflow: hidden;
  background: #ffffff;
  border: 1px solid #e2e8f0;
}

.menu-panel {
  border-right: 1px solid #e2e8f0;
  padding: 18px 12px;
  background: #f8fafc;
}

.menu-title {
  margin: 0 0 12px;
  padding: 0 8px;
  font-size: 16px;
  color: #1e293b;
}

.system-menu {
  border-right: none;
  background: transparent;
}

.menu-link {
  display: inline-flex;
  width: 100%;
  color: inherit;
  text-decoration: none;
}

.menu-item-disabled {
  color: #94a3b8;
}

.menu-disabled-text {
  color: #94a3b8;
}

.content-panel {
  padding: 20px;
}

.header-block h1 {
  margin: 0;
  font-size: 24px;
  color: #0f172a;
}

.header-block p {
  margin: 8px 0 0;
  color: #64748b;
}
</style>
