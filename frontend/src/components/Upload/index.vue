<template>
  <el-upload
    class="upload-demo"
    :action="action"
    :headers="headers"
    :on-success="onSuccess"
    :show-file-list="false"
  >
    <el-button type="success">Excel 导入</el-button>
  </el-upload>
</template>

<script setup>
const props = defineProps(['action'])
const emit = defineEmits(['success'])

const headers = {
  Authorization: `Bearer ${localStorage.getItem('token')}`
}

const onSuccess = (res) => {
  emit('success', res)
}
</script>