<template>
  <div class="system-role-shell" data-testid="system-role-shell">
    <el-container class="shell-container">
      <el-aside class="menu-panel" width="240px">
        <h2 class="menu-title">System Management</h2>
        <el-menu default-active="roles" class="system-menu">
          <el-menu-item index="roles" data-testid="system-menu-item">Role Management</el-menu-item>
          <el-menu-item index="menus" data-testid="system-menu-item">Menu Permissions</el-menu-item>
          <el-menu-item index="scope" data-testid="system-menu-item">
            <router-link class="menu-link" to="/system/audit-logs">Audit Logs</router-link>
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
  color: inherit;
  text-decoration: none;
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
