<template>
  <div class="game-page page-container">
    <div class="game-table">
      <div class="top-bar">
        <div class="room-info">
          <span>房间号: {{ gameStore.roomNo }}</span>
          <span>底分: {{ gameStore.baseScore }}</span>
        </div>
        <el-button type="danger" plain size="small" @click="handleLeave">
          退出房间
        </el-button>
      </div>

      <div class="bottom-cards" v-if="gameStore.bottomCards && gameStore.bottomCards.length > 0 && gameStore.landlordIndex >= 0">
        <span class="label">底牌:</span>
        <div class="cards-row">
          <PokerCard
            v-for="card in gameStore.bottomCards"
            :key="card.code"
            :card="card"
            small
          />
        </div>
      </div>

      <div class="opponent-area">
        <div class="opponent left">
          <div class="player-info-card" :class="{ 'is-turn': leftPlayerSeat === gameStore.currentPlayerIndex }">
            <div class="avatar-box">
              <el-avatar :size="50" :src="leftPlayer?.avatar || '/default-avatar.png'" />
              <div v-if="leftPlayer?.identity === 1" class="landlord-tag">地主</div>
            </div>
            <div class="info">
              <span class="name">{{ leftPlayer?.nickname || '等待中...' }}</span>
              <span class="cards-count">剩余 {{ leftPlayer?.cardCount || 0 }} 张</span>
            </div>
          </div>
          <div class="opponent-cards">
            <div
              v-for="n in (leftPlayer?.cardCount || 0)"
              :key="'left-' + n"
              class="card-back-item"
            >
              <PokerCard back small />
            </div>
          </div>
          <div v-if="leftPlayerLastPlay && leftPlayerLastPlay.length > 0" class="played-cards">
            <div class="play-label">上家出牌</div>
            <div class="played-cards-row">
              <PokerCard
                v-for="card in leftPlayerLastPlay"
                :key="card.code"
                :card="card"
                small
              />
            </div>
          </div>
        </div>

        <div class="opponent right">
          <div class="player-info-card" :class="{ 'is-turn': rightPlayerSeat === gameStore.currentPlayerIndex }">
            <div class="avatar-box">
              <el-avatar :size="50" :src="rightPlayer?.avatar || '/default-avatar.png'" />
              <div v-if="rightPlayer?.identity === 1" class="landlord-tag">地主</div>
            </div>
            <div class="info">
              <span class="name">{{ rightPlayer?.nickname || '等待中...' }}</span>
              <span class="cards-count">剩余 {{ rightPlayer?.cardCount || 0 }} 张</span>
            </div>
          </div>
          <div class="opponent-cards">
            <div
              v-for="n in (rightPlayer?.cardCount || 0)"
              :key="'right-' + n"
              class="card-back-item"
            >
              <PokerCard back small />
            </div>
          </div>
          <div v-if="rightPlayerLastPlay && rightPlayerLastPlay.length > 0" class="played-cards">
            <div class="play-label">上家出牌</div>
            <div class="played-cards-row">
              <PokerCard
                v-for="card in rightPlayerLastPlay"
                :key="card.code"
                :card="card"
                small
              />
            </div>
          </div>
        </div>
      </div>

      <div class="center-area">
        <div v-if="gameStore.gameStatus === 'GRABBING'" class="grabbing-panel">
          <h3>抢地主阶段</h3>
          <p v-if="isMyTurnToGrab" class="my-turn">轮到你了！</p>
          <p v-else>等待其他玩家选择...</p>
        </div>
        <div v-else-if="gameStore.gameStatus === 'PLAYING' && gameStore.lastPlayCards && gameStore.lastPlayCards.length > 0" class="center-played">
          <div class="play-from">
            {{ getPlayerBySeat(gameStore.lastPlayIndex)?.nickname }} 出牌
          </div>
          <div class="center-cards">
            <PokerCard
              v-for="card in gameStore.lastPlayCards"
              :key="card.code"
              :card="card"
            />
          </div>
          <div class="card-type">{{ gameStore.lastPlayType ? cardTypeNames[gameStore.lastPlayType] : '' }}</div>
        </div>
      </div>

      <div class="my-area">
        <div class="my-info">
          <div class="player-info-card mine">
            <div class="avatar-box">
              <el-avatar :size="50" :src="myPlayer?.avatar || '/default-avatar.png'" />
              <div v-if="myPlayer?.identity === 1" class="landlord-tag">地主</div>
            </div>
            <div class="info">
              <span class="name">{{ myPlayer?.nickname }}</span>
              <span class="score">积分: {{ myPlayer?.score }}</span>
            </div>
          </div>
        </div>

        <div class="my-cards">
          <div
            v-for="card in gameStore.myCards"
            :key="card.code"
            class="my-card-item"
            :class="{ selected: isCardSelected(card) }"
            @click="toggleCard(card)"
          >
            <PokerCard :card="card" />
          </div>
        </div>

        <div class="action-buttons">
          <template v-if="gameStore.gameStatus === 'GRABBING' && isMyTurnToGrab">
            <el-button type="warning" size="large" @click="handleGrab(false)">不抢</el-button>
            <el-button type="danger" size="large" @click="handleGrab(true)">抢地主</el-button>
          </template>
          <template v-else-if="gameStore.gameStatus === 'PLAYING' && gameStore.isMyTurn">
            <el-button type="info" @click="handleReset">重置</el-button>
            <el-button type="primary" @click="handleHint">提示</el-button>
            <el-button type="warning" @click="handlePass" :disabled="!canPass">不出</el-button>
            <el-button type="success" size="large" @click="handlePlay">出牌</el-button>
          </template>
          <template v-else>
            <span class="waiting-tip">等待其他玩家操作...</span>
          </template>
        </div>
      </div>
    </div>

    <el-dialog
      v-model="gameStore.showResult"
      title="对局结算"
      width="500px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <div class="game-result">
        <div class="result-title" :class="{ win: isMyWin, lose: !isMyWin }">
          {{ isMyWin ? '🎉 恭喜获胜！' : '😔 很遗憾，输了' }}
        </div>
        <div class="result-info">
          <div class="info-item">
            <span>底分:</span>
            <span>{{ gameStore.baseScore }}分</span>
          </div>
          <div class="info-item">
            <span>我的身份:</span>
            <span :class="{ landlord: gameStore.isLandlord }">{{ gameStore.isLandlord ? '地主' : '农民' }}</span>
          </div>
          <div class="info-item score-change">
            <span>积分变动:</span>
            <span :class="{ positive: myScoreChange > 0, negative: myScoreChange < 0 }">
              {{ myScoreChange > 0 ? '+' : '' }}{{ myScoreChange }}
            </span>
          </div>
        </div>
        <div class="player-results">
          <div
            v-for="(p, index) in gameStore.gameResult?.players"
            :key="p.userId"
            class="player-result-item"
            :class="{ me: p.userId === userStore.userInfo?.id }"
          >
            <el-avatar :size="36" :src="p.avatar || '/default-avatar.png'" />
            <div class="info">
              <span class="name">{{ p.nickname }}</span>
              <span class="identity">{{ p.identity === 1 ? '地主' : '农民' }}</span>
            </div>
            <span class="score" :class="{ positive: p.scoreChange > 0, negative: p.scoreChange < 0 }">
              {{ p.scoreChange > 0 ? '+' : '' }}{{ p.scoreChange }}
            </span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="handleBackToLobby">返回大厅</el-button>
        <el-button type="primary" @click="handlePlayAgain">再来一局</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>import { computed, onMounted, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useGameStore } from '@/stores/game';
import { useUserStore } from '@/stores/user';
import { initSocket, joinRoom, leaveRoom, playCards, passCards, hintCards, grabLandlord } from '@/socket';
import { ElMessage, ElMessageBox } from 'element-plus';
import PokerCard from '@/components/PokerCard.vue';
const route = useRoute();
const router = useRouter();
const gameStore = useGameStore();
const userStore = useUserStore();
const roomNo = computed(() => route.params.roomNo);
const cardTypeNames = {
 SINGLE: '单张',
 PAIR: '对子',
 TRIPLE: '三张',
 TRIPLE_ONE: '三带一',
 STRAIGHT: '顺子',
 STRAIGHT_PAIR: '连对',
 PLANE: '飞机',
 PLANE_ONE: '飞机带单',
 BOMB: '炸弹',
 ROCKET: '王炸',
 PASS: '不出'
};
const myPlayer = computed(() => {
 if (gameStore.myIndex >= 0 && gameStore.players[gameStore.myIndex]) {
 return gameStore.players[gameStore.myIndex];
 }
 return null;
});
const leftPlayerSeat = computed(() => (gameStore.myIndex + 1) % 3);
const rightPlayerSeat = computed(() => (gameStore.myIndex + 2) % 3);
const leftPlayer = computed(() => gameStore.players[leftPlayerSeat.value]);
const rightPlayer = computed(() => gameStore.players[rightPlayerSeat.value]);
const leftPlayerLastPlay = computed(() => {
 if (gameStore.lastPlayIndex === leftPlayerSeat.value && gameStore.lastPlayType !== 'PASS') {
 return gameStore.lastPlayCards;
 }
 return [];
});
const rightPlayerLastPlay = computed(() => {
 if (gameStore.lastPlayIndex === rightPlayerSeat.value && gameStore.lastPlayType !== 'PASS') {
 return gameStore.lastPlayCards;
 }
 return [];
});
const isMyTurnToGrab = computed(() => {
 if (gameStore.gameStatus !== 'GRABBING')
 return false;
 const currentGrabIdx = gameStore.grabOrder[gameStore.currentGrabIndex];
 return currentGrabIdx === gameStore.myIndex;
});
const canPass = computed(() => {
 return gameStore.lastPlayCards && gameStore.lastPlayCards.length > 0 && gameStore.lastPlayIndex !== gameStore.myIndex;
});
const isMyWin = computed(() => {
 if (!gameStore.gameResult)
 return false;
 const me = gameStore.gameResult.players.find(p => p.userId === userStore.userInfo?.id);
 if (!me)
 return false;
 const landlordWin = gameStore.gameResult.landlordWin;
 const isLandlord = me.identity === 1;
 return (isLandlord && landlordWin) || (!isLandlord && !landlordWin);
});
const myScoreChange = computed(() => {
 if (!gameStore.gameResult)
 return 0;
 const me = gameStore.gameResult.players.find(p => p.userId === userStore.userInfo?.id);
 return me?.scoreChange || 0;
});
function getPlayerBySeat(seatIndex) {
 return gameStore.players[seatIndex];
}
function isCardSelected(card) {
 return gameStore.selectedCards.some(c => c.code === card.code);
}
function toggleCard(card) {
 if (gameStore.gameStatus !== 'PLAYING' || !gameStore.isMyTurn)
 return;
 gameStore.toggleCardSelect(card);
}
function handleReset() {
 gameStore.clearSelectedCards();
}
function handleHint() {
 hintCards(roomNo.value);
}
function handlePlay() {
 if (gameStore.selectedCards.length === 0) {
 ElMessage.warning('请选择要出的牌');
 return;
 }
 playCards(roomNo.value, gameStore.selectedCards);
 gameStore.clearSelectedCards();
}
function handlePass() {
 passCards(roomNo.value);
 gameStore.clearSelectedCards();
}
function handleGrab(grab) {
 grabLandlord(roomNo.value, grab);
}
function handleLeave() {
 ElMessageBox.confirm('对局中退出会扣除双倍积分，确定要退出吗？', '提示', {
 confirmButtonText: '确定退出',
 cancelButtonText: '取消',
 type: 'warning'
 }).then(() => {
 leaveRoom(roomNo.value);
 gameStore.resetState();
 router.push('/lobby');
 }).catch(() => { });
}
function handleBackToLobby() {
 gameStore.hideResult();
 leaveRoom(roomNo.value);
 gameStore.resetState();
 router.push('/lobby');
}
function handlePlayAgain() {
 gameStore.hideResult();
 leaveRoom(roomNo.value);
 gameStore.resetState();
 router.push('/lobby');
}
onMounted(() => {
 if (!userStore.token) {
 router.push('/login');
 return;
 }
 initSocket();
 setTimeout(() => {
 joinRoom(roomNo.value);
 }, 100);
});
onUnmounted(() => {
 // 保持socket连接
});
</script>

<style scoped lang="scss">
.game-page {
  .game-table {
    width: 100%;
    height: 100vh;
    background: linear-gradient(135deg, #0d47a1 0%, #063477 100%);
    position: relative;
    display: flex;
    flex-direction: column;
    padding: 20px;
  }

  .top-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    color: white;
    font-size: 14px;
    padding: 10px 20px;
    background: rgba(0, 0, 0, 0.2);
    border-radius: 8px;

    .room-info {
      display: flex;
      gap: 20px;
    }
  }

  .bottom-cards {
    position: absolute;
    top: 60px;
    left: 50%;
    transform: translateX(-50%);
    display: flex;
    align-items: center;
    gap: 10px;

    .label {
      color: #ffd700;
      font-size: 14px;
    }

    .cards-row {
      display: flex;
      gap: 4px;
    }
  }

  .opponent-area {
    flex: 1;
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    padding: 60px 40px 0;
  }

  .opponent {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 10px;
    width: 250px;

    &.left {
      align-items: flex-start;
    }

    &.right {
      align-items: flex-end;
    }

    .player-info-card {
      display: flex;
      align-items: center;
      gap: 12px;
      background: rgba(0, 0, 0, 0.3);
      padding: 10px 15px;
      border-radius: 30px;
      border: 2px solid transparent;
      transition: all 0.3s;

      &.is-turn {
        border-color: #ffd700;
        box-shadow: 0 0 15px rgba(255, 215, 0, 0.5);
      }

      .avatar-box {
        position: relative;

        .landlord-tag {
          position: absolute;
          top: -5px;
          right: -5px;
          background: #E6A23C;
          color: white;
          font-size: 10px;
          padding: 1px 6px;
          border-radius: 8px;
        }
      }

      .info {
        display: flex;
        flex-direction: column;
        color: white;

        .name {
          font-size: 14px;
          font-weight: bold;
        }

        .cards-count {
          font-size: 12px;
          color: #909399;
        }
      }
    }

    .opponent-cards {
      display: flex;
      position: relative;
      height: 84px;

      .card-back-item {
        position: absolute;
        transition: left 0.3s;
      }
    }

    .played-cards {
      margin-top: 10px;

      .play-label {
        color: #909399;
        font-size: 12px;
        margin-bottom: 4px;
      }

      .played-cards-row {
        display: flex;
        gap: 2px;
      }
    }
  }

  .opponent.left .opponent-cards .card-back-item {
    left: calc(var(--idx) * -15px);
  }

  .opponent.right .opponent-cards .card-back-item {
    right: calc(var(--idx) * -15px);
  }

  .center-area {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    text-align: center;

    .grabbing-panel {
      color: white;

      h3 {
        margin: 0 0 10px 0;
        color: #ffd700;
      }

      .my-turn {
        color: #F56C6C;
        font-size: 20px;
        font-weight: bold;
        animation: pulse 1s ease-in-out infinite;
      }
    }

    .center-played {
      .play-from {
        color: #ffd700;
        margin-bottom: 10px;
        font-size: 14px;
      }

      .center-cards {
        display: flex;
        gap: 4px;
        justify-content: center;
      }

      .card-type {
        color: white;
        margin-top: 10px;
        font-size: 14px;
      }
    }
  }

  .my-area {
    padding: 10px 20px 20px;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 15px;

    .my-info {
      .player-info-card {
        display: flex;
        align-items: center;
        gap: 12px;
        background: rgba(0, 0, 0, 0.4);
        padding: 8px 16px;
        border-radius: 25px;
        border: 2px solid #409EFF;

        .avatar-box {
          position: relative;

          .landlord-tag {
            position: absolute;
            top: -5px;
            right: -5px;
            background: #E6A23C;
            color: white;
            font-size: 10px;
            padding: 1px 6px;
            border-radius: 8px;
          }
        }

        .info {
          display: flex;
          flex-direction: column;
          color: white;

          .name {
            font-weight: bold;
          }

          .score {
            color: #E6A23C;
            font-size: 12px;
          }
        }
      }
    }

    .my-cards {
      display: flex;
      justify-content: center;
      min-height: 130px;
      position: relative;
      perspective: 1000px;

      .my-card-item {
        margin: 0 -15px;
        transition: transform 0.2s;
        transform-origin: bottom center;

        &:first-child {
          margin-left: 0;
        }

        &:last-child {
          margin-right: 0;
        }

        &:hover {
          z-index: 10;
        }
      }
    }

    .action-buttons {
      display: flex;
      gap: 15px;
      align-items: center;
      height: 50px;

      .waiting-tip {
        color: #909399;
        font-size: 16px;
      }
    }
  }

  .game-result {
    .result-title {
      text-align: center;
      font-size: 28px;
      font-weight: bold;
      margin-bottom: 20px;

      &.win {
        color: #67C23A;
      }

      &.lose {
        color: #F56C6C;
      }
    }

    .result-info {
      display: flex;
      justify-content: center;
      gap: 30px;
      margin-bottom: 20px;
      padding: 15px;
      background: #f5f7fa;
      border-radius: 8px;

      .info-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 5px;
        font-size: 14px;

        &.score-change span:last-child {
          font-size: 20px;
          font-weight: bold;

          &.positive {
            color: #67C23A;
          }

          &.negative {
            color: #F56C6C;
          }
        }
      }

      .landlord {
        color: #E6A23C;
        font-weight: bold;
      }
    }

    .player-results {
      .player-result-item {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 10px;
        border-radius: 8px;
        margin-bottom: 8px;
        background: #f5f7fa;

        &.me {
          background: #ecf5ff;
        }

        .info {
          flex: 1;
          display: flex;
          flex-direction: column;

          .name {
            font-weight: bold;
          }

          .identity {
            font-size: 12px;
            color: #909399;
          }
        }

        .score {
          font-size: 18px;
          font-weight: bold;

          &.positive {
            color: #67C23A;
          }

          &.negative {
            color: #F56C6C;
          }
        }
      }
    }
  }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
</style>
