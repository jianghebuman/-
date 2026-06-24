<template>
  <div class="ent-list portal-content">
    <div class="page-card head">
      <h2><el-icon><OfficeBuilding /></el-icon> 名企推荐</h2>
      <p class="sub">汇聚优质企业，发现你的理想雇主</p>
    </div>

    <div class="grid mt-20" v-loading="loading">
      <div class="ent-card" v-for="e in list" :key="e.id" @click="$router.push(`/enterprise/${e.id}`)">
        <el-avatar :size="72" :src="e.logo" shape="square" class="logo"><el-icon size="32"><OfficeBuilding /></el-icon></el-avatar>
        <div class="info">
          <h3 class="name">{{ e.companyName }}</h3>
          <p class="meta">
            <span v-if="e.industry">{{ e.industry }}</span>
            <span class="sep" v-if="e.industry && e.scale">·</span>
            <span v-if="e.scale">{{ e.scale }}</span>
          </p>
          <p class="city" v-if="e.city"><el-icon><Location /></el-icon> {{ e.city }}</p>
          <p class="intro">{{ e.intro || '该企业暂未填写介绍' }}</p>
        </div>
      </div>
      <el-empty v-if="!loading && list.length === 0" description="暂无企业" style="grid-column: 1 / -1;" />
    </div>

    <div class="pagination-wrap">
      <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total"
        layout="total, prev, pager, next" background @current-change="load" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { OfficeBuilding, Location } from '@element-plus/icons-vue'
import { publicApi } from '@/api'

const query = reactive({ pageNum: 1, pageSize: 12 })
const list = ref([])
const total = ref(0)
const loading = ref(false)

const load = async () => {
  loading.value = true
  try {
    const res = await publicApi.enterprises(query)
    list.value = res.data.records
    total.value = Number(res.data.total)
  } finally { loading.value = false }
}
onMounted(load)
</script>

<style scoped lang="scss">
.head h2 { color: var(--cr-text); .el-icon { vertical-align: middle; color: var(--cr-primary); } }
.head .sub { color: var(--cr-text-muted); margin-top: 6px; }
.grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(min(100%, 340px), 1fr)); gap: 16px; }
.ent-card { background: rgba(255,255,255,.96); border: 1px solid var(--cr-border-soft); border-radius: var(--cr-radius); padding: clamp(16px, 1.5vw, 22px); display: flex; gap: 16px; cursor: pointer; transition: border-color .2s ease, box-shadow .2s ease, transform .2s ease;
  &:hover { border-color: rgba(37,99,235,.28); box-shadow: var(--cr-shadow); transform: translateY(-2px); }
  .logo { flex-shrink: 0; }
  .info { flex: 1; min-width: 0; }
  .name { font-size: 17px; color: var(--cr-text); margin-bottom: 6px; line-height: 1.4; }
  .meta { font-size: 13px; color: var(--cr-text-muted); margin-bottom: 4px; .sep { margin: 0 6px; } }
  .city { font-size: 13px; color: var(--cr-text-soft); margin-bottom: 8px; .el-icon { vertical-align: middle; color: var(--cr-primary); } }
  .intro { color: var(--cr-text-soft); font-size: 13px; line-height: 1.6; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
}

@media (max-width: 460px) {
  .ent-card {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
