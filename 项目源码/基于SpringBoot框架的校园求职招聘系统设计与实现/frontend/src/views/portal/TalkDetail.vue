<template>
  <div class="activity-detail portal-content" v-loading="loading">
    <div v-if="talk" class="page-card detail-card">
      <h1 class="title">{{ talk.title }}</h1>
      <div class="meta">
        <span><el-icon><OfficeBuilding /></el-icon> {{ talk.companyName || '主办方' }}</span>
        <span><el-icon><Clock /></el-icon> {{ formatDateTime(talk.talkTime) }}</span>
        <span><el-icon><Location /></el-icon> {{ talk.location || '待定' }}</span>
        <span><el-icon><User /></el-icon> 已有 {{ talk.signCount || 0 }} 人报名</span>
      </div>
      <el-divider />
      <div class="content">{{ talk.content }}</div>
      <el-divider />
      <div class="actions">
        <el-button @click="$router.push('/talks')"><el-icon><Back /></el-icon> 返回列表</el-button>
        <el-button type="primary" @click="onSign">报名参加</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Back, Clock, Location, OfficeBuilding, User } from '@element-plus/icons-vue'
import { activityApi, noticeApi, publicApi } from '@/api'
import { useUserStore } from '@/store/user'
import { showLoginPrompt, showSignupSuccessPrompt } from '@/utils/loginPrompt'

const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const talk = ref(null)
const formatDateTime = (d) => d ? d.replace('T', ' ').substring(0, 16) : ''

const load = async () => {
  loading.value = true
  try {
    const res = await publicApi.talkDetail(route.params.id)
    talk.value = res.data
  } finally { loading.value = false }
}

const onSign = async () => {
  if (!userStore.isLogin) {
    showLoginPrompt('登录后才能报名参加宣讲会。')
    return
  }
  const res = await activityApi.sign(1, talk.value.id)
  showSignupSuccessPrompt(`${res.message || '报名成功，请准时参加'}。时间：${formatDateTime(talk.value.talkTime) || '待定'}，地点：${talk.value.location || '待定'}。`)
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
.content { color: var(--cr-text); font-size: 1rem; line-height: 1.9; min-height: 12.5rem; white-space: pre-line; }
.actions { display: flex; justify-content: center; gap: .75rem; flex-wrap: wrap; }
</style>
