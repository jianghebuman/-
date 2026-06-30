<template>
  <div class="page-container profile-page">
    <div class="page-card profile-card">
      <div class="header">
        <div>
          <h2>企业资料</h2>
          <p>展示在企业主页中，是学生了解企业的重要信息。</p>
        </div>
      </div>
      <el-divider />
      <div class="profile-body">
        <el-upload class="identity-uploader" :show-file-list="false" :http-request="uploadLogo" accept="image/*">
          <div class="identity-upload">
            <div class="identity-preview">
              <el-avatar :size="128" :src="form.logo" shape="square" class="logo-avatar">
                <el-icon><OfficeBuilding /></el-icon>
              </el-avatar>
              <span class="upload-mark"><el-icon><UploadFilled /></el-icon></span>
            </div>
            <div class="identity-copy">
              <span class="upload-label">{{ form.logo ? '更换 Logo' : '上传 Logo' }}</span>
              <span class="upload-sub">会显示在企业主页和岗位列表</span>
            </div>
            <div class="identity-hints">
              <div><span>主页展示</span><strong>企业头像</strong></div>
              <div><span>岗位列表</span><strong>品牌识别</strong></div>
              <div><span>建议尺寸</span><strong>正方形图片</strong></div>
            </div>
          </div>
        </el-upload>
        <el-form :model="form" label-width="110px" class="profile-form">
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :xl="8"><el-form-item label="企业名称"><el-input v-model="form.companyName" /></el-form-item></el-col>
            <el-col :xs="24" :sm="12" :xl="8"><el-form-item label="行业"><el-input v-model="form.industry" /></el-form-item></el-col>
            <el-col :xs="24" :sm="12" :xl="8"><el-form-item label="规模"><el-input v-model="form.scale" /></el-form-item></el-col>
            <el-col :xs="24" :sm="12" :xl="8"><el-form-item label="性质"><el-input v-model="form.nature" /></el-form-item></el-col>
            <el-col :xs="24" :sm="12" :xl="8"><el-form-item label="城市"><el-input v-model="form.city" /></el-form-item></el-col>
            <el-col :xs="24" :sm="12" :xl="8"><el-form-item label="联系人"><el-input v-model="form.contactName" /></el-form-item></el-col>
            <el-col :xs="24" :sm="12" :xl="8"><el-form-item label="联系电话"><el-input v-model="form.contactPhone" /></el-form-item></el-col>
            <el-col :xs="24" :sm="12" :xl="8"><el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item></el-col>
            <el-col :span="24"><el-form-item label="地址"><el-input v-model="form.address" /></el-form-item></el-col>
            <el-col :span="24"><el-form-item label="福利标签"><el-input v-model="form.welfare" placeholder="用逗号分隔，如 五险一金,年终奖" /></el-form-item></el-col>
            <el-col :span="24"><el-form-item label="企业简介"><el-input v-model="form.intro" type="textarea" :rows="5" maxlength="1000" show-word-limit /></el-form-item></el-col>
          </el-row>
          <el-form-item class="form-actions">
            <el-button type="primary" :loading="saving" @click="save">保存资料</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { OfficeBuilding, UploadFilled } from '@element-plus/icons-vue'
import { enterpriseApi } from '@/api'
import { useUserStore } from '@/store/user'

const form = reactive({})
const saving = ref(false)
const store = useUserStore()

const load = async () => Object.assign(form, (await enterpriseApi.profile()).data || {})
const save = async () => {
  saving.value = true
  try {
    await enterpriseApi.updateProfile(form)
    ElMessage.success('保存成功')
  } finally {
    saving.value = false
  }
}

const uploadLogo = async ({ file }) => {
  const fd = new FormData()
  fd.append('file', file)
  const res = await enterpriseApi.uploadLogo(fd)
  form.logo = res.data
  store.setAvatar(res.data)
  ElMessage.success('Logo上传成功')
}

onMounted(load)
</script>

<style scoped lang="scss">
.profile-page {
  width: 100%;
  max-width: 1680px;
  margin: 0 auto;
}

.profile-card {
  min-height: calc(100dvh - 8.5rem);
  padding: clamp(24px, 2vw, 34px);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  h2 { margin-bottom: 6px; }
  p { color: var(--cr-text-muted); }
}

.profile-body {
  display: grid;
  grid-template-columns: minmax(340px, 420px) minmax(0, 1fr);
  gap: clamp(28px, 3vw, 48px);
  align-items: start;
}

.identity-uploader {
  width: 100%;

  :deep(.el-upload) {
    display: block;
    outline: none;
  }
}

.identity-upload {
  width: 100%;
  min-width: 0;
  min-height: 520px;
  padding: 34px 28px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  gap: 20px;
  border: 1px solid var(--cr-border);
  border-radius: 12px;
  background: linear-gradient(180deg, #f8fbff 0%, #ffffff 100%);
  cursor: pointer;
  transition: border-color .2s ease, box-shadow .2s ease;

  &:hover {
    border-color: var(--cr-primary);
    box-shadow: 0 8px 18px rgba(37, 99, 235, .14);
  }
}

.identity-preview {
  position: relative;
  width: 128px;
  height: 128px;
  flex: 0 0 128px;
}

.logo-avatar {
  width: 128px !important;
  height: 128px !important;
  border: 2px solid #fff;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--cr-primary-soft), var(--cr-accent-soft));
  color: var(--cr-primary);
  font-size: 44px;
  box-shadow: inset 0 0 0 1px rgba(37, 99, 235, .16), 0 6px 16px rgba(31, 45, 61, .12);
}

.upload-mark {
  position: absolute;
  right: -6px;
  bottom: -6px;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #fff;
  border-radius: 50%;
  background: var(--cr-primary);
  color: #fff;
  box-shadow: 0 4px 10px rgba(37, 99, 235, .3);
}

.identity-copy {
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.upload-label {
  color: var(--cr-text);
  font-size: 22px;
  font-weight: 600;
  line-height: 1.3;
}

.upload-sub {
  max-width: 260px;
  margin-top: 8px;
  color: var(--cr-text-soft);
  font-size: 15px;
  line-height: 1.7;
}

.identity-hints {
  width: 100%;
  margin-top: 12px;
  padding-top: 22px;
  display: grid;
  gap: 12px;
  border-top: 1px solid var(--cr-border-soft);
}

.identity-hints div {
  min-width: 0;
  padding: 16px;
  border: 1px solid var(--cr-border-soft);
  border-radius: 10px;
  background: #fff;
}

.identity-hints span {
  display: block;
  margin-bottom: 6px;
  color: var(--cr-text-muted);
  font-size: 14px;
}

.identity-hints strong {
  color: var(--cr-text);
  font-size: 17px;
}

.profile-form {
  width: 100%;
  max-width: none;

  :deep(.el-form-item) {
    margin-bottom: 18px;
  }
}

@media (max-width: 1180px) {
  .profile-card {
    min-height: 0;
    padding: 24px;
  }

  .header {
    align-items: flex-start;
    flex-wrap: wrap;
  }

  .profile-body {
    grid-template-columns: 1fr;
    gap: 22px;
  }

  .identity-uploader,
  .identity-upload,
  .identity-uploader :deep(.el-upload) {
    width: 100%;
  }

  .identity-upload {
    min-width: 0;
    min-height: 0;
  }
}

@media (max-width: 640px) {
  .profile-page {
    padding: 12px;
  }

  .profile-card {
    padding: 20px;
  }

  .identity-upload {
    min-height: 188px;
  }
}
</style>
