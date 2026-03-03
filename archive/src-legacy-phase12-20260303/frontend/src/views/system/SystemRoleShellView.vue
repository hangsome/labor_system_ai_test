<template>
  <div class="system-role-page" data-testid="system-role-shell">
    <section class="page-head">
      <div>
        <h1>角色权限管理</h1>
        <p>统一管理系统角色、菜单权限与数据访问范围</p>
      </div>
      <div class="head-actions">
        <el-button @click="toggleOnlyEnabled">{{ showOnlyEnabled ? '查看全部' : '仅看启用' }}</el-button>
        <el-button type="primary" @click="openCreateDialog">添加角色</el-button>
      </div>
    </section>

    <section class="stats-grid">
      <el-card shadow="never" class="stat-card">
        <h4>全部角色</h4>
        <p>{{ roleRows.length }}</p>
        <small>↑ 12.5% 较上月</small>
      </el-card>
      <el-card shadow="never" class="stat-card">
        <h4>启用角色</h4>
        <p>{{ enabledCount }}</p>
        <small>↑ 9.3% 较上月</small>
      </el-card>
      <el-card shadow="never" class="stat-card">
        <h4>只读角色</h4>
        <p>{{ readOnlyCount }}</p>
        <small>↑ 3.2% 较上月</small>
      </el-card>
      <el-card shadow="never" class="stat-card stat-card-alert">
        <h4>待审核角色</h4>
        <p>{{ pendingCount }}</p>
        <small>↑ 1 较昨日</small>
      </el-card>
    </section>

    <el-card shadow="never" class="role-table-card" data-testid="role-page-card">
      <template #header>
        <div class="card-title-row">
          <h3>角色列表</h3>
          <div class="header-tools">
            <el-input
              v-model.trim="keyword"
              placeholder="搜索角色名称或ID"
              clearable
              style="width: 260px"
              @keyup.enter="queryRoles"
              @clear="queryRoles"
            />
          </div>
        </div>
      </template>

      <el-table :data="filteredRows" stripe border>
        <el-table-column prop="roleId" label="角色ID" min-width="120" />
        <el-table-column prop="roleName" label="角色名称" min-width="180" />
        <el-table-column label="数据范围" min-width="160">
          <template #default="{ row }">
            {{ getScopeText(row.scope) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="120">
          <template #default="{ row }">
            <el-tag :type="getRoleStatusTag(row.status)" effect="light">
              {{ getRoleStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" min-width="180" />
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewRole(row)">查看</el-button>
            <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="deleteRole(row.roleId)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '新增角色' : '编辑角色'"
      width="520px"
    >
      <el-form label-position="top">
        <el-form-item label="角色ID">
          <el-input v-model.trim="roleForm.roleId" :disabled="dialogMode === 'edit'" />
        </el-form-item>
        <el-form-item label="角色名称">
          <el-input v-model.trim="roleForm.roleName" />
        </el-form-item>
        <el-form-item label="数据范围">
          <el-select v-model="roleForm.scope" style="width: 100%">
            <el-option v-for="item in scopeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色状态">
          <el-select v-model="roleForm.status" style="width: 100%">
            <el-option v-for="item in roleStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRole">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="drawerVisible" title="角色详情" :size="420">
      <template v-if="currentRole">
        <p><strong>角色ID：</strong>{{ currentRole.roleId }}</p>
        <p><strong>角色名称：</strong>{{ currentRole.roleName }}</p>
        <p><strong>数据范围：</strong>{{ getScopeText(currentRole.scope) }}</p>
        <p><strong>角色状态：</strong>{{ getRoleStatusText(currentRole.status) }}</p>
        <p><strong>更新时间：</strong>{{ currentRole.updatedAt }}</p>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { scopeTextMap } from '../../constants/status'

type ScopeCode = keyof typeof scopeTextMap

type RoleStatusCode = 'ENABLED' | 'PENDING' | 'DISABLED'

interface RoleRow {
  roleId: string
  roleName: string
  scope: ScopeCode
  status: RoleStatusCode
  updatedAt: string
}

const roleStatusTextMap: Record<RoleStatusCode, string> = {
  ENABLED: '启用',
  PENDING: '待审核',
  DISABLED: '禁用',
}

const roleStatusTagMap: Record<RoleStatusCode, 'success' | 'warning' | 'info'> = {
  ENABLED: 'success',
  PENDING: 'warning',
  DISABLED: 'info',
}

const roleRows = ref<RoleRow[]>([
  {
    roleId: 'R202601001',
    roleName: '系统管理员',
    scope: 'ALL',
    status: 'ENABLED',
    updatedAt: '2026-02-26 20:30',
  },
  {
    roleId: 'R202601002',
    roleName: '财务专员',
    scope: 'CLIENT',
    status: 'ENABLED',
    updatedAt: '2026-02-26 19:12',
  },
  {
    roleId: 'R202601003',
    roleName: '用工主管',
    scope: 'PROJECT',
    status: 'ENABLED',
    updatedAt: '2026-02-26 18:45',
  },
  {
    roleId: 'R202601004',
    roleName: '审计观察员',
    scope: 'SELF',
    status: 'PENDING',
    updatedAt: '2026-02-26 16:20',
  },
  {
    roleId: 'R202601005',
    roleName: '客诉处理员',
    scope: 'DEPT',
    status: 'DISABLED',
    updatedAt: '2026-02-25 14:10',
  },
])

const keyword = ref('')
const showOnlyEnabled = ref(false)

const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const drawerVisible = ref(false)
const currentRole = ref<RoleRow | null>(null)

const roleForm = reactive<RoleRow>({
  roleId: '',
  roleName: '',
  scope: 'ALL',
  status: 'ENABLED',
  updatedAt: '',
})

const scopeOptions = (Object.keys(scopeTextMap) as ScopeCode[]).map((key) => ({
  value: key,
  label: scopeTextMap[key],
}))

const roleStatusOptions = (Object.keys(roleStatusTextMap) as RoleStatusCode[]).map((key) => ({
  value: key,
  label: roleStatusTextMap[key],
}))

const filteredRows = computed(() => {
  const value = keyword.value.trim().toLowerCase()
  return roleRows.value.filter((item) => {
    const matchKeyword =
      !value ||
      item.roleName.toLowerCase().includes(value) ||
      item.roleId.toLowerCase().includes(value)
    const matchStatus = !showOnlyEnabled.value || item.status === 'ENABLED'
    return matchKeyword && matchStatus
  })
})

const enabledCount = computed(() => roleRows.value.filter((item) => item.status === 'ENABLED').length)
const pendingCount = computed(() => roleRows.value.filter((item) => item.status === 'PENDING').length)
const readOnlyCount = computed(() => roleRows.value.filter((item) => item.scope === 'SELF').length)

function nowTimestamp() {
  const date = new Date()
  const yyyy = date.getFullYear()
  const MM = String(date.getMonth() + 1).padStart(2, '0')
  const dd = String(date.getDate()).padStart(2, '0')
  const HH = String(date.getHours()).padStart(2, '0')
  const mm = String(date.getMinutes()).padStart(2, '0')
  return `${yyyy}-${MM}-${dd} ${HH}:${mm}`
}

function queryRoles() {
  ElMessage.success('已按条件筛选角色')
}

function toggleOnlyEnabled() {
  showOnlyEnabled.value = !showOnlyEnabled.value
  queryRoles()
}

function openCreateDialog() {
  dialogMode.value = 'create'
  roleForm.roleId = `R2026${String(roleRows.value.length + 1).padStart(4, '0')}`
  roleForm.roleName = ''
  roleForm.scope = 'ALL'
  roleForm.status = 'ENABLED'
  roleForm.updatedAt = nowTimestamp()
  dialogVisible.value = true
}

function openEditDialog(row: RoleRow) {
  dialogMode.value = 'edit'
  roleForm.roleId = row.roleId
  roleForm.roleName = row.roleName
  roleForm.scope = row.scope
  roleForm.status = row.status
  roleForm.updatedAt = row.updatedAt
  dialogVisible.value = true
}

function saveRole() {
  if (!roleForm.roleId || !roleForm.roleName) {
    ElMessage.error('请完整填写角色信息')
    return
  }

  if (dialogMode.value === 'create') {
    const exists = roleRows.value.some((item) => item.roleId === roleForm.roleId)
    if (exists) {
      ElMessage.error('角色ID已存在')
      return
    }
    roleRows.value.unshift({ ...roleForm, updatedAt: nowTimestamp() })
    ElMessage.success('角色创建成功')
  } else {
    const index = roleRows.value.findIndex((item) => item.roleId === roleForm.roleId)
    if (index >= 0) {
      roleRows.value[index] = { ...roleForm, updatedAt: nowTimestamp() }
      ElMessage.success('角色更新成功')
    }
  }

  dialogVisible.value = false
}

function deleteRole(roleId: string) {
  roleRows.value = roleRows.value.filter((item) => item.roleId !== roleId)
  ElMessage.success('角色删除成功')
}

function viewRole(row: RoleRow) {
  currentRole.value = row
  drawerVisible.value = true
}

function getScopeText(scope: string) {
  return scopeTextMap[scope as ScopeCode] ?? scope
}

function getRoleStatusText(status: string) {
  return roleStatusTextMap[status as RoleStatusCode] ?? status
}

function getRoleStatusTag(status: string) {
  return roleStatusTagMap[status as RoleStatusCode] ?? 'info'
}
</script>

<style scoped>
.system-role-page {
  padding: 22px;
}

.page-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 14px;
}

.page-head h1 {
  margin: 0;
  font-size: 34px;
  line-height: 1.15;
  color: #1f2937;
}

.page-head p {
  margin: 8px 0 0;
  color: #6b7280;
}

.head-actions {
  display: flex;
  gap: 10px;
}

.stats-grid {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.stat-card {
  border-radius: 12px;
  border: 1px solid #edf0f6;
}

.stat-card h4 { margin: 0; font-size: 14px; color: #64748b; }
.stat-card p { margin: 10px 0 8px; font-size: 46px; line-height: 1; color: #111827; font-weight: 700; }
.stat-card small { color: #16a34a; font-size: 13px; font-weight: 600; }
.stat-card-alert p, .stat-card-alert small { color: #ef4444; }

.role-table-card {
  margin-top: 16px;
  border-radius: 12px;
  border: 1px solid #edf0f6;
}

.card-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title-row h3 { margin: 0; font-size: 18px; }

.header-tools {
  display: flex;
  gap: 10px;
}

@media (max-width: 1180px) {
  .stats-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}

@media (max-width: 720px) {
  .system-role-page { padding: 14px; }
  .page-head { flex-direction: column; }
  .stats-grid { grid-template-columns: 1fr; }
}
</style>
