<script setup lang="ts">
import { computed } from 'vue'
import { formatMoney } from '@/utils/format'
import type { AlgorithmBreakdown } from '@/types/domain'

const props = defineProps<{
  breakdown: AlgorithmBreakdown | null
}>()

const currency = computed(() => props.breakdown?.currency ?? 'PLN')
</script>

<template>
  <div v-if="breakdown" class="card p-4">
    <h3 class="mb-3 text-sm font-semibold text-gray-100">Wynik algorytmu</h3>

    <div class="overflow-hidden rounded-lg border border-gray-700">
      <table class="w-full text-sm">
        <tbody>
          <tr v-for="(line, index) in breakdown.lines" :key="index" class="border-b border-gray-800 last:border-0">
            <td class="px-3 py-2 font-medium text-gray-300">{{ line.label }}</td>
            <td class="px-3 py-2 text-gray-400">{{ line.detail }}</td>
            <td class="px-3 py-2 text-right tabular-nums text-gray-200">
              {{ formatMoney(line.amount, currency) }}
            </td>
          </tr>
        </tbody>
        <tfoot>
          <tr class="bg-gray-800">
            <td class="px-3 py-2 font-semibold text-gray-100" colspan="2">Razem</td>
            <td class="px-3 py-2 text-right font-bold tabular-nums text-brand-400">
              {{ formatMoney(breakdown.total, currency) }}
            </td>
          </tr>
        </tfoot>
      </table>
    </div>

    <div v-if="breakdown.notes.length" class="mt-3 rounded-lg bg-amber-500/10 p-3 text-xs text-amber-400">
      <p v-for="(note, index) in breakdown.notes" :key="index">{{ note }}</p>
    </div>

    <details class="mt-3">
      <summary class="cursor-pointer text-xs font-medium text-gray-400">
        Zastosowane reguły ({{ breakdown.appliedRules.length }})
      </summary>
      <ul class="mt-2 space-y-1">
        <li
          v-for="(rule, index) in breakdown.appliedRules"
          :key="index"
          class="rounded bg-gray-800 px-2 py-1 font-mono text-xs text-gray-400"
        >
          {{ rule }}
        </li>
      </ul>
    </details>
  </div>
</template>

