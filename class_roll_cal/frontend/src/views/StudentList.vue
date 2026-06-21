<template>
  <div>
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px">
      <h3>学生管理</h3>
      <div>
        <el-button type="primary" @click="showAddDialog">新增学生</el-button>
        <el-upload
          :action="'/api/students/import'"
          :show-file-list="false"
          :on-success="onImportSuccess"
          :on-error="onImportError"
          accept=".xlsx,.xls,.csv"
          style="display: inline-block; margin-left: 10px"
        >
          <el-button type="success">导入Excel/CSV</el-button>
        </el-upload>
      </div>
    </div>

    <el-input v-model="keyword" placeholder="搜索学号或姓名" style="width: 300px; margin-bottom: 16px" clearable @clear="fetchData" @keyup.enter="fetchData">
      <template #append>
        <el-button @click="fetchData">搜索</el-button>
      </template>
    </el-input>

    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="studentNo" label="学号" width="140" />
      <el-table-column prop="name" label="姓名" width="120" />
      <el-table-column prop="callCount" label="被点名次数" width="120" />
      <el-table-column prop="answerCount" label="回答正确次数" width="130" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.enabled === 1 ? 'success' : 'danger'">
            {{ row.enabled === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" min-width="200">
        <template #default="{ row }">
          <el-button size="small" @click="showEditDialog(row)">编辑</el-button>
          <el-button size="small" :type="row.enabled === 1 ? 'warning' : 'success'" @click="toggle(row.id)">
            {{ row.enabled === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-popconfirm title="确定删除？" @confirm="del(row.id)">
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="page"
      :page-size="size"
      :total="total"
      layout="total, prev, pager, next"
      @current-change="fetchData"
      style="margin-top: 16px; justify-content: flex-end"
    />

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑学生' : '新增学生'" width="450px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="学号" required>
          <el-input v-model="form.studentNo" placeholder="请输入学号" />
        </el-form-item>
        <el-form-item label="姓名" required>
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getStudents, addStudent, updateStudent, deleteStudent, toggleStudent } from '../api/student'

const tableData = ref([])
const loading = ref(false)
const keyword = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({ studentNo: '', name: '' })
let editId = null

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const { data } = await getStudents({ page: page.value, size: size.value, keyword: keyword.value })
    tableData.value = data.data.records
    total.value = data.data.total
  } finally {
    loading.value = false
  }
}

function showAddDialog() {
  isEdit.value = false
  form.value = { studentNo: '', name: '' }
  dialogVisible.value = true
}

function showEditDialog(row) {
  isEdit.value = true
  editId = row.id
  form.value = { studentNo: row.studentNo, name: row.name }
  dialogVisible.value = true
}

async function submit() {
  if (!form.value.studentNo || !form.value.name) {
    ElMessage.warning('学号和姓名不能为空')
    return
  }
  if (isEdit.value) {
    await updateStudent(editId, form.value)
    ElMessage.success('更新成功')
  } else {
    await addStudent(form.value)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  fetchData()
}

async function del(id) {
  await deleteStudent(id)
  ElMessage.success('已删除')
  fetchData()
}

async function toggle(id) {
  await toggleStudent(id)
  ElMessage.success('状态已切换')
  fetchData()
}

function onImportSuccess(res) {
  ElMessage.success(`导入完成：成功 ${res.data.success} 条，失败 ${res.data.fail} 条`)
  fetchData()
}

function onImportError(err) {
  ElMessage.error('导入失败：' + (err.message || '未知错误'))
}
</script>
