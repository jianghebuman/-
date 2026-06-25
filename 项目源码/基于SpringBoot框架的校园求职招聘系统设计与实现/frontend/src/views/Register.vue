<template>
  <div class="register-page">
    <header class="register-topbar">
      <router-link class="brand-mark" to="/">
        <span class="brand-icon">
          <Briefcase />
        </span>
        <span>校园招聘工作台</span>
      </router-link>
      <router-link class="ghost-link" to="/login">
        <el-icon><ArrowLeft /></el-icon>
        返回登录
      </router-link>
    </header>

    <main class="register-stage">
      <section class="register-shell" aria-label="账号注册">
        <aside class="register-brief">
          <div class="window-strip">
            <span></span>
            <span></span>
            <span></span>
          </div>
          <span class="brief-kicker">账号注册</span>
          <h1>{{ activeCopy.title }}</h1>
          <p>{{ activeCopy.description }}</p>
          <div class="brief-highlights" aria-label="注册完成后可使用的能力">
            <div v-for="item in activeCopy.highlights" :key="item.label">
              <b>{{ item.value }}</b>
              <span>{{ item.label }}</span>
            </div>
          </div>
          <div class="brief-flow">
            <div v-for="item in activeCopy.flow" :key="item.title" class="flow-item">
              <component :is="item.icon" />
              <div>
                <b>{{ item.title }}</b>
                <span>{{ item.desc }}</span>
              </div>
            </div>
          </div>
        </aside>

        <section class="register-panel">
          <div class="role-switcher" aria-label="选择注册身份">
            <button
              v-for="item in roleOptions"
              :key="item.value"
              type="button"
              class="role-chip"
              :class="{ active: role === item.value }"
              :aria-pressed="role === item.value"
              @click="role = item.value"
            >
              <component :is="item.icon" />
              <span>{{ item.label }}</span>
            </button>
          </div>

          <el-form
            v-if="role === 'STUDENT'"
            ref="stuRef"
            :model="stuForm"
            :rules="stuRules"
            label-position="top"
            class="register-form"
          >
            <div class="form-section">
              <div class="section-heading">
                <span>登录信息</span>
                <small>用于后续进入学生工作台</small>
              </div>
              <div class="form-grid">
                <el-form-item label="账号" prop="username">
                  <el-input v-model="stuForm.username" :prefix-icon="User" placeholder="输入登录账号" size="large" />
                </el-form-item>
                <el-form-item label="密码" prop="password">
                  <el-input
                    v-model="stuForm.password"
                    :prefix-icon="Lock"
                    type="password"
                    show-password
                    placeholder="至少 6 位"
                    size="large"
                  />
                </el-form-item>
              </div>
            </div>

            <div class="form-section">
              <div class="section-heading">
                <span>学生信息</span>
                <small>建议与学籍信息保持一致</small>
              </div>
              <div class="form-grid">
                <el-form-item label="姓名" prop="realName">
                  <el-input v-model="stuForm.realName" placeholder="输入真实姓名" size="large" />
                </el-form-item>
                <el-form-item label="学号">
                  <el-input v-model="stuForm.studentNo" placeholder="输入学号" size="large" />
                </el-form-item>
                <el-form-item label="学院">
                  <el-input v-model="stuForm.college" placeholder="所在学院" size="large" />
                </el-form-item>
                <el-form-item label="专业">
                  <el-input v-model="stuForm.major" placeholder="所学专业" size="large" />
                </el-form-item>
                <el-form-item label="年级">
                  <el-input v-model="stuForm.grade" placeholder="如 2023级" size="large" />
                </el-form-item>
                <el-form-item label="手机号" prop="phone">
                  <el-input v-model="stuForm.phone" :prefix-icon="Phone" placeholder="用于接收面试联系" size="large" />
                </el-form-item>
                <el-form-item label="邮箱" class="wide-field">
                  <el-input v-model="stuForm.email" :prefix-icon="Message" placeholder="常用邮箱" size="large" />
                </el-form-item>
              </div>
            </div>

            <button class="primary-action" type="button" :disabled="loading" @click="registerStudent">
              <span>{{ loading ? '正在创建账号' : '创建学生账号' }}</span>
              <el-icon><Right /></el-icon>
            </button>
          </el-form>

          <el-form
            v-else
            ref="entRef"
            :model="entForm"
            :rules="entRules"
            label-position="top"
            class="register-form"
          >
            <div class="form-section">
              <div class="section-heading">
                <span>登录信息</span>
                <small>企业 HR 后续使用该账号管理招聘</small>
              </div>
              <div class="form-grid">
                <el-form-item label="账号" prop="username">
                  <el-input v-model="entForm.username" :prefix-icon="User" placeholder="输入登录账号" size="large" />
                </el-form-item>
                <el-form-item label="密码" prop="password">
                  <el-input
                    v-model="entForm.password"
                    :prefix-icon="Lock"
                    type="password"
                    show-password
                    placeholder="至少 6 位"
                    size="large"
                  />
                </el-form-item>
              </div>
            </div>

            <div class="form-section">
              <div class="section-heading">
                <span>企业资质信息</span>
                <small>注册后仍需上传营业执照等认证材料</small>
              </div>
              <div class="form-grid">
                <el-form-item label="企业名称" prop="companyName">
                  <el-input v-model="entForm.companyName" :prefix-icon="OfficeBuilding" placeholder="营业执照登记名称" size="large" />
                </el-form-item>
                <el-form-item label="统一社会信用代码" prop="creditCode">
                  <el-input
                    v-model.trim="entForm.creditCode"
                    :prefix-icon="Postcard"
                    maxlength="18"
                    placeholder="18 位大写字母或数字"
                    size="large"
                    @input="normalizeCreditCode"
                  />
                </el-form-item>
                <el-form-item label="所属行业" prop="industry">
                  <el-select v-model="entForm.industry" placeholder="选择行业" size="large">
                    <el-option v-for="item in industryOptions" :key="item" :label="item" :value="item" />
                  </el-select>
                </el-form-item>
                <el-form-item label="企业规模">
                  <el-select v-model="entForm.scale" placeholder="选择规模" size="large">
                    <el-option v-for="item in scaleOptions" :key="item" :label="item" :value="item" />
                  </el-select>
                </el-form-item>
                <el-form-item label="所在城市" prop="city">
                  <el-input v-model="entForm.city" :prefix-icon="Location" placeholder="如 北京、上海、深圳" size="large" />
                </el-form-item>
                <el-form-item label="官网">
                  <el-input v-model="entForm.website" :prefix-icon="Link" placeholder="https://example.com" size="large" />
                </el-form-item>
              </div>
            </div>

            <div class="form-section">
              <div class="section-heading">
                <span>招聘联系人</span>
                <small>用于管理员核验企业入驻信息</small>
              </div>
              <div class="form-grid">
                <el-form-item label="联系人姓名" prop="contactName">
                  <el-input v-model="entForm.contactName" placeholder="企业招聘负责人" size="large" />
                </el-form-item>
                <el-form-item label="联系电话" prop="contactPhone">
                  <el-input v-model="entForm.contactPhone" :prefix-icon="Phone" placeholder="手机号或固定电话" size="large" />
                </el-form-item>
                <el-form-item label="企业邮箱" prop="email" class="wide-field">
                  <el-input v-model="entForm.email" :prefix-icon="Message" placeholder="建议使用企业域名邮箱" size="large" />
                </el-form-item>
              </div>
            </div>

            <el-form-item prop="agreeTerms" class="agree-field">
              <el-checkbox v-model="entForm.agreeTerms" size="large">
                我确认企业信息真实有效，注册后将提交营业执照等认证材料
              </el-checkbox>
            </el-form-item>

            <button class="primary-action" type="button" :disabled="loading" @click="registerEnterprise">
              <span>{{ loading ? '正在提交入驻信息' : '提交企业入驻申请' }}</span>
              <el-icon><Right /></el-icon>
            </button>
          </el-form>

          <div class="panel-footer">
            <router-link to="/login">已有账号，去登录</router-link>
            <router-link to="/">先浏览招聘信息</router-link>
          </div>
        </section>
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft,
  Briefcase,
  Check,
  DocumentChecked,
  Link,
  Location,
  Lock,
  Message,
  OfficeBuilding,
  Phone,
  Postcard,
  Right,
  School,
  Tickets,
  User
} from '@element-plus/icons-vue'
import { authApi } from '@/api'

const router = useRouter()
const role = ref('STUDENT')
const loading = ref(false)
const stuRef = ref()
const entRef = ref()

const roleOptions = [
  { value: 'STUDENT', label: '学生注册', icon: School },
  { value: 'ENTERPRISE', label: '企业入驻', icon: OfficeBuilding }
]

const copyMap = {
  STUDENT: {
    title: '把简历和投递记录放进同一个工作台。',
    description: '学生账号用于维护个人资料、在线简历、投递记录和面试安排。',
    highlights: [
      { value: '资料', label: '个人信息与求职意向' },
      { value: '简历', label: '在线简历和附件管理' },
      { value: '进度', label: '投递、面试、Offer 跟进' }
    ],
    flow: [
      { icon: User, title: '建立个人账号', desc: '填写基础身份信息' },
      { icon: Tickets, title: '完善求职资料', desc: '维护简历与求职意向' },
      { icon: Check, title: '跟进招聘流程', desc: '查看投递、面试与 Offer' }
    ]
  },
  ENTERPRISE: {
    title: '企业入驻先核验，再开展校园招聘。',
    description: '参照招聘平台常见做法，企业注册需提交公司资质和招聘联系人信息。',
    highlights: [
      { value: '资质', label: '企业认证资料留档' },
      { value: '岗位', label: '招聘岗位发布与刷新' },
      { value: '候选', label: '简历筛选和面试邀约' }
    ],
    flow: [
      { icon: Postcard, title: '登记企业资质', desc: '填写统一社会信用代码' },
      { icon: DocumentChecked, title: '提交认证材料', desc: '注册后上传营业执照' },
      { icon: Check, title: '通过后发布岗位', desc: '管理员审核后开展招聘' }
    ]
  }
}

const activeCopy = computed(() => copyMap[role.value])

const stuForm = reactive({
  username: '',
  password: '',
  realName: '',
  studentNo: '',
  college: '',
  major: '',
  grade: '',
  phone: '',
  email: ''
})

const entForm = reactive({
  username: '',
  password: '',
  companyName: '',
  creditCode: '',
  industry: '',
  scale: '',
  city: '',
  contactName: '',
  contactPhone: '',
  email: '',
  website: '',
  agreeTerms: false
})

const industryOptions = ['互联网', '计算机软件', '电子商务', '金融', '教育培训', '制造业', '文化传媒', '医疗健康', '其他']
const scaleOptions = ['20人以下', '20-99人', '100-499人', '500-999人', '1000人以上']

const required = (msg) => [{ required: true, message: msg, trigger: 'blur' }]
const passwordRule = [
  { required: true, message: '请输入密码', trigger: 'blur' },
  { min: 6, message: '密码至少 6 位', trigger: 'blur' }
]
const emailRule = [
  { type: 'email', message: '请输入有效的企业邮箱', trigger: ['blur', 'change'] }
]
const phoneRule = [
  { required: true, message: '请输入联系电话', trigger: 'blur' },
  {
    pattern: /^1[3-9]\d{9}$|^0\d{2,3}-?\d{7,8}$/,
    message: '请输入有效的手机号或固定电话',
    trigger: 'blur'
  }
]
const creditCodeRule = [
  { required: true, message: '请输入统一社会信用代码', trigger: 'blur' },
  { pattern: /^[0-9A-Z]{18}$/, message: '请输入18位大写字母或数字', trigger: ['blur', 'change'] }
]
const stuRules = {
  username: required('请输入账号'),
  password: passwordRule,
  realName: required('请输入姓名'),
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号', trigger: 'blur' }
  ]
}

const entRules = {
  username: required('请输入账号'),
  password: passwordRule,
  companyName: required('请输入企业名称'),
  creditCode: creditCodeRule,
  industry: required('请选择所属行业'),
  city: required('请输入所在城市'),
  contactName: required('请输入联系人姓名'),
  contactPhone: phoneRule,
  email: emailRule,
  agreeTerms: [
    {
      validator: (_rule, value, callback) => {
        if (value) callback()
        else callback(new Error('请确认企业信息真实有效'))
      },
      trigger: 'change'
    }
  ]
}

const normalizeCreditCode = (value) => {
  entForm.creditCode = value.toUpperCase().replace(/[^0-9A-Z]/g, '').slice(0, 18)
}

const registerStudent = () => {
  stuRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await authApi.registerStudent(stuForm)
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } finally {
      loading.value = false
    }
  })
}

const registerEnterprise = () => {
  entRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await authApi.registerEnterprise(entForm)
      ElMessage.success('企业账号已创建，请登录后完善企业认证')
      router.push('/login')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped lang="scss">
.register-page {
  position: relative;
  --auth-shell-width: min(150rem, calc(100% - clamp(1.5rem, 4vw, 6rem)));
  min-height: 100dvh;
  overflow-x: hidden;
  color: #172033;
  background:
    linear-gradient(90deg, rgba(37, 99, 235, 0.05) 1px, transparent 1px),
    linear-gradient(0deg, rgba(37, 99, 235, 0.05) 1px, transparent 1px),
    #f4f7fb;
  background-size: 42px 42px;
  font-family: "Inter", "PingFang SC", "Microsoft YaHei", sans-serif;
}

.register-page::before {
  position: absolute;
  inset: 0;
  content: "";
  pointer-events: none;
  background:
    radial-gradient(circle at 16% 18%, rgba(8, 145, 178, 0.14), transparent 28%),
    linear-gradient(180deg, transparent 0%, rgba(37, 99, 235, 0.08) 100%);
}

.register-topbar {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: var(--auth-shell-width);
  margin: 0 auto;
  padding: clamp(1rem, 2dvh, 1.5rem) 0 clamp(0.75rem, 1.6dvh, 1.125rem);
}

.brand-mark,
.ghost-link {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  font-weight: 800;
}

.brand-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  color: #f7efe0;
  background: #2563eb;
  border: 1px solid rgba(37, 99, 235, 0.22);
  border-radius: 8px;

  svg {
    width: 18px;
    height: 18px;
  }
}

.ghost-link {
  padding: 9px 12px;
  color: #5d6f88;
  background: rgba(255, 255, 255, 0.58);
  border: 1px solid rgba(93, 111, 136, 0.18);
  border-radius: 8px;
  transition: color 0.2s ease, border-color 0.2s ease, transform 0.2s ease;
}

.ghost-link:hover {
  color: #172033;
  border-color: rgba(37, 99, 235, 0.32);
  transform: translateY(-1px);
}

.register-stage {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: center;
  min-height: calc(100dvh - clamp(4.75rem, 6dvh, 6.25rem));
  padding: clamp(1rem, 2dvh, 1.5rem) 0 clamp(1.5rem, 3dvh, 3rem);
}

.register-shell {
  display: grid;
  grid-template-columns: minmax(34rem, 0.92fr) minmax(42rem, 1.08fr);
  width: var(--auth-shell-width);
  min-height: clamp(36rem, 86dvh, 92rem);
  overflow: hidden;
  border: 1px solid rgba(93, 111, 136, 0.18);
  border-radius: clamp(1rem, 1vw, 1.5rem);
  background: #ffffff;
  box-shadow: 0 34px 70px rgba(22, 38, 68, 0.16);
}

.register-brief {
  position: relative;
  display: flex;
  flex-direction: column;
  min-height: 100%;
  padding: clamp(2rem, 3.4vw, 5rem);
  color: #eef6ff;
  background:
    linear-gradient(90deg, rgba(255, 255, 255, 0.08) 1px, transparent 1px),
    linear-gradient(0deg, rgba(255, 255, 255, 0.06) 1px, transparent 1px),
    #15243b;
  background-size: 32px 32px;
}

.window-strip {
  display: flex;
  gap: 8px;
  margin-bottom: clamp(3rem, 7dvh, 7rem);

  span {
    width: 10px;
    height: 10px;
    border: 1px solid rgba(238, 246, 255, 0.42);
    border-radius: 50%;
  }
}

.brief-kicker {
  width: max-content;
  padding: 7px 10px;
  margin-bottom: 18px;
  color: #15243b;
  background: #0891b2;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 900;
}

.register-brief h1 {
  margin: 0;
  width: min(44rem, 100%);
  font-size: 2.75rem;
  line-height: 1.08;
  font-weight: 850;
}

.register-brief p {
  width: min(38rem, 100%);
  margin: 20px 0 0;
  color: rgba(238, 246, 255, 0.76);
  font-size: 15px;
  line-height: 1.8;
}

.brief-highlights {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 0.75rem;
  width: min(42rem, 100%);
  margin-top: clamp(2rem, 5dvh, 4rem);

  div {
    min-height: 6.75rem;
    padding: 1rem;
    border: 1px solid rgba(238, 246, 255, 0.16);
    border-radius: 0.75rem;
    background: rgba(238, 246, 255, 0.06);
  }

  b {
    display: block;
    margin-bottom: 0.625rem;
    color: #eef6ff;
    font-size: 1.25rem;
    font-weight: 900;
  }

  span {
    display: block;
    color: rgba(238, 246, 255, 0.68);
    font-size: 0.8125rem;
    line-height: 1.65;
  }
}

.brief-flow {
  display: grid;
  gap: 14px;
  width: min(40rem, 100%);
  margin-top: auto;
  padding-top: 0;
}

.flow-item {
  display: grid;
  grid-template-columns: 38px minmax(0, 1fr);
  gap: 12px;
  align-items: start;
  padding: 14px 0;
  border-top: 1px dashed rgba(238, 246, 255, 0.22);

  svg {
    width: 20px;
    height: 20px;
    margin-top: 1px;
    color: #0891b2;
  }

  b {
    display: block;
    font-size: 14px;
  }

  span {
    display: block;
    margin-top: 5px;
    color: rgba(238, 246, 255, 0.66);
    font-size: 13px;
    line-height: 1.55;
  }
}

.register-panel {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: clamp(2rem, 3.4vw, 5rem);
  background: #ffffff;
}

.role-switcher {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 28px;
}

.role-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 46px;
  padding: 0 14px;
  color: #5d6f88;
  background: #f8fbff;
  border: 1px solid rgba(93, 111, 136, 0.16);
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 900;
  transition: background 0.2s ease, border-color 0.2s ease, color 0.2s ease, transform 0.2s ease;

  svg {
    width: 17px;
    height: 17px;
  }
}

.role-chip:hover {
  color: #172033;
  border-color: rgba(37, 99, 235, 0.28);
  transform: translateY(-1px);
}

.role-chip.active {
  color: #ffffff;
  background: #2563eb;
  border-color: #2563eb;
  box-shadow: inset 0 -2px 0 rgba(8, 145, 178, 0.72);
}

.register-form {
  display: grid;
  gap: 22px;

  :deep(.el-form-item) {
    margin-bottom: 0;
  }

  :deep(.el-form-item__label) {
    margin-bottom: 7px;
    color: #172033;
    font-weight: 850;
    line-height: 1.2;
  }

  :deep(.el-input__wrapper),
  :deep(.el-select__wrapper) {
    min-height: 46px;
    border-radius: 8px;
    background: #f8fbff;
    box-shadow: inset 0 0 0 1px rgba(93, 111, 136, 0.2);
    transition: box-shadow 0.2s ease, background 0.2s ease;
  }

  :deep(.el-input__wrapper.is-focus),
  :deep(.el-select__wrapper.is-focused) {
    background: #ffffff;
    box-shadow: inset 0 0 0 1px #2563eb, 0 0 0 4px rgba(37, 99, 235, 0.12);
  }

  :deep(.el-input__inner) {
    color: #172033;
    font-weight: 650;
  }
}

.form-section {
  padding: 18px;
  border: 1px solid rgba(93, 111, 136, 0.16);
  border-radius: 8px;
  background: #f8fbff;
}

.section-heading {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px dashed rgba(93, 111, 136, 0.2);

  span {
    color: #172033;
    font-size: 15px;
    font-weight: 900;
  }

  small {
    color: #8a99ad;
    font-size: 12px;
    line-height: 1.45;
    text-align: right;
  }
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px 16px;
}

.wide-field {
  grid-column: 1 / -1;
}

.agree-field {
  padding: 2px 4px 0;

  :deep(.el-checkbox) {
    align-items: flex-start;
    min-width: 0;
    height: auto;
    color: #5d6f88;
    white-space: normal;
    font-weight: 750;
  }

  :deep(.el-checkbox__label) {
    min-width: 0;
    line-height: 1.6;
    white-space: normal;
  }
}

.primary-action {
  display: inline-flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  min-height: 52px;
  padding: 0 16px 0 20px;
  color: #ffffff;
  background: #172033;
  border: 1px solid #172033;
  border-radius: 8px;
  box-shadow: 0 12px 26px rgba(37, 99, 235, 0.24);
  cursor: pointer;
  font-size: 15px;
  font-weight: 900;
  transition: background 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.primary-action:hover:not(:disabled) {
  background: #1d4ed8;
  box-shadow: 0 16px 30px rgba(37, 99, 235, 0.28);
  transform: translateY(-1px);
}

.primary-action:disabled {
  cursor: wait;
  opacity: 0.74;
}

.panel-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: nowrap;
  gap: 14px;
  margin-top: 22px;
  padding-top: 18px;
  border-top: 1px solid rgba(93, 111, 136, 0.16);
  font-size: 14px;

  a {
    color: #2563eb;
    font-weight: 850;
  }

  a:hover {
    color: #172033;
  }
}

@media (min-width: 1800px) {
  .register-panel {
    justify-content: flex-start;
    padding-block: clamp(5rem, 9dvh, 9rem) clamp(4rem, 8dvh, 8rem);
  }

  .register-brief h1 {
    font-size: 3.5rem;
  }

  .role-chip {
    min-height: 3.5rem;
    font-size: 0.9375rem;
  }

  .role-switcher {
    gap: 0.75rem;
    margin-bottom: 2rem;
  }

  .register-form {
    gap: 1.5rem;

    :deep(.el-input__wrapper),
    :deep(.el-select__wrapper) {
      min-height: 3.5rem;
    }
  }

  .primary-action {
    min-height: 4.25rem;
    font-size: 1rem;
  }
}

@media (max-width: 980px) {
  .register-shell {
    grid-template-columns: 1fr;
    min-height: 0;
  }

  .register-brief {
    min-height: 0;
    order: 2;
  }

  .register-panel {
    order: 1;
  }

  .brief-flow {
    margin-top: 34px;
    padding-top: 0;
  }

  .brief-highlights {
    grid-template-columns: 1fr;
    margin-top: 28px;
  }
}

@media (max-width: 640px) {
  .register-topbar {
    width: calc(100% - 1.25rem);
    padding-top: 16px;
  }

  .brand-mark span:last-child {
    display: none;
  }

  .register-stage {
    padding: 12px 14px 28px;
  }

  .register-shell {
    width: 100%;
    border-radius: 0.875rem;
  }

  .register-brief,
  .register-panel {
    padding: 22px;
  }

  .register-brief {
    order: 2;
  }

  .register-panel {
    order: 1;
  }

  .window-strip {
    margin-bottom: 28px;
  }

  .register-brief h1 {
    font-size: 1.75rem;
  }

  .brief-highlights div {
    min-height: 0;
  }

  .role-switcher {
    grid-template-columns: 1fr;
    gap: 8px;
    margin-bottom: 20px;
  }

  .role-chip {
    min-height: 44px;
    padding: 0 10px;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .section-heading {
    align-items: flex-start;
    flex-direction: column;
  }

  .section-heading small {
    text-align: left;
  }

  .panel-footer {
    align-items: center;
    justify-content: space-between;
    flex-direction: row;
    flex-wrap: wrap;
    gap: 10px;
    font-size: 13px;

    a {
      white-space: nowrap;
    }
  }
}
</style>

