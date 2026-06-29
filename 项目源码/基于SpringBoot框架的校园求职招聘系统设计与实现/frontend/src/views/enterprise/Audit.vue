<template>
  <div class="page-container">
    <div class="page-card">
      <div class="header">
        <h2>企业认证</h2>
        <p>认证通过后可提升岗位可信度，岗位审核通过率也更高。</p>
      </div>
      <el-divider />
      <el-alert :type="alert.type" :title="alert.title" :description="alert.desc" show-icon class="mb-20" />

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

      <el-form :model="form" label-width="180px" style="max-width: 780px">
        <el-form-item label="统一社会信用代码 / 营业执照号">
          <el-input v-model="form.licenseNo" />
        </el-form-item>
        <el-form-item label="营业执照图片">
          <el-upload :show-file-list="false" accept="image/*" :http-request="uploadLicense">
            <el-button>上传营业执照</el-button>
          </el-upload>
          <span v-if="form.licenseImg" class="file">{{ form.licenseImg }}</span>
        </el-form-item>
        <el-form-item label="补充材料">
          <el-upload :show-file-list="false" accept="image/*" :http-request="uploadExtra">
            <el-button>上传补充材料</el-button>
          </el-upload>
          <span v-if="form.extraImg" class="file">{{ form.extraImg }}</span>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="submit">提交认证</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { enterpriseApi } from '@/api'

const form = reactive({ licenseNo: '', licenseImg: '', extraImg: '' })
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
    load()
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>
