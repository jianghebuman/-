import { chatApi, noticeApi } from '@/api'

export async function refreshUnreadCounts(userStore, { includeChat = true } = {}) {
  if (!userStore.isLogin || userStore.role === 'ADMIN') {
    userStore.setUnreadCounts(0, 0)
    return
  }
  const noticeRes = await noticeApi.unread()
  let chatTotal = userStore.unreadChatCount
  if (includeChat) {
    const chatRes = await chatApi.conversations()
    chatTotal = (chatRes.data || []).reduce((sum, item) => sum + Number(item.unread || 0), 0)
  }
  userStore.setUnreadCounts(Number(noticeRes.data || 0), chatTotal)
}
