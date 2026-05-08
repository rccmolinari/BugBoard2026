import { createRouter, createWebHistory } from 'vue-router'

// Importa le pagine
import Login from '../pages/LoginPage.vue'
import Register from '../pages/RegisterPage.vue'
import User from '../pages/DashboardPage.vue'
import Admin from '../pages/AdminPage.vue'
import AdminUsers from '../pages/AdminUsersPage.vue'


const routes = [
  {
    path: '/',
    alias: '/login',
    name: 'Login',
    component: Login,
    meta: { guestOnly: true },
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: { guestOnly: true },
  },
  {
    path: '/user',
    name: 'User',
    component: User,
    meta: { requiresAuth: true, allowedRoles: ['normal', 'readonly'] },
  },
  {
    path: '/admin',
    name: 'Admin',
    component: Admin,
    meta: { requiresAuth: true, allowedRoles: ['admin'] },
  },
  {
    path: '/admin/users',
    name: 'AdminUsers',
    component: AdminUsers,
    meta: { requiresAuth: true, allowedRoles: ['admin'] },
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/',
  },
]


const router = createRouter({
  history: createWebHistory(),
  routes,
})

function getUtenteFromStorage() {
  const raw = sessionStorage.getItem('bb_utente')
  if (!raw) return null

  try {
    return JSON.parse(raw)
  } catch (error) {
    console.error('Sessione utente non valida in sessionStorage:', error)
    sessionStorage.removeItem('bb_utente')
    return null
  }
}

function homePathFor(utente) {
  return utente?.ruolo === 'admin' ? '/admin' : '/user'
}

router.beforeEach((to) => {
  const utente = getUtenteFromStorage()

  if (to.meta.requiresAuth && !utente) {
    return { path: '/', query: { redirect: to.fullPath } }
  }

  if (to.meta.guestOnly && utente) {
    return homePathFor(utente)
  }

  const allowedRoles = to.meta.allowedRoles
  if (allowedRoles && utente && !allowedRoles.includes(utente.ruolo)) {
    return homePathFor(utente)
  }

  return true
})

export default router
