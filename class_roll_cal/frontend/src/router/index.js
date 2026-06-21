import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/students' },
  { path: '/students', name: 'StudentList', component: () => import('../views/StudentList.vue') },
  { path: '/roll-call', name: 'RollCall', component: () => import('../views/RollCall.vue') },
  { path: '/statistics', name: 'Statistics', component: () => import('../views/Statistics.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
