<template>
  <div class="layout-root">
    <header class="layout-header">
      <Header />
    </header>
    <div class="layout-body">
      <div class="layout-content">
        <main class="layout-main">
          <router-view />
        </main>
        <footer class="layout-footer">
          <Footer />
        </footer>
      </div>
      <AssistantFloatBall />
    </div>
  </div>
</template>

<script setup>
import Header from './Header.vue'
import Footer from './Footer.vue'
import AssistantFloatBall from '@/components/assistant/AssistantFloatBall.vue'
</script>

<style scoped>
.layout-root {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  width: 100%;
}

.layout-header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
  width: 100%;
}

.layout-body {
  flex: 1;
  display: flex;
  align-items: stretch;
}

.layout-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.layout-main {
  flex: 1;
  padding: 20px;
  background: #fff;
  width: 100%;
}

.layout-footer {
  background: #333;
  color: #fff;
  width: 100%;
}
</style>
