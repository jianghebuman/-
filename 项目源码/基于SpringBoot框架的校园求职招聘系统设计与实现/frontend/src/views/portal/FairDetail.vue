<template>
  <div class="activity-detail portal-content" v-loading="loading">
    <div v-if="fair" class="page-card detail-card">
      <h1 class="title">{{ fair.title }}</h1>
      <div class="meta">
        <span><el-icon><Clock /></el-icon> {{ formatDateTime(fair.fairTime) }}</span>
        <span><el-icon><Location /></el-icon> {{ fair.location || '待定' }}</span>
        <span><el-icon><Trophy /></el-icon> {{ fair.host || '校就业指导中心' }}</span>
      </div>
      <div class="stats">
        <div><b>{{ fair.companyCount || 0 }}</b><span>参会企业</span></div>
        <div><b>{{ fair.jobCount || 0 }}</b><span>提供岗位</span></div>
        <div><b>{{ fair.signCount || 0 }}</b><span>报名人数</span></div>
      </div>
      <el-divider />
      <div class="content">{{ fair.content }}</div>
      <el-divider />
      <div class="actions">
        <el-button @click="$router.push('/fairs')"><el-icon><Back /></el-icon> 返回列表</el-button>
        <el-button type="primary" @click="onSign">立即报名</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Back, Clock, Location, Trophy } from '@element-plus/icons-vue'
import { activityApi, noticeApi, publicApi } from '@/api'
import { useUserStore } from '@/store/user'
import { showLoginPrompt, showSignupSuccessPrompt } from '@/utils/loginPrompt'

const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const fair = ref(null)
const formatDateTime = (d) => d ? d.replace('T', ' ').substring(0, 16) : ''

const load = async () => {
  loading.value = true
  try {
    const res = await publicApi.fairDetail(route.params.id)
    fair.value = res.data
  } finally { loading.value = false }
}

const onSign = async () => {
  if (!userStore.isLogin) {
    showLoginPrompt('登录后才能报名参加招聘会。')
    return
  }
  const res = await activityApi.sign(2, fair.value.id)
  showSignupSuccessPrompt(`${res.message || '报名成功，请准时参加'}。时间：${formatDateTime(fair.value.fairTime) || '待定'}，地点：${fair.value.location || '待定'}。`)
  const unreadRes = await noticeApi.unread()
  userStore.setUnreadCounts(Number(unreadRes.data || 0), userStore.unreadChatCount)
  load()
}

onMounted(load)
</script>

<style scoped lang="scss">
.detail-card { max-width: 56.25rem; margin: 0 auto; padding: clamp(1.25rem, 4vw, 3.75rem); }
.title { text-align: center; color: var(--cr-text); margin-bottom: 1rem; line-height: 1.4; }
.meta { display: flex; justify-content: center; gap: .75rem 1.25rem; flex-wrap: wrap; color: var(--cr-text-muted); font-size: .8125rem; .el-icon { vertical-align: middle; margin-right: .125rem; color: var(--cr-primary); } }
.stats { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: .75rem; margin: 1.5rem auto 0; max-width: 36rem; padding: .875rem; background: var(--cr-surface-soft); border-radius: var(--cr-radius-sm);
  div { text-align: center; }
  b { display: block; color: var(--cr-danger); font-size: clamp(1.25rem, 2vw, 1.5rem); }
  span { color: var(--cr-text-muted); font-size: .75rem; }
}
.content { color: var(--cr-text); font-size: 1rem; line-height: 1.9; min-height: 12.5rem; white-space: pre-line; }
.actions { display: flex; justify-content: center; gap: .75rem; flex-wrap: wrap; }
</style>
