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
        name: 'Login',
        component: Login
    },
    {
        path: '/user',
        name: 'User',
        component: User
    },
    {
        path: '/register',
        name: 'Register',
        component: Register
    },
    {
        path: '/admin',
        name: 'Admin',
        component: Admin
    },
    {
        path: '/admin/users',
        name: 'AdminUsers',
        component: AdminUsers
    }
]


const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
