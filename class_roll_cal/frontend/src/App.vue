<template>
  <el-container style="min-height: 100vh">
    <el-header style="background: #409eff; display: flex; align-items: center; padding: 0 20px">
      <h2 style="color: #fff; margin: 0">📋 课堂点名系统</h2>
      <el-menu
        :default-active="route.path"
        mode="horizontal"
        style="margin-left: 40px; border-bottom: none; flex: 1"
        background-color="#409eff"
        text-color="#fff"
        active-text-color="#ffd04b"
        router
      >
        <el-menu-item index="/students">学生管理</el-menu-item>
        <el-menu-item index="/roll-call">点名</el-menu-item>
        <el-menu-item index="/statistics">统计</el-menu-item>
      </el-menu>
    </el-header>
    <el-main>
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { useRoute } from 'vue-router'
const route = useRoute()
</script>
