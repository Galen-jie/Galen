<template>
  <div class="countdown-timer">
    <span class="countdown-label" v-if="showLabel">剩余时间：</span>
    <span class="countdown-time">
      <span class="time-item" v-if="days > 0">{{ days }}<span class="time-unit">天</span></span>
      <span class="time-item">{{ hours.toString().padStart(2, '0') }}</span>
      <span class="time-separator">:</span>
      <span class="time-item">{{ minutes.toString().padStart(2, '0') }}</span>
      <span class="time-separator">:</span>
      <span class="time-item">{{ seconds.toString().padStart(2, '0') }}</span>
    </span>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'

const props = defineProps({
  endTime: {
    type: [String, Number],
    required: true
  },
  showLabel: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['finish'])

const remaining = ref(0)
let timer = null

const days = computed(() => Math.floor(remaining.value / (24 * 60 * 60)))
const hours = computed(() => Math.floor((remaining.value % (24 * 60 * 60)) / (60 * 60)))
const minutes = computed(() => Math.floor((remaining.value % (60 * 60)) / 60))
const seconds = computed(() => remaining.value % 60)

const calculateRemaining = () => {
  let endTimestamp
  if (typeof props.endTime === 'string') {
    endTimestamp = new Date(props.endTime).getTime()
  } else {
    // 如果是秒级时间戳，转换为毫秒
    endTimestamp = props.endTime > 9999999999 ? props.endTime : props.endTime * 1000
  }

  const now = Date.now()
  remaining.value = Math.max(0, Math.floor((endTimestamp - now) / 1000))

  if (remaining.value <= 0) {
    clearInterval(timer)
    emit('finish')
  }
}

onMounted(() => {
  calculateRemaining()
  timer = setInterval(calculateRemaining, 1000)
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style scoped>
.countdown-timer {
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

.countdown-label {
  font-size: 12px;
  color: #666;
}

.countdown-time {
  display: inline-flex;
  align-items: center;
}

.time-item {
  display: inline-flex;
  align-items: center;
  background: #f5576c;
  color: #fff;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
  min-width: 24px;
  justify-content: center;
}

.time-unit {
  font-size: 10px;
  margin-left: 2px;
  font-weight: normal;
}

.time-separator {
  color: #f5576c;
  font-weight: bold;
  margin: 0 2px;
}
</style>