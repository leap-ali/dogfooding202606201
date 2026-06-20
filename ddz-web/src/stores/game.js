import { defineStore } from 'pinia'

export const useGameStore = defineStore('game', {
  state: () => ({
    roomNo: '',
    baseScore: 1,
    gameStatus: 'WAITING',
    myIndex: -1,
    landlordIndex: -1,
    currentPlayerIndex: -1,
    bottomCards: [],
    lastPlayCards: [],
    lastPlayIndex: -1,
    lastPlayType: null,
    passCount: 0,
    players: [],
    selectedCards: [],
    grabOrder: [],
    currentGrabIndex: 0,
    gameResult: null,
    showResult: false
  }),

  getters: {
    myCards: (state) => {
      if (state.myIndex >= 0 && state.players[state.myIndex]) {
        return state.players[state.myIndex].cards || []
      }
      return []
    },
    isMyTurn: (state) => {
      return state.myIndex === state.currentPlayerIndex
    },
    isLandlord: (state) => {
      return state.myIndex === state.landlordIndex
    },
    leftPlayer: (state) => {
      const idx = (state.myIndex + 1) % 4
      return state.players[idx] || null
    },
    topPlayer: (state) => {
      const idx = (state.myIndex + 2) % 4
      return state.players[idx] || null
    },
    rightPlayer: (state) => {
      const idx = (state.myIndex + 3) % 4
      return state.players[idx] || null
    },
    leftPlayerSeat: (state) => (state.myIndex + 1) % 4,
    topPlayerSeat: (state) => (state.myIndex + 2) % 4,
    rightPlayerSeat: (state) => (state.myIndex + 3) % 4
  },

  actions: {
    resetState() {
      this.roomNo = ''
      this.baseScore = 1
      this.gameStatus = 'WAITING'
      this.myIndex = -1
      this.landlordIndex = -1
      this.currentPlayerIndex = -1
      this.bottomCards = []
      this.lastPlayCards = []
      this.lastPlayIndex = -1
      this.lastPlayType = null
      this.passCount = 0
      this.players = []
      this.selectedCards = []
      this.grabOrder = []
      this.currentGrabIndex = 0
      this.gameResult = null
      this.showResult = false
    },

    setRoomState(state) {
      this.roomNo = state.roomNo
      this.baseScore = state.baseScore
      this.gameStatus = state.gameStatus
      this.myIndex = state.myIndex
      this.landlordIndex = state.landlordIndex
      this.currentPlayerIndex = state.currentPlayerIndex
      this.bottomCards = state.bottomCards || []
      this.lastPlayCards = state.lastPlayCards || []
      this.lastPlayIndex = state.lastPlayIndex
      this.lastPlayType = state.lastPlayType
      this.players = state.players || []
      if (state.grabOrder) {
        this.grabOrder = state.grabOrder
        this.currentGrabIndex = state.currentGrabIndex
      }
      this.passCount = state.passCount || 0
    },

    setMySelectedCards(cards) {
      this.selectedCards = cards
    },

    toggleCardSelect(card) {
      const idx = this.selectedCards.findIndex(c => c.code === card.code)
      if (idx >= 0) {
        this.selectedCards.splice(idx, 1)
      } else {
        this.selectedCards.push(card)
      }
    },

    clearSelectedCards() {
      this.selectedCards = []
    },

    setGameResult(result) {
      this.gameResult = result
      this.showResult = true
    },

    hideResult() {
      this.showResult = false
    }
  }
})
