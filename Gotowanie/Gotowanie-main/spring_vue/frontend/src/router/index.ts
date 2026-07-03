import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import type { Role } from '@/types/auth'

import LoginPage from '@/pages/LoginPage.vue'
import RegisterPage from '@/pages/RegisterPage.vue'
import DashboardPage from '@/pages/DashboardPage.vue'
import ResourcesPage from '@/pages/ResourcesPage.vue'
import ResourceDetailsPage from '@/pages/ResourceDetailsPage.vue'
import CreateRequestPage from '@/pages/CreateRequestPage.vue'
import MyRequestsPage from '@/pages/MyRequestsPage.vue'
import RequestDetailsPage from '@/pages/RequestDetailsPage.vue'
import AdminResourcesPage from '@/pages/AdminResourcesPage.vue'
import AdminRequestsPage from '@/pages/AdminRequestsPage.vue'
import CalendarPage from '@/pages/CalendarPage.vue'

declare module 'vue-router' {
  interface RouteMeta {
    layout?: 'auth' | 'dashboard'
    public?: boolean
    requiresAuth?: boolean
    roles?: Role[]
  }
}

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/dashboard' },
    { path: '/login', name: 'login', component: LoginPage, meta: { layout: 'auth', public: true } },
    { path: '/register', name: 'register', component: RegisterPage, meta: { layout: 'auth', public: true } },
    { path: '/dashboard', name: 'dashboard', component: DashboardPage, meta: { requiresAuth: true } },
    { path: '/resources', name: 'resources', component: ResourcesPage, meta: { requiresAuth: true } },
    {
      path: '/resources/:id',
      name: 'resource-details',
      component: ResourceDetailsPage,
      meta: { requiresAuth: true },
    },
    {
      path: '/requests/new',
      name: 'create-request',
      component: CreateRequestPage,
      meta: { requiresAuth: true },
    },
    { path: '/requests/my', name: 'my-requests', component: MyRequestsPage, meta: { requiresAuth: true } },
    {
      path: '/requests/:id',
      name: 'request-details',
      component: RequestDetailsPage,
      meta: { requiresAuth: true },
    },
    {
      path: '/admin/resources',
      name: 'admin-resources',
      component: AdminResourcesPage,
      meta: { requiresAuth: true, roles: ['ADMIN'] },
    },
    {
      path: '/admin/requests',
      name: 'admin-requests',
      component: AdminRequestsPage,
      meta: { requiresAuth: true, roles: ['ADMIN', 'OPERATOR'] },
    },
    {
      path: '/calendar',
      name: 'calendar',
      component: CalendarPage,
      meta: { requiresAuth: true, roles: ['ADMIN'] },
    },
    { path: '/:pathMatch(.*)*', redirect: '/dashboard' },
  ],
})

// Server-side security is authoritative; these guards are purely UX (avoid rendering pages the
// user cannot use, and redirect to login).
router.beforeEach(async (to) => {
  const auth = useAuthStore()
  if (!auth.initialized) {
    await auth.init()
  }

  if (to.meta.public) {
    return auth.isAuthenticated ? { name: 'dashboard' } : true
  }

  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  const roles = to.meta.roles
  if (roles && roles.length > 0 && (auth.role === null || !roles.includes(auth.role))) {
    return { name: 'dashboard' }
  }

  return true
})

export default router
