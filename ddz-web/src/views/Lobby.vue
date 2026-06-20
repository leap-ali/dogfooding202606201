<template>
  <div class="lobby-page page-container">
    <div class="lobby-header">
      <div class="logo-area">
        <h1>四人斗地主</h1>
      </div>
      <div class="user-area">
        <div class="user-info">
          <el-avatar :size="40" src="/default-avatar.png" />
          <div class="user-detail">
            <span class="nickname">{{ userStore.userInfo?.nickname }}</span>
            <span class="score">积分: {{ userStore.userInfo?.score }}</span>
          </div>
        </div>
        <el-button type="danger" plain size="small" @click="handleLogout">退出</el-button>
      </div>
    </div>

    <div class="lobby-content">
      <div class="left-panel">
        <div class="panel-header">
          <h2>房间列表</h2>
          <el-button type="primary" @click="showCreateRoom = true">
            <el-icon><Plus /></el-icon>
            创建房间
          </el-button>
        </div>
        <div class="room-list">
          <div v-if="roomList.length === 0" class="empty-tip">
            暂无房间，快来创建第一个吧~
          </div>
          <div
            v-for="room in roomList"
            :key="room.id"
            class="room-card"
            @click="handleJoinRoom(room)"
          >
            <div class="room-no">房间号: {{ room.roomNo }}</div>
            <div class="room-info">
              <span class="base-score">底分: {{ room.baseScore }}</span>
              <span class="player-count">
                {{ getPlayerCount(room) }}/4
              </span>
            </div>
            <div class="room-status" :class="{ playing: room.status === 1 }">
              {{ room.status === 0 ? '空闲' : '对局中' }}
            </div>
          </div>
        </div>
      </div>

      <div class="right-panel">
        <div class="ranking-box">
          <div class="panel-header">
            <h2><el-icon><Trophy /></el-icon> 积分排行榜</h2>
          </div>
          <div class="ranking-list">
            <div v-for="(player, index) in topPlayers" :key="player.id" class="ranking-item">
              <span class="rank" :class="'rank-' + (index + 1)">{{ index + 1 }}</span>
              <el-avatar :size="32" :src="player.avatar || '/default-avatar.png'" />
              <span class="ranking-name">{{ player.nickname }}</span>
              <span class="ranking-score">{{ player.score }}分</span>
            </div>
          </div>
        </div>

        <div class="quick-join">
          <div class="panel-header">
            <h2>快速加入</h2>
          </div>
          <el-input v-model="joinRoomNo" placeholder="输入房间号" style="margin-bottom: 12px">
            <template #append>
              <el-button @click="handleQuickJoin">加入</el-button>
            </template>
          </el-input>
          <el-button type="success" class="full-btn" @click="handleRefresh">
            <el-icon><Refresh /></el-icon>
            刷新房间
          </el-button>
        </div>

        <div class="history-box">
          <div class="panel-header">
            <h2><el-icon><Document /></el-icon> 历史记录</h2>
          </div>
          <div class="history-list">
            <div v-if="gameRecords.length === 0" class="empty-tip">暂无记录</div>
            <div v-for="(record, index) in gameRecords.slice(0, 5)" :key="index" class="history-item">
              <span :class="{ win: record.isWin === 1, lose: record.isWin === 0 }">
                {{ record.isWin === 1 ? '胜' : '负' }}
              </span>
              <span class="identity">{{ record.identity === 1 ? '地主' : '农民' }}</span>
              <span class="score-change" :class="{ positive: record.scoreChange > 0, negative: record.scoreChange < 0 }">
                {{ record.scoreChange > 0 ? '+' : '' }}{{ record.scoreChange }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="showCreateRoom" title="创建房间" width="400px">
      <div class="create-room-form">
        <span class="label">选择底分:</span>
        <div class="score-options">
          <div
            v-for="score in [1, 2, 5, 10]"
            :key="score"
            class="score-option"
            :class="{ active: selectedBaseScore === score }"
            @click="selectedBaseScore = score"
          >
            {{ score }}分
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showCreateRoom = false">取消</el-button>
        <el-button type="primary" @click="handleCreateRoom">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { userApi, roomApi, gameApi } from '@/api'
import { ElMessage } from 'element-plus'
import { Plus, Trophy, Refresh, Document } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const roomList = ref([])
const topPlayers = ref([])
const gameRecords = ref([])
const showCreateRoom = ref(false)
const selectedBaseScore = ref(1)
const joinRoomNo = ref('')

onMounted(() => {
  loadRoomList()
  loadTopPlayers()
  loadGameRecords()
})

async function loadRoomList() {
  try {
    const res = await roomApi.getRoomList()
    roomList.value = res.data
  } catch (e) {}
}

async function loadTopPlayers() {
  try {
    const res = await userApi.getTopPlayers(10)
    topPlayers.value = res.data
  } catch (e) {}
}

async function loadGameRecords() {
  try {
    const res = await gameApi.getRecords()
    gameRecords.value = res.data
  } catch (e) {}
}

function getPlayerCount(room) {
  let count = 0
  if (room.player1Id) count++
  if (room.player2Id) count++
  if (room.player3Id) count++
  if (room.player4Id) count++
  return count
}

async function handleCreateRoom() {
  try {
    const res = await roomApi.createRoom(selectedBaseScore.value)
    ElMessage.success('房间创建成功')
    showCreateRoom.value = false
    router.push('/room/' + res.data.roomNo)
  } catch (e) {}
}

async function handleJoinRoom(room) {
  if (room.status === 1) {
    ElMessage.warning('房间对局中，无法加入')
    return
  }
  try {
    await roomApi.joinRoom(room.roomNo)
    router.push('/room/' + room.roomNo)
  } catch (e) {}
}

async function handleQuickJoin() {
  if (!joinRoomNo.value) {
    ElMessage.warning('请输入房间号')
    return
  }
  try {
    await roomApi.joinRoom(joinRoomNo.value)
    router.push('/room/' + joinRoomNo.value)
  } catch (e) {}
}

function handleRefresh() {
  loadRoomList()
  loadTopPlayers()
  loadGameRecords()
  ElMessage.success('刷新成功')
}

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped lang="scss">
.lobby-page {
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);

  .lobby-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px 30px;
    background: rgba(0, 0, 0, 0.3);

    .logo-area h1 {
      margin: 0;
      color: #409EFF;
      font-size: 28px;
      letter-spacing: 2px;
    }

    .user-area {
      display: flex;
      align-items: center;
      gap: 15px;

      .user-info {
        display: flex;
        align-items: center;
        gap: 10px;

        .user-detail {
          display: flex;
          flex-direction: column;

          .nickname {
            color: white;
            font-weight: bold;
          }

          .score {
            color: #E6A23C;
            font-size: 12px;
          }
        }
      }
    }
  }

  .lobby-content {
    flex: 1;
    display: flex;
    padding: 20px 30px;
    gap: 20px;
    overflow: hidden;
  }

  .left-panel {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-width: 0;

    .panel-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 15px;

      h2 {
        margin: 0;
        color: white;
        font-size: 20px;
      }
    }

    .room-list {
      flex: 1;
      overflow-y: auto;
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
      gap: 15px;
      align-content: start;
    }

    .empty-tip {
      grid-column: 1 / -1;
      text-align: center;
      color: #909399;
      padding: 40px 0;
    }
  }

  .room-card {
    background: rgba(255, 255, 255, 0.1);
    border-radius: 10px;
    padding: 18px;
    cursor: pointer;
    transition: all 0.3s;
    border: 2px solid transparent;

    &:hover {
      background: rgba(255, 255, 255, 0.15);
      transform: translateY(-2px);
      border-color: #409EFF;
    }

    .room-no {
      color: white;
      font-size: 16px;
      font-weight: bold;
      margin-bottom: 10px;
    }

    .room-info {
      display: flex;
      justify-content: space-between;
      margin-bottom: 10px;

      .base-score {
        color: #E6A23C;
      }

      .player-count {
        color: #67C23A;
      }
    }

    .room-status {
      display: inline-block;
      padding: 3px 10px;
      border-radius: 12px;
      font-size: 12px;
      background: #67C23A;
      color: white;

      &.playing {
        background: #F56C6C;
      }
    }
  }

  .right-panel {
    width: 320px;
    display: flex;
    flex-direction: column;
    gap: 15px;
  }

  .ranking-box, .quick-join, .history-box {
    background: rgba(255, 255, 255, 0.08);
    border-radius: 10px;
    padding: 15px;

    .panel-header {
      margin-bottom: 12px;

      h2 {
        margin: 0;
        color: white;
        font-size: 16px;
        display: flex;
        align-items: center;
        gap: 6px;
      }
    }
  }

  .ranking-list {
    .ranking-item {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 8px 0;
      border-bottom: 1px solid rgba(255, 255, 255, 0.1);

      &:last-child {
        border-bottom: none;
      }

      .rank {
        width: 24px;
        height: 24px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 12px;
        font-weight: bold;
        background: #666;
        color: white;

        &.rank-1 { background: #FFD700; color: #333; }
        &.rank-2 { background: #C0C0C0; color: #333; }
        &.rank-3 { background: #CD7F32; color: white; }
      }

      .ranking-name {
        flex: 1;
        color: white;
        font-size: 14px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .ranking-score {
        color: #E6A23C;
        font-size: 14px;
      }
    }
  }

  .full-btn {
    width: 100%;
  }

  .history-list {
    max-height: 200px;
    overflow-y: auto;

    .history-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 8px 0;
      border-bottom: 1px solid rgba(255, 255, 255, 0.05);
      font-size: 13px;

      .win { color: #67C23A; font-weight: bold; }
      .lose { color: #F56C6C; font-weight: bold; }
      .identity { color: #909399; }
      .score-change.positive { color: #67C23A; }
      .score-change.negative { color: #F56C6C; }
    }

    .empty-tip {
      text-align: center;
      color: #666;
      padding: 20px 0;
    }
  }

  .create-room-form {
    .label {
      display: block;
      margin-bottom: 12px;
      font-size: 14px;
      color: #606266;
    }

    .score-options {
      display: flex;
      gap: 12px;

      .score-option {
        flex: 1;
        padding: 15px 0;
        text-align: center;
        border: 2px solid #dcdfe6;
        border-radius: 8px;
        cursor: pointer;
        font-size: 18px;
        transition: all 0.3s;

        &:hover {
          border-color: #409EFF;
        }

        &.active {
          border-color: #409EFF;
          background: #ecf5ff;
          color: #409EFF;
          font-weight: bold;
        }
      }
    }
  }
}
</style>
