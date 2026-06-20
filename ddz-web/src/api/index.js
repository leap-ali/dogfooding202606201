import request from './request'

export const userApi = {
  login(username, password) {
    return request({
      url: '/user/login',
      method: 'post',
      data: { username, password }
    })
  },

  register(username, password) {
    return request({
      url: '/user/register',
      method: 'post',
      data: { username, password }
    })
  },

  guestLogin() {
    return request({
      url: '/user/guest-login',
      method: 'post'
    })
  },

  getUserInfo() {
    return request({
      url: '/user/info',
      method: 'get'
    })
  },

  getTopPlayers(limit = 20) {
    return request({
      url: '/user/top',
      method: 'get',
      params: { limit }
    })
  }
}

export const roomApi = {
  createRoom(baseScore = 1) {
    return request({
      url: '/room/create',
      method: 'post',
      data: { baseScore }
    })
  },

  joinRoom(roomNo) {
    return request({
      url: '/room/join',
      method: 'post',
      data: { roomNo }
    })
  },

  leaveRoom(roomNo) {
    return request({
      url: '/room/leave',
      method: 'post',
      data: { roomNo }
    })
  },

  getRoomList() {
    return request({
      url: '/room/list',
      method: 'get'
    })
  },

  getRoomInfo(roomNo) {
    return request({
      url: '/room/' + roomNo,
      method: 'get'
    })
  }
}

export const gameApi = {
  getRecords() {
    return request({
      url: '/game/records',
      method: 'get'
    })
  }
}
