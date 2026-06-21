import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 学生 CRUD
export const getStudents = (params) => api.get('/students', { params })
export const getStudent = (id) => api.get(`/students/${id}`)
export const addStudent = (data) => api.post('/students', data)
export const addBatchStudents = (data) => api.post('/students/batch', data)
export const updateStudent = (id, data) => api.put(`/students/${id}`, data)
export const deleteStudent = (id) => api.delete(`/students/${id}`)
export const toggleStudent = (id) => api.put(`/students/${id}/toggle`)
export const importStudents = (formData) => api.post('/students/import', formData, {
  headers: { 'Content-Type': 'multipart/form-data' }
})

// 点名
export const rollCall = (n = 3) => api.post('/roll-call', null, { params: { n } })
export const markCorrect = (id) => api.post(`/roll-call/${id}/correct`)
export const markWrong = (id) => api.post(`/roll-call/${id}/wrong`)
export const rollCallStatus = () => api.get('/roll-call/status')
export const resetRound = () => api.post('/roll-call/reset')

// 统计
export const getStatistics = () => api.get('/statistics')
