import { defineStore } from 'pinia'
import { userApi } from '@/api'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('user') || 'null'),
    topPlayers: []
  }),

  actions: {
    async login(username, password) {
      const res = await userApi.login(username, password)
      this.token = res.data.token
      this.userInfo = res.data.user
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('user', JSON.stringify(res.data.user))
      return res
    },

    async register(username, password) {
      const res = await userApi.register(username, password)
      this.token = res.data.token
      this.userInfo = res.data.user
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('user', JSON.stringify(res.data.user))
      return res
    },

    async guestLogin() {
      const res = await userApi.guestLogin()
      this.token = res.data.token
      this.userInfo = res.data.user
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('user', JSON.stringify(res.data.user))
      return res
    },

    async getUserInfo() {
      const res = await userApi.getUserInfo()
      this.userInfo = res.data
      localStorage.setItem('user', JSON.stringify(res.data))
      return res
    },

    async getTopPlayers(limit = 20) {
      const res = await userApi.getTopPlayers(limit)
      this.topPlayers = res.data
      return res
    },

    updateScore(scoreChange) {
      if (this.userInfo) {
        this.userInfo.score += scoreChange
        localStorage.setItem('user', JSON.stringify(this.userInfo))
      }
    },

    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }
})
