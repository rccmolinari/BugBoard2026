import { createRouter, createWebHistory } from 'vue-router'

// Importa le pagine
import Login from '../pages/LoginPage.vue'
import User from '../pages/DashboardPage.vue'
import Admin from '../pages/AdminPage.vue'


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
        path: '/admin',
        name: 'Admin',
        component: Admin
    }
]


const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router