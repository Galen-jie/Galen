<template>
  <el-container class="layout-container">
    <el-header class="layout-header">
      <Header />
    </el-header>
    <el-main class="layout-main">
      <router-view />
    </el-main>
    <el-footer class="layout-footer">
      <Footer />
    </el-footer>
  </el-container>
</template>

<script setup>
import Header from './Header.vue'
import Footer from './Footer.vue'
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  width: 100%;
}

.layout-header {
  padding: 0;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
  width: 100%;
}

.layout-main {
  flex: 1;
  padding: 20px;
  background: #f5f5f5;
  width: 100%;
}

.layout-footer {
  padding: 0;
  background: #333;
  color: #fff;
  height: auto !important;
  width: 100%;
}
</style>