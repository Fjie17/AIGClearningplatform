<template>
  <el-input 
    v-model="internalValue" 
    @input="handleInput" 
    v-bind="$attrs" 
  />
</template>

<script setup>
import { computed } from 'vue'
const props = defineProps(['modelValue'])
const emit = defineEmits(['update:modelValue'])

const internalValue = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val.toUpperCase())
})

const handleInput = (val) => {
  emit('update:modelValue', val.toUpperCase())
}
</script>