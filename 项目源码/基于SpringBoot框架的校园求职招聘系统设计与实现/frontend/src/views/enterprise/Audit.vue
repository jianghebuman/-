<template>
  <div class="page-container audit-page">
    <div class="page-card audit-card">
      <div class="header">
        <div>
          <h2>企业认证</h2>
          <p>认证通过后可提升岗位可信度，岗位审核通过率也更高。</p>
        </div>
      </div>
      <el-divider />
      <div class="audit-layout">
        <main class="audit-main">
          <el-alert :type="alert.type" :title="alert.title" :description="alert.desc" show-icon class="mb-20" />

          <section class="audit-form-panel">
            <div class="section-title">
              <h3>提交认证材料</h3>
              <p>营业执照号和图片是必填项，补充材料可用于提高审核效率。</p>
            </div>
            <el-form :model="form" label-width="180px" class="audit-form">
              <el-form-item label="统一社会信用代码 / 营业执照号">
                <el-input v-model="form.licenseNo" />
              </el-form-item>
              <el-form-item label="营业执照图片">
                <div class="upload-row">
                  <el-upload :show-file-list="false" accept="image/*" :http-request="uploadLicense">
                    <el-button>上传营业执照</el-button>
                  </el-upload>
                  <span v-if="form.licenseImg" class="file">{{ form.licenseImg }}</span>
                </div>
              </el-form-item>
              <el-form-item label="补充材料">
                <div class="upload-row">
                  <el-upload :show-file-list="false" accept="image/*" :http-request="uploadExtra">
                    <el-button>上传补充材料</el-button>
                  </el-upload>
                  <span v-if="form.extraImg" class="file">{{ form.extraImg }}</span>
                </div>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="submitting" @click="submit">提交认证</el-button>
              </el-form-item>
            </el-form>
          </section>

          <el-descriptions v-if="latest" title="最近一次认证记录" :column="2" border class="mb-20">
            <el-descriptions-item label="统一社会信用代码 / 营业执照号">{{ latest.licenseNo }}</el-descriptions-item>
            <el-descriptions-item label="审核状态">
              <el-tag :type="auditType(latest.auditStatus)">{{ auditText(latest.auditStatus) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="审核意见">{{ latest.auditRemark || '-' }}</el-descriptions-item>
            <el-descriptions-item label="提交时间">{{ latest.createTime }}</el-descriptions-item>
          </el-descriptions>

          <el-descriptions v-if="hasVerify(latest)" title="权威数据核验痕迹" :column="2" border class="mb-20">
            <el-descriptions-item label="核验来源">
              <el-link v-if="latest.verifySourceUrl" :href="latest.verifySourceUrl" target="_blank" type="primary">
                {{ latest.verifySource || '权威数据来源' }}
              </el-link>
              <span v-else>{{ latest.verifySource || '-' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="核验时间">{{ latest.verifyTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="核验结论">
              <el-tag :type="verifyType(latest.verifyResult)">{{ verifyText(latest.verifyResult) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="登记状态">{{ latest.verifyStatus || '-' }}</el-descriptions-item>
            <el-descriptions-item label="权威企业名称">{{ latest.verifyCompanyName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="权威信用代码">{{ latest.verifyCreditCode || '-' }}</el-descriptions-item>
            <el-descriptions-item label="核验说明" :span="2">{{ latest.verifyRemark || '-' }}</el-descriptions-item>
            <el-descriptions-item label="快照哈希" :span="2">{{ latest.verifySnapshotHash || '-' }}</el-descriptions-item>
          </el-descriptions>
        </main>

        <aside class="audit-side">
          <div class="side-head">
            <span>当前认证状态</span>
            <strong>{{ auditText(auditStatus) }}</strong>
          </div>
          <div class="side-list">
            <div>
              <span>材料要求</span>
              <strong>执照号 + 图片</strong>
            </div>
            <div>
              <span>审核流程</span>
              <strong>系统核验后人工复核</strong>
            </div>
            <div>
              <span>通过后权益</span>
              <strong>岗位可信度提升</strong>
            </div>
          </div>
          <p>如认证被驳回，请根据审核意见更新材料后重新提交。</p>
        </aside>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { enterpriseApi } from '@/api'
import { useUserStore } from '@/store/user'
import { refreshUnreadCounts } from '@/utils/unreadCounts'

const form = reactive({ licenseNo: '', licenseImg: '', extraImg: '' })
const userStore = useUserStore()
const latest = ref(null)
const auditStatus = ref(0)
const submitting = ref(false)

const auditText = (status) => ({ 0: '未认证', 1: '待审核', 2: '已通过', 3: '已驳回' }[status] || '未知')
const auditType = (status) => (status === 2 ? 'success' : status === 1 ? 'warning' : status === 3 ? 'danger' : 'info')
const verifyText = (status) => ({ 0: '未核验', 1: '一致', 2: '不一致', 3: '未接入或异常' }[status] || '未知')
const verifyType = (status) => (status === 1 ? 'success' : status === 2 ? 'danger' : status === 3 ? 'warning' : 'info')
const hasVerify = (row) => !!row && row.verifyResult !== undefined && row.verifyResult !== null

const alert = computed(() => ({
  0: { type: 'info', title: '尚未认证', desc: '请提交营业执照等材料，系统将先进行权威数据核验，不一致或未接入时进入人工审核。' },
  1: { type: 'warning', title: '认证审核中', desc: '系统已保留核验痕迹，管理员正在复核，请耐心等待。' },
  2: { type: 'success', title: '认证已通过', desc: '企业已通过认证，可正常开展招聘，后台仍支持人工复核。' },
  3: { type: 'error', title: '认证被驳回', desc: '请根据审核意见修改材料后重新提交。' }
}[auditStatus.value]))

const load = async () => {
  const res = await enterpriseApi.auditStatus()
  auditStatus.value = res.data.auditStatus ?? 0
  latest.value = res.data.latestAudit
  if (latest.value) {
    form.licenseNo = latest.value.licenseNo
    form.licenseImg = latest.value.licenseImg
    form.extraImg = latest.value.extraImg
  }
}

const up = async (file) => {
  const fd = new FormData()
  fd.append('file', file)
  return (await enterpriseApi.uploadAuditMaterial(fd)).data
}

const uploadLicense = async ({ file }) => {
  form.licenseImg = await up(file)
  ElMessage.success('上传成功')
}

const uploadExtra = async ({ file }) => {
  form.extraImg = await up(file)
  ElMessage.success('上传成功')
}

const submit = async () => {
  if (!form.licenseNo || !form.licenseImg) {
    return ElMessage.warning('请填写统一社会信用代码 / 营业执照号并上传执照图片')
  }
  submitting.value = true
  try {
    await enterpriseApi.submitAudit(form)
    ElMessage.success('认证申请已提交，系统将先进行权威数据核验')
    await refreshUnreadCounts(userStore, { includeChat: false }).catch(() => {})
    load()
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<style scoped lang="scss">
.audit-page {
  width: min(150rem, calc(100% - clamp(1rem, 3vw, 3rem)));
}

.audit-card {
  min-height: calc(100dvh - 8.5rem);
  padding: clamp(24px, 2vw, 34px);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;

  h2 {
    margin-bottom: 6px;
    color: var(--cr-text);
  }

  p {
    color: var(--cr-text-muted);
    line-height: 1.6;
  }
}

.audit-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(26rem, 32rem);
  gap: clamp(24px, 3vw, 52px);
  align-items: start;
}

.audit-main {
  min-width: 0;
}

.audit-form-panel {
  margin-bottom: 20px;
  padding: 24px;
  border: 1px solid var(--cr-border-soft);
  border-radius: 12px;
  background: var(--cr-surface-soft);
}

.section-title {
  margin-bottom: 18px;

  h3 {
    margin-bottom: 6px;
    color: var(--cr-text);
    font-size: 22px;
  }

  p {
    color: var(--cr-text-muted);
    font-size: 15px;
    line-height: 1.7;
  }
}

.audit-form {
  max-width: 58rem;
}

.upload-row {
  min-width: 0;
  width: 100%;
  display: flex;
  align-items: center;
  gap: 12px;
}

.file {
  min-width: 0;
  color: var(--cr-text-soft);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.audit-side {
  position: sticky;
  top: 20px;
  min-height: 34rem;
  padding: 28px;
  border: 1px solid rgba(37, 99, 235, 0.14);
  border-radius: 12px;
  background: linear-gradient(180deg, #f8fbff, #fff);
  box-shadow: var(--cr-shadow-soft);

  p {
    margin-top: 22px;
    color: var(--cr-text-soft);
    font-size: 15px;
    line-height: 1.8;
  }
}

.side-head {
  padding-bottom: 20px;
  border-bottom: 1px solid var(--cr-border-soft);

  span {
    display: block;
    margin-bottom: 8px;
    color: var(--cr-text-muted);
    font-size: 15px;
  }

  strong {
    color: var(--cr-primary);
    font-size: 30px;
    line-height: 1.25;
  }
}

.side-list {
  display: grid;
  gap: 14px;
  margin-top: 22px;

  div {
    min-width: 0;
    padding: 18px;
    border: 1px solid var(--cr-border-soft);
    border-radius: 10px;
    background: #fff;
  }

  span {
    display: block;
    margin-bottom: 8px;
    color: var(--cr-text-muted);
    font-size: 14px;
  }

  strong {
    color: var(--cr-text);
    font-size: 17px;
    line-height: 1.5;
  }
}

@media (max-width: 980px) {
  .audit-layout {
    grid-template-columns: 1fr;
  }

  .audit-side {
    position: static;
    min-height: 0;
  }

  .upload-row {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
