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
        <el-popconfirm title="确定批量删除选中的学生？" @confirm="batchDelete">
          <template #reference>
            <el-button type="danger" style="margin-left: 10px" :disabled="selectedIds.size === 0">
              删除选中 ({{ selectedIds.size }})
            </el-button>
          </template>
        </el-popconfirm>
      </div>
    </div>

    <el-input v-model="keyword" placeholder="搜索学号或姓名" style="width: 300px; margin-bottom: 16px" clearable @clear="fetchData" @keyup.enter="fetchData">
      <template #append>
        <el-button @click="fetchData">搜索</el-button>
      </template>
    </el-input>

    <el-table ref="tableRef" :data="tableData" row-key="id" border stripe v-loading="loading" @selection-change="onSelectionChange">
      <el-table-column type="selection" width="50" reserve-selection />
      <el-table-column prop="studentNo" label="学号" />
      <el-table-column prop="name" label="姓名" />
      <el-table-column prop="callCount" label="被点名次数" />
      <el-table-column prop="answerCount" label="回答正确次数" />
      <el-table-column label="状态">
        <template #default="{ row }">
          <el-tag :type="row.enabled === 1 ? 'success' : 'danger'">
            {{ row.enabled === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" />
      <el-table-column label="操作">
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
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { getStudents, addStudent, updateStudent, deleteStudent, deleteBatchStudents, toggleStudent } from '../api/student'

const tableRef = ref(null)
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
const selectedIds = ref(new Set())

function onSelectionChange(rows) {
  // 同步当前页的勾选状态到 selectedIds
  const currentPageIds = new Set(tableData.value.map(r => r.id))
  // 先从 selectedIds 中移除当前页的所有 id
  currentPageIds.forEach(id => selectedIds.value.delete(id))
  // 再将当前页勾选的 id 加回去
  rows.forEach(r => selectedIds.value.add(r.id))
  // 更新按钮显示数量（触发响应式）
  selectedIds.value = new Set(selectedIds.value)
}

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const { data } = await getStudents({ page: page.value, size: size.value, keyword: keyword.value })
    tableData.value = data.data.records
    total.value = data.data.total
    // 翻页后恢复之前勾选的行
    await nextTick()
    if (tableRef.value) {
      tableData.value.forEach(row => {
        if (selectedIds.value.has(row.id)) {
          tableRef.value.toggleRowSelection(row, true)
        }
      })
    }
  } catch (e) {
    ElMessage.error('加载学生列表失败')
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
  try {
    if (isEdit.value) {
      await updateStudent(editId, form.value)
      ElMessage.success('更新成功')
    } else {
      await addStudent(form.value)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

async function del(id) {
  try {
    await deleteStudent(id)
    ElMessage.success('已删除')
    fetchData()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '删除失败')
  }
}

async function batchDelete() {
  const ids = [...selectedIds.value]
  if (ids.length === 0) return
  try {
    await deleteBatchStudents(ids)
    ElMessage.success(`已批量删除 ${ids.length} 名学生`)
    selectedIds.value = new Set()
    tableRef.value?.clearSelection()
    fetchData()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '批量删除失败')
  }
}

async function toggle(id) {
  try {
    await toggleStudent(id)
    ElMessage.success('状态已切换')
    fetchData()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

function onImportSuccess(res) {
  ElMessage.success(`导入完成：成功 ${res.data.success} 条，失败 ${res.data.fail} 条`)
  fetchData()
}

function onImportError(err) {
  ElMessage.error('导入失败：' + (err.message || '未知错误'))
}
</script>
