<script setup>
import { ref, onUnmounted, watchEffect, nextTick } from 'vue'
import { Chart } from 'chart.js/auto'

const props = defineProps({
  type:    { type: String, required: true },
  data:    { type: Object, required: true },
  options: { type: Object, default: () => ({}) },
})

const canvas = ref(null)
let chart = null

const stop = watchEffect(async () => {
  // Accède à props.data pour que watchEffect le surveille
  const chartData = props.data
  const chartType = props.type

  await nextTick()
  if (!canvas.value) return

  if (chart) { chart.destroy(); chart = null }

  chart = new Chart(canvas.value, {
    type: chartType,
    data: JSON.parse(JSON.stringify(chartData)),
    options: {
      responsive: true,
      maintainAspectRatio: false,
      ...props.options,
    },
  })
})

onUnmounted(() => {
  stop()
  if (chart) { chart.destroy(); chart = null }
})
</script>

<template>
  <div class="relative w-full h-full">
    <canvas ref="canvas" />
  </div>
</template>
