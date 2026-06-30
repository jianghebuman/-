import { defineStore } from 'pinia'

// 用户登录状态
export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userId: localStorage.getItem('userId') || '',
    username: localStorage.getItem('username') || '',
    name: localStorage.getItem('name') || '',
    role: localStorage.getItem('role') || '',
    avatar: localStorage.getItem('avatar') || '',
    auditStatus: localStorage.getItem('auditStatus') || '',
    enterpriseId: localStorage.getItem('enterpriseId') || '',
    hrRole: localStorage.getItem('hrRole') || '',
    unreadNoticeCount: 0,
    unreadChatCount: 0
  }),
  getters: {
    isLogin: (state) => !!state.token
  },
  actions: {
    setUser(data) {
      this.token = data.token
      this.userId = data.userId
      this.username = data.username
      this.name = data.name
      this.role = data.role
      this.avatar = data.avatar || ''
      this.auditStatus = data.auditStatus != null ? data.auditStatus : ''
      this.enterpriseId = data.enterpriseId != null ? data.enterpriseId : ''
      this.hrRole = data.hrRole || ''
      localStorage.setItem('token', data.token)
      localStorage.setItem('userId', data.userId)
      localStorage.setItem('username', data.username)
      localStorage.setItem('name', data.name || '')
      localStorage.setItem('role', data.role)
      localStorage.setItem('avatar', data.avatar || '')
      localStorage.setItem('auditStatus', data.auditStatus != null ? data.auditStatus : '')
      localStorage.setItem('enterpriseId', data.enterpriseId != null ? data.enterpriseId : '')
      localStorage.setItem('hrRole', data.hrRole || '')
    },
    setAvatar(avatar) {
      this.avatar = avatar
      localStorage.setItem('avatar', avatar)
    },
    setUnreadCounts(noticeCount, chatCount) {
      this.unreadNoticeCount = noticeCount
      this.unreadChatCount = chatCount
    },
    logout() {
      this.token = ''
      this.userId = ''
      this.username = ''
      this.name = ''
      this.role = ''
      this.avatar = ''
      this.auditStatus = ''
      this.enterpriseId = ''
      this.hrRole = ''
      this.unreadNoticeCount = 0
      this.unreadChatCount = 0
      localStorage.clear()
    }
  }
})
