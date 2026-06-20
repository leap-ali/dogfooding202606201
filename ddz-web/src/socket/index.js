import { io } from 'socket.io-client'
import { useUserStore } from '@/stores/user'
import { useGameStore } from '@/stores/game'
import { ElMessage } from 'element-plus'
import router from '@/router'

let socket = null
let reconnectTimer = null

export function initSocket() {
  const userStore = useUserStore()
  const gameStore = useGameStore()
  
  if (!userStore.token) {
    return null
  }

  if (socket) {
    return socket
  }

  socket = io('http://localhost:9092', {
    query: { token: userStore.token },
    transports: ['websocket', 'polling'],
    reconnection: true,
    reconnectionDelay: 1000,
    reconnectionDelayMax: 5000
  })

  socket.on('connect', () => {
    console.log('Socket connected:', socket.id)
    if (gameStore.roomNo) {
      socket.emit('join_room', { roomNo: gameStore.roomNo })
    }
  })

  socket.on('disconnect', () => {
    console.log('Socket disconnected')
  })

  socket.on('error', (msg) => {
    ElMessage.error(msg)
  })

  socket.on('room_state', (state) => {
    gameStore.setRoomState(state)
  })

  socket.on('left_room', (roomNo) => {
    if (gameStore.roomNo === roomNo) {
      gameStore.resetState()
      router.push('/lobby')
    }
  })

  socket.on('grab_update', (data) => {
    console.log('grab_update:', data)
  })

  socket.on('landlord_decided', (data) => {
    console.log('landlord_decided:', data)
  })

  socket.on('play_cards', (data) => {
    console.log('play_cards:', data)
  })

  socket.on('pass_cards', (data) => {
    console.log('pass_cards:', data)
  })

  socket.on('play_error', (msg) => {
    ElMessage.warning(msg)
  })

  socket.on('hint_cards_result', (cards) => {
    if (cards && cards.length > 0) {
      gameStore.setMySelectedCards(cards)
    } else {
      ElMessage.info('没有可以出的牌')
    }
  })

  socket.on('game_over', (result) => {
    gameStore.setGameResult(result)
    const myResult = result.players.find(p => p.userId === userStore.userInfo.id)
    if (myResult) {
      userStore.updateScore(myResult.scoreChange)
    }
  })

  socket.on('chat_message', (msg) => {
    console.log('chat_message:', msg)
  })

  return socket
}

export function getSocket() {
  return socket
}

export function joinRoom(roomNo) {
  if (socket) {
    socket.emit('join_room', { roomNo })
  }
}

export function leaveRoom(roomNo) {
  if (socket) {
    socket.emit('leave_room', { roomNo })
  }
}

export function readyGame(roomNo) {
  if (socket) {
    socket.emit('ready_game', { roomNo })
  }
}

export function grabLandlord(roomNo, grab) {
  if (socket) {
    socket.emit('grab_landlord', { roomNo, grab })
  }
}

export function playCards(roomNo, cards) {
  if (socket) {
    socket.emit('play_cards', { roomNo, cards })
  }
}

export function passCards(roomNo) {
  if (socket) {
    socket.emit('pass_cards', { roomNo })
  }
}

export function hintCards(roomNo) {
  if (socket) {
    socket.emit('hint_cards', { roomNo })
  }
}

export function sendChat(roomNo, message) {
  if (socket) {
    socket.emit('chat_message', { roomNo, message })
  }
}

export function disconnectSocket() {
  if (socket) {
    socket.disconnect()
    socket = null
  }
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
}
