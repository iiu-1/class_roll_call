<template>
  <div>
    <h3>统计报表</h3>

    <div class="stat-row">
      <el-card class="stat-card-item">
        <div class="stat-card">
          <div class="stat-value">{{ stats.totalStudents }}</div>
          <div class="stat-label">学生总数</div>
        </div>
      </el-card>
      <el-card class="stat-card-item">
        <div class="stat-card">
          <div class="stat-value">{{ stats.enabledStudents }}</div>
          <div class="stat-label">启用学生</div>
        </div>
      </el-card>
      <el-card class="stat-card-item">
        <div class="stat-card">
          <div class="stat-value">{{ stats.totalCallCount }}</div>
          <div class="stat-label">总点名次数</div>
        </div>
      </el-card>
      <el-card class="stat-card-item">
        <div class="stat-card">
          <div class="stat-value">{{ stats.totalAnswerCount }}</div>
          <div class="stat-label">总答对次数</div>
        </div>
      </el-card>
      <el-card class="stat-card-item">
        <div class="stat-card">
          <div class="stat-value">{{ ((stats.overallRate ?? 0) * 100).toFixed(1) }}%</div>
          <div class="stat-label">整体正确率</div>
        </div>
      </el-card>
    </div>

    <el-card v-loading="loading">
      <h4 style="margin-top: 0">学生明细（按正确率排序）</h4>
      <el-table :data="stats.details" border stripe>
        <el-table-column prop="studentNo" label="学号" width="140" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="callCount" label="被点名次数" width="120" />
        <el-table-column prop="answerCount" label="回答正确次数" width="130" />
        <el-table-column label="正确率" width="120">
          <template #default="{ row }">
            <el-progress :percentage="Math.round(row.rate * 100)" :color="rateColor(row.rate)" />
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getStatistics } from '../api/student'

const stats = ref({
  totalStudents: 0,
  enabledStudents: 0,
  totalCallCount: 0,
  totalAnswerCount: 0,
  overallRate: 0,
  details: []
})
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const { data } = await getStatistics()
    stats.value = data.data
  } catch (e) {
    ElMessage.error('统计数据加载失败')
  } finally {
    loading.value = false
  }
})

function rateColor(rate) {
  if (rate >= 0.6) return '#67c23a'
  if (rate >= 0.3) return '#e6a23c'
  return '#f56c6c'
}
</script>

<style scoped>
.stat-row {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}
.stat-card-item {
  flex: 1;
  min-width: 0;
}
.stat-card { text-align: center; padding: 10px; }
.stat-value { font-size: 32px; font-weight: bold; color: #409eff; }
.stat-label { font-size: 14px; color: #909399; margin-top: 4px; }
</style>
