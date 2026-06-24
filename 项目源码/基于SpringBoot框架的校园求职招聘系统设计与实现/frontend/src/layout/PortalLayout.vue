<template>
  <div class="portal-layout">
    <el-header class="portal-header">
      <div class="portal-content flex-between" style="height: 100%;">
        <div class="logo" @click="$router.push('/')">
          <el-icon size="28"><School /></el-icon>
          <span>校园求职招聘系统</span>
        </div>
        <el-menu mode="horizontal" :default-active="$route.path" router class="nav" :ellipsis="false">
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/jobs">职位</el-menu-item>
          <el-menu-item index="/enterprises">企业</el-menu-item>
          <el-menu-item index="/talks">宣讲会</el-menu-item>
          <el-menu-item index="/fairs">招聘会</el-menu-item>
          <el-menu-item index="/news">资讯</el-menu-item>
          <el-menu-item index="/forum">社区</el-menu-item>
        </el-menu>
        <div class="user-area">
          <template v-if="userStore.isLogin">
            <el-dropdown trigger="click" @command="onCommand">
              <button class="user-link" type="button">
                <el-avatar :size="28" :src="userStore.avatar"><el-icon><User /></el-icon></el-avatar>
                <span>{{ userStore.name || userStore.username }}</span>
              </button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="center">个人中心</el-dropdown-item>
                  <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button text @click="$router.push('/login')">登录</el-button>
            <el-button type="primary" @click="$router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </el-header>
    <el-main class="portal-main">
      <router-view />
    </el-main>
    <el-footer class="portal-footer">
      <div class="portal-content">
        <p>© 2026 校园求职招聘系统 | 基于 Spring Boot 框架的毕业设计</p>
        <p>Tech: Spring Boot · MyBatis-Plus · MySQL · Vue 3 · Element Plus · ECharts</p>
      </div>
    </el-footer>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { School, User } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const onCommand = (cmd) => {
  if (cmd === 'logout') {
    ElMessageBox.confirm('确定退出登录？', '提示', { type: 'warning' }).then(() => {
      userStore.logout(); router.push('/login')
    }).catch(() => {})
  } else if (cmd === 'center') {
    if (userStore.role === 'STUDENT') router.push('/student')
    else if (userStore.role === 'ENTERPRISE') router.push('/enterprise')
    else router.push('/admin')
  }
}
</script>

<style scoped lang="scss">
.portal-layout { min-height: 100dvh; display: flex; flex-direction: column; }
.portal-header {
  position: sticky;
  top: 0;
  z-index: 20;
  height: auto;
  min-height: 68px;
  padding: 0;
  background: rgba(255, 255, 255, 0.88);
  border-bottom: 1px solid var(--cr-border-soft);
  box-shadow: 0 8px 24px rgba(22, 38, 68, 0.06);
  backdrop-filter: blur(14px);
}
.portal-header .portal-content {
  gap: clamp(12px, 2vw, 28px);
  min-height: 68px;
  padding: 8px 0;
}
.logo { display: flex; align-items: center; gap: 8px; cursor: pointer; font-size: 18px; font-weight: 700; color: var(--cr-text); min-width: max-content; }
.logo .el-icon { color: var(--cr-primary); }
.nav { flex: 1; border-bottom: none; margin-left: 0; min-width: 0; overflow-x: auto; }
.nav :deep(.el-menu--horizontal) { border-bottom: 0; }
.nav :deep(.el-menu-item) { min-width: auto; padding: 0 clamp(10px, 1.5vw, 20px); justify-content: center; color: var(--cr-text-soft); font-weight: 650; }
.nav :deep(.el-menu-item.is-active) { color: var(--cr-primary); border-bottom-color: var(--cr-primary); }
.user-area { display: flex; align-items: center; gap: 10px; min-width: max-content; justify-content: flex-end; }
.user-link { appearance: none; border: 0; background: transparent; padding: 6px 8px; display: flex; align-items: center; gap: 6px; cursor: pointer; color: var(--cr-text); font: inherit; border-radius: 8px; outline: none; }
.user-link:hover { background: var(--cr-surface-soft); }
.user-link:focus-visible { box-shadow: 0 0 0 3px rgba(37,99,235,.18); }
.user-link span:last-child { max-width: 130px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
:deep(.el-tooltip__trigger:focus-visible) { outline: none; }
:deep(.el-tooltip__trigger:focus) { outline: none; }
.portal-main { flex: 1; background: transparent; padding: clamp(14px, 2vw, 26px) 0; }
.portal-footer { background: #172033; color: #b9c5d6; text-align: center; padding: 22px; height: auto; font-size: 13px; line-height: 1.8; }

@media (min-width: 1400px) {
  .nav :deep(.el-menu-item) { padding: 0 24px; }
}

@media (max-width: 1100px) {
  .portal-header .portal-content {
    flex-wrap: wrap;
    justify-content: flex-start;
  }
  .logo { font-size: 17px; }
  .nav { order: 3; flex-basis: 100%; }
  .nav :deep(.el-menu-item) { padding: 0 14px; }
  .user-area { margin-left: auto; }
}

@media (max-width: 640px) {
  .logo span { max-width: 52vw; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  .portal-header .portal-content { gap: 8px 12px; }
  .user-area :deep(.el-button) { padding: 8px 10px; }
}
</style>
