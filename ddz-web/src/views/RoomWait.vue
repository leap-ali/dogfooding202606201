<template>
  <div class="room-wait page-container">
    <div class="room-wait-content card-base">
      <div class="room-header">
        <h2>房间号: {{ gameStore.roomNo }}</h2>
        <span class="base-score">底分: {{ gameStore.baseScore }}</span>
      </div>

      <div class="players-area">
        <div
          v-for="(player, index) in gameStore.players"
          :key="player.userId"
          class="player-slot"
          :class="{ active: index === gameStore.myIndex, 'is-landlord': player.identity === 1 }"
        >
          <div class="player-avatar">
            <el-avatar :size="80" :src="player.avatar || '/default-avatar.png'" />
            <div v-if="player.identity === 1" class="landlord-badge">地主</div>
          </div>
          <div class="player-info">
            <span class="nickname">{{ player.nickname }}</span>
            <span class="score">积分: {{ player.score }}</span>
            <span class="card-count" v-if="gameStore.gameStatus !== 'WAITING'">
              手牌: {{ player.cardCount }}张
            </span>
          </div>
          <div class="status-badge" :class="{ online: player.isOnline }">
            {{ player.isOnline ? '在线' : '离线' }}
          </div>
        </div>

        <div
          v-for="n in (3 - gameStore.players.length)"
          :key="'empty-' + n"
          class="player-slot empty"
        >
          <div class="empty-avatar">
            <el-icon :size="40"><User /></el-icon>
          </div>
          <span class="empty-text">等待玩家加入...</span>
        </div>
      </div>

      <div v-if="gameStore.gameStatus === 'WAITING'" class="waiting-tip">
        <el-icon class="loading-icon"><Loading /></el-icon>
        等待其他玩家加入... ({{ gameStore.players.length }}/3)
      </div>

      <div v-if="gameStore.gameStatus === 'GRABBING'" class="grabbing-tip">
        <p>抢地主阶段</p>
        <p v-if="isMyTurnToGrab" class="highlight">轮到你抢地主了！</p>
        <p v-else>等待其他玩家选择...</p>
      </div>

      <div class="action-area">
        <template v-if="gameStore.gameStatus === 'WAITING'">
          <el-button type="danger" @click="handleLeaveRoom">
            <el-icon><Back /></el-icon>
            退出房间
          </el-button>
          <el-button
            type="success"
            :disabled="gameStore.players.length < 3"
            @click="handleReady"
          >
            开始游戏
          </el-button>
        </template>

        <template v-if="gameStore.gameStatus === 'GRABBING' && isMyTurnToGrab">
          <el-button type="warning" size="large" @click="handleGrab(false)">
            不抢
          </el-button>
          <el-button type="danger" size="large" @click="handleGrab(true)">
            抢地主
          </el-button>
        </template>

        <template v-if="gameStore.gameStatus === 'PLAYING'">
          <el-button type="primary" @click="goToGame">
            进入游戏
          </el-button>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useGameStore } from '@/stores/game'
import { useUserStore } from '@/stores/user'
import { initSocket, joinRoom, leaveRoom, grabLandlord, readyGame } from '@/socket'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Back, Loading } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const gameStore = useGameStore()
const userStore = useUserStore()

const roomNo = computed(() => route.params.roomNo)

const isMyTurnToGrab = computed(() => {
  if (gameStore.gameStatus !== 'GRABBING') return false
  const currentGrabIdx = gameStore.grabOrder[gameStore.currentGrabIndex]
  return currentGrabIdx === gameStore.myIndex
})

onMounted(() => {
  if (!userStore.token) {
    router.push('/login')
    return
  }
  initSocket()
  joinRoom(roomNo.value)
  
  setTimeout(() => {
    if (gameStore.gameStatus === 'PLAYING') {
      router.push('/game/' + roomNo.value)
    }
  }, 500)
})

onUnmounted(() => {
  // 不移除socket，游戏页面继续用
})

watch(
  () => gameStore.gameStatus,
  (newStatus) => {
    if (newStatus === 'PLAYING') {
      ElMessage.success('游戏开始！')
      setTimeout(() => {
        router.push('/game/' + roomNo.value)
      }, 500)
    }
  }
)

function handleLeaveRoom() {
  ElMessageBox.confirm('确定要退出房间吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    leaveRoom(roomNo.value)
    router.push('/lobby')
  }).catch(() => {})
}

function handleReady() {
  if (gameStore.players.length < 3) {
    ElMessage.warning('人数不足，无法开始')
    return
  }
  readyGame(roomNo.value)
}

function handleGrab(grab) {
  grabLandlord(roomNo.value, grab)
}

function goToGame() {
  router.push('/game/' + roomNo.value)
}
</script>

<style scoped lang="scss">
.room-wait {
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #1a1a2e 0%, #0d47a1 100%);

  .room-wait-content {
    width: 800px;
    max-width: 90%;
    padding: 40px;
    background: rgba(255, 255, 255, 0.95);
    color: #333;
    border-radius: 16px;
  }

  .room-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30px;
    padding-bottom: 15px;
    border-bottom: 2px solid #eee;

    h2 {
      margin: 0;
      color: #409EFF;
    }

    .base-score {
      color: #E6A23C;
      font-size: 16px;
      font-weight: bold;
    }
  }

  .players-area {
    display: flex;
    justify-content: space-around;
    gap: 20px;
    margin-bottom: 30px;
  }

  .player-slot {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20px;
    border-radius: 12px;
    background: #f5f7fa;
    position: relative;
    transition: all 0.3s;

    &.active {
      background: #ecf5ff;
      box-shadow: 0 0 0 2px #409EFF;
    }

    &.is-landlord {
      background: #fdf6ec;
      box-shadow: 0 0 0 2px #E6A23C;
    }

    &.empty {
      border: 2px dashed #dcdfe6;
      background: transparent;
    }

    .player-avatar {
      position: relative;
      margin-bottom: 12px;

      .landlord-badge {
        position: absolute;
        bottom: -5px;
        left: 50%;
        transform: translateX(-50%);
        background: #E6A23C;
        color: white;
        padding: 2px 8px;
        border-radius: 10px;
        font-size: 12px;
        white-space: nowrap;
      }
    }

    .player-info {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 4px;

      .nickname {
        font-size: 16px;
        font-weight: bold;
      }

      .score {
        color: #E6A23C;
        font-size: 13px;
      }

      .card-count {
        color: #67C23A;
        font-size: 13px;
      }
    }

    .status-badge {
      margin-top: 8px;
      padding: 2px 10px;
      border-radius: 12px;
      font-size: 12px;
      background: #909399;
      color: white;

      &.online {
        background: #67C23A;
      }
    }

    .empty-avatar {
      width: 80px;
      height: 80px;
      border-radius: 50%;
      background: #f0f0f0;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #c0c0c0;
      margin-bottom: 12px;
    }

    .empty-text {
      color: #909399;
      font-size: 13px;
    }
  }

  .waiting-tip {
    text-align: center;
    color: #909399;
    font-size: 16px;
    margin-bottom: 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;

    .loading-icon {
      animation: spin 1s linear infinite;
    }
  }

  .grabbing-tip {
    text-align: center;
    margin-bottom: 20px;

    p {
      margin: 5px 0;
      font-size: 16px;
    }

    .highlight {
      color: #F56C6C;
      font-weight: bold;
      font-size: 18px;
    }
  }

  .action-area {
    display: flex;
    justify-content: center;
    gap: 20px;
  }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
