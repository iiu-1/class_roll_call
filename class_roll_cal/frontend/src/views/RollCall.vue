<template>
  <div>
    <h3>课堂点名</h3>

    <el-card style="max-width: 600px; margin: 20px auto">
      <div style="margin-bottom: 16px">
        <span>阈值 n：</span>
        <el-input-number v-model="threshold" :min="1" :max="20" />
        <span style="margin-left: 8px; color: #909399">（超过 n 人未答对切换高分模式）</span>
      </div>

      <div style="text-align: center; margin: 30px 0">
        <div v-if="status?.student" class="result-card">
          <div class="result-label">本次点名</div>
          <div class="result-name">{{ status.student.name }}</div>
          <div class="result-sno">{{ status.student.studentNo }}</div>
          <div class="result-stats">
            被点名 {{ status.student.callCount }} 次 | 答对 {{ status.student.answerCount }} 次
          </div>
          <el-tag v-if="status.highScoreMode" type="warning" style="margin-top: 8px">高分补抽模式</el-tag>
          <el-tag v-if="status.fullRoundExhausted" type="info" style="margin-top: 8px; margin-left: 4px">全员已点过一轮</el-tag>
          <div style="margin-top: 20px">
            <el-button type="success" size="large" @click="correct">✅ 回答正确</el-button>
            <el-button type="danger" size="large" @click="wrong">❌ 回答错误</el-button>
          </div>
        </div>
        <div v-else class="empty-state">点击「点名」按钮开始</div>
      </div>

      <div style="text-align: center">
        <el-button type="primary" size="large" @click="doRollCall" :loading="rolling">
          🎲 点名
        </el-button>
        <el-button size="large" @click="doReset">🔄 换题</el-button>
      </div>

      <div style="margin-top: 16px; color: #909399; text-align: center">
        本轮已点 {{ status?.currentRoundCount || 0 }} 人 |
        答错 {{ status?.currentWrongCount || 0 }} 人 |
        阈值 {{ status?.threshold || threshold }}
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { rollCall, markCorrect, markWrong, rollCallStatus, resetRound } from '../api/student'

const threshold = ref(3)
const status = ref(null)
const rolling = ref(false)

onMounted(async () => {
  try {
    const { data } = await rollCallStatus()
    status.value = data.data
  } catch (e) { /* ignore */ }
})

async function doRollCall() {
  rolling.value = true
  try {
    const { data } = await rollCall(threshold.value)
    status.value = data.data
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '点名失败')
  } finally {
    rolling.value = false
  }
}

async function correct() {
  try {
    const { data } = await markCorrect(status.value.student.id)
    status.value = data.data
    ElMessage.success('回答正确！')
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

async function wrong() {
  try {
    const { data } = await markWrong(status.value.student.id)
    status.value = data.data
    ElMessage.warning('回答错误！')
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

async function doReset() {
  await resetRound()
  status.value = null
  ElMessage.info('已换题')
}
</script>

<style scoped>
.result-card {
  background: #f5f7fa;
  border-radius: 12px;
  padding: 30px;
  margin: 10px 0;
}
.result-label { color: #909399; font-size: 14px; margin-bottom: 8px; }
.result-name { font-size: 48px; font-weight: bold; color: #303133; }
.result-sno { font-size: 18px; color: #606266; margin-top: 4px; }
.result-stats { font-size: 13px; color: #909399; margin-top: 8px; }
.empty-state { color: #c0c4cc; font-size: 18px; padding: 40px; }
</style>
