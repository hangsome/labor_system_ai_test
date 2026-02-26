import { createRouter, createWebHistory } from 'vue-router'
import { store } from '../store'
import { useAuthStore } from '../store/auth'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue'),
  },
  {
    path: '/',
    redirect: '/dashboard',
  },
  {
    path: '/dashboard',
    name: 'dashboard',
    component: () => import('../views/DashboardView.vue'),
    meta: {
      requiresAuth: true,
    },
  },
  {
    path: '/crm/leads',
    name: 'crm-leads',
    component: () => import('../views/crm/LeadListView.vue'),
    meta: {
      requiresAuth: true,
    },
  },
  {
    path: '/crm/leads/:leadId',
    name: 'crm-lead-detail',
    component: () => import('../views/crm/LeadDetailView.vue'),
    meta: {
      requiresAuth: true,
    },
  },
  {
    path: '/system/roles',
    name: 'system-roles',
    component: () => import('../views/system/SystemRoleShellView.vue'),
    meta: {
      requiresAuth: true,
    },
  },
  {
    path: '/system/audit-logs',
    name: 'system-audit-logs',
    component: () => import('../views/system/AuditLogView.vue'),
    meta: {
      requiresAuth: true,
    },
  },
]

export const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const authStore = useAuthStore(store)
  authStore.hydrate()

  if (to.path === '/login' && authStore.isAuthenticated) {
    return '/dashboard'
  }

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return {
      path: '/login',
      query: { redirect: to.fullPath },
    }
  }

  return true
})
