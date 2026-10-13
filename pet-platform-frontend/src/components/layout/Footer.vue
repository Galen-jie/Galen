<template>
  <div class="footer">
    <div class="footer-container">
      <div class="footer-content">
        <div class="footer-section">
          <h4>关于我们</h4>
          <p>宠物救助平台致力于为流浪动物寻找温暖的家，让每一个生命都能被温柔以待。</p>
        </div>
        <div class="footer-section">
          <h4>联系我们</h4>
          <p>邮箱: contact@pet-rescue.com</p>
          <p>电话: 400-123-4567</p>
        </div>
        <div class="footer-section">
          <h4>快速链接</h4>
          <div class="footer-links">
            <router-link to="/pet">宠物救助</router-link>
            <router-link to="/goods">宠物商城</router-link>
            <router-link to="/seckill">秒杀专区</router-link>
          </div>
        </div>
      </div>
      <div class="footer-bottom">
        <p>© 2026 宠物救助平台. All rights reserved.</p>
      </div>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.footer {
  width: 100%;
  padding: 40px 0 20px;
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.footer-content {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 40px;
  margin-bottom: 30px;
}

.footer-section h4 {
  font-size: 18px;
  margin-bottom: 15px;
  color: #fff;
}

.footer-section p {
  color: #ccc;
  line-height: 1.8;
  margin: 5px 0;
}

.footer-links {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.footer-links a {
  color: #ccc;
  text-decoration: none;
  transition: color 0.3s;
}

.footer-links a:hover {
  color: #409eff;
}

.footer-bottom {
  border-top: 1px solid #555;
  padding-top: 20px;
  text-align: center;
  color: #999;
}

@media (max-width: 768px) {
  .footer-content {
    grid-template-columns: 1fr;
    gap: 30px;
  }
}
</style>