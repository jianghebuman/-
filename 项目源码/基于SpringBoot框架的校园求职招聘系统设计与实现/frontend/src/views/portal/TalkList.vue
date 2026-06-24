<template>
  <div class="talk-list portal-content">
    <div class="page-card head">
      <h2><el-icon><ChatRound /></el-icon> 校园宣讲会</h2>
      <p class="sub">近期企业到校宣讲安排，提前了解、提前准备</p>
    </div>

    <div class="list mt-20" v-loading="loading">
      <div class="talk-item" v-for="t in list" :key="t.id">
        <div class="date">
          <div class="day">{{ getDay(t.talkTime) }}</div>
          <div class="month">{{ getMonth(t.talkTime) }}</div>
        </div>
        <div class="body">
          <h3 class="title">{{ t.title }}</h3>
          <div class="meta">
            <span><el-icon><OfficeBuilding /></el-icon> {{ t.companyName || '主办方' }}</span>
            <span><el-icon><Clock /></el-icon> {{ formatDateTime(t.talkTime) }}</span>
            <span><el-icon><Location /></el-icon> {{ t.location }}</span>
          </div>
          <p class="content">{{ t.content }}</p>
          <div class="footer">
            <el-tag size="small" type="success" v-if="t.signCount">已有 {{ t.signCount }} 人报名</el-tag>
            <el-button type="primary" plain size="small" @click="onSign(t)">报名参加</el-button>
          </div>
        </div>
      </div>
      <el-empty v-if="!loading && list.length === 0" description="暂无宣讲会安排" />
    </div>

    <div class="pagination-wrap">
      <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total"
        layout="total, prev, pager, next" background @current-change="load" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatRound, OfficeBuilding, Clock, Location } from '@element-plus/icons-vue'
import { publicApi } from '@/api'

const query = reactive({ pageNum: 1, pageSize: 10 })
const list = ref([])
const total = ref(0)
const loading = ref(false)

const formatDateTime = (d) => d ? d.replace('T', ' ').substring(0, 16) : ''
const getDay = (d) => d ? new Date(d.replace(' ', 'T')).getDate() : ''
const getMonth = (d) => d ? `${new Date(d.replace(' ', 'T')).getMonth() + 1}月` : ''
const onSign = () => ElMessage.success('已报名，请准时参加')

const load = async () => {
  loading.value = true
  try {
    const res = await publicApi.talks(query)
    list.value = res.data.records; total.value = Number(res.data.total)
  } finally { loading.value = false }
}
onMounted(load)
</script>

<style scoped lang="scss">
.head h2 { color: var(--cr-text); .el-icon { vertical-align: middle; color: var(--cr-primary); } }
.head .sub { color: var(--cr-text-muted); margin-top: 6px; }
.talk-item { background: #fff; border: 1px solid var(--cr-border-soft); border-radius: var(--cr-radius); padding: clamp(16px, 1.5vw, 20px); margin-bottom: 16px; display: grid; grid-template-columns: 80px minmax(0, 1fr); gap: 20px; box-shadow: var(--cr-shadow-soft);
  .date { width: 80px; flex-shrink: 0; background: linear-gradient(135deg, var(--cr-primary), var(--cr-accent)); border-radius: var(--cr-radius-sm); color: #fff; text-align: center; padding: 14px 0;
    .day { font-size: 30px; font-weight: 600; line-height: 1; }
    .month { font-size: 14px; margin-top: 6px; }
  }
  .body { min-width: 0; }
  .title { color: var(--cr-text); margin-bottom: 10px; line-height: 1.4; }
  .meta { display: flex; flex-wrap: wrap; gap: 8px 16px; color: var(--cr-text-muted); font-size: 13px; margin-bottom: 10px; .el-icon { vertical-align: middle; color: var(--cr-primary); } }
  .content { color: var(--cr-text-soft); line-height: 1.7; margin-bottom: 14px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
  .footer { display: flex; justify-content: space-between; align-items: center; }
}

@media (max-width: 560px) {
  .talk-item {
    grid-template-columns: 1fr;
    gap: 14px;
  }

  .talk-item .date {
    width: 100%;
    display: flex;
    align-items: baseline;
    justify-content: center;
    gap: 8px;
  }

  .talk-item .footer {
    align-items: stretch;
    flex-direction: column;
    gap: 10px;
  }
}
</style>
