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
    component: () => import('../components/AppLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/dashboard',
      },
      {
        path: 'dashboard',
        name: 'dashboard',
        component: () => import('../views/DashboardView.vue'),
      },
      {
        path: 'crm/leads',
        name: 'crm-leads',
        component: () => import('../views/crm/LeadListView.vue'),
      },
      {
        path: 'crm/leads/:leadId',
        name: 'crm-lead-detail',
        component: () => import('../views/crm/LeadDetailView.vue'),
      },
      {
        path: 'crm/employer-units',
        name: 'crm-employer-units',
        component: () => import('../views/crm/EmployerUnitListView.vue'),
      },
      {
        path: 'contracts/labor-contracts',
        name: 'contract-labor-list',
        component: () => import('../views/contract/ContractListView.vue'),
      },
      {
        path: 'contracts/labor-contracts/:contractId',
        name: 'contract-labor-detail',
        component: () => import('../views/contract/ContractDetailView.vue'),
      },
      {
        path: 'system/roles',
        name: 'system-roles',
        component: () => import('../views/system/SystemRoleShellView.vue'),
      },
      {
        path: 'system/audit-logs',
        name: 'system-audit-logs',
        component: () => import('../views/system/AuditLogView.vue'),
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('../views/NotFoundView.vue'),
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
