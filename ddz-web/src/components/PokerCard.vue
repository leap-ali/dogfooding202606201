<template>
  <div
    class="poker-card"
    :class="{ selected, back, small, disabled }"
    :style="cardStyle"
    @click="handleClick"
  >
    <template v-if="!back">
      <div class="card-top">
        <span class="rank" :class="suitClass">{{ rankText }}</span>
        <span class="suit" :class="suitClass">{{ suitText }}</span>
      </div>
      <div class="card-middle" :class="suitClass">
        <span v-if="isJoker" class="joker-text">{{ rankText }}</span>
        <span v-else class="big-suit">{{ suitText }}</span>
      </div>
      <div class="card-bottom">
        <span class="suit" :class="suitClass">{{ suitText }}</span>
        <span class="rank" :class="suitClass">{{ rankText }}</span>
      </div>
    </template>
    <template v-else>
      <div class="card-back-content">
        <div class="back-pattern">♠</div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  card: {
    type: Object,
    default: null
  },
  back: {
    type: Boolean,
    default: false
  },
  selected: {
    type: Boolean,
    default: false
  },
  small: {
    type: Boolean,
    default: false
  },
  disabled: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['click'])

const suit = computed(() => props.card?.suit ?? 0)
const rank = computed(() => props.card?.rank ?? 3)

const suitClass = computed(() => {
  if (rank.value >= 14) {
    return rank.value === 14 ? 'joker-small' : 'joker-big'
  }
  return suit.value === 0 || suit.value === 2 ? 'black' : 'red'
})

const suitText = computed(() => {
  if (rank.value >= 14) return ''
  const suits = ['♠', '♥', '♣', '♦']
  return suits[suit.value] || ''
})

const rankText = computed(() => {
  if (rank.value === 14) return '小王'
  if (rank.value === 15) return '大王'
  const ranks = ['', '', '', '3', '4', '5', '6', '7', '8', '9', '10', 'J', 'Q', 'K', 'A', '2']
  return ranks[rank.value] || ''
})

const isJoker = computed(() => rank.value >= 14)

const cardStyle = computed(() => {
  if (props.small) {
    return {
      width: '60px',
      height: '84px'
    }
  }
  return {
    width: '80px',
    height: '112px'
  }
})

function handleClick() {
  if (!props.disabled) {
    emit('click')
  }
}
</script>

<style scoped lang="scss">
.poker-card {
  position: relative;
  width: 80px;
  height: 112px;
  background: white;
  border-radius: 6px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.3);
  cursor: pointer;
  user-select: none;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  overflow: hidden;

  &:hover:not(.disabled) {
    transform: translateY(-4px);
    box-shadow: 0 6px 12px rgba(0, 0, 0, 0.4);
  }

  &.selected {
    transform: translateY(-20px);
    box-shadow: 0 8px 16px rgba(64, 158, 255, 0.5);
    border: 2px solid #409EFF;
  }

  &.disabled {
    cursor: not-allowed;
    opacity: 0.7;
  }

  &.back {
    background: linear-gradient(135deg, #c41e3a 0%, #8b0000 100%);

    .card-back-content {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      border: 3px solid #ffd700;
      border-radius: 4px;
      margin: 4px;
      box-sizing: border-box;

      .back-pattern {
        font-size: 36px;
        color: #ffd700;
        opacity: 0.8;
      }
    }
  }

  .card-top {
    position: absolute;
    top: 4px;
    left: 6px;
    display: flex;
    flex-direction: column;
    align-items: center;
    font-weight: bold;
    line-height: 1;

    .rank {
      font-size: 14px;
    }

    .suit {
      font-size: 16px;
      margin-top: 2px;
    }
  }

  .card-middle {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    font-size: 36px;
    font-weight: bold;

    .joker-text {
      font-size: 18px;
      writing-mode: vertical-rl;
      letter-spacing: 2px;
    }

    .big-suit {
      font-size: 40px;
    }
  }

  .card-bottom {
    position: absolute;
    bottom: 4px;
    right: 6px;
    display: flex;
    flex-direction: column;
    align-items: center;
    font-weight: bold;
    line-height: 1;
    transform: rotate(180deg);

    .rank {
      font-size: 14px;
    }

    .suit {
      font-size: 16px;
      margin-top: 2px;
    }
  }

  .red {
    color: #e74c3c;
  }

  .black {
    color: #2c3e50;
  }

  .joker-small {
    color: #2c3e50;
  }

  .joker-big {
    color: #e74c3c;
  }

  &.small {
    .card-top .rank {
      font-size: 11px;
    }
    .card-top .suit {
      font-size: 12px;
    }
    .card-middle .big-suit {
      font-size: 28px;
    }
    .card-middle .joker-text {
      font-size: 14px;
    }
    .card-bottom .rank {
      font-size: 11px;
    }
    .card-bottom .suit {
      font-size: 12px;
    }
    .card-back-content .back-pattern {
      font-size: 24px;
    }
  }
}
</style>
