import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/lobby',
    name: 'Lobby',
    component: () => import('@/views/Lobby.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/room/:roomNo',
    name: 'RoomWait',
    component: () => import('@/views/RoomWait.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/game/:roomNo',
    name: 'Game',
    component: () => import('@/views/Game.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const token = localStorage.getItem('token')
  
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
