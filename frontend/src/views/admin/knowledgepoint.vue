<template>
  <div class="wrapper">
    <div class="search-box card">
      <el-form :inline="true" :model="queryParams">
        <el-form-item v-for="item in searchConfig" :key="item.prop" :label="item.label">
          <el-input 
            v-if="item.type === 'input'" 
            v-model="queryParams[item.prop]" 
            :placeholder="item.placeholder" 
            clearable 
          />
          <el-select v-else-if="item.type === 'select'" v-model="queryParams[item.prop]" :placeholder="item.placeholder" clearable>
            <el-option v-for="opt in item.options" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="btn-list mt15 mb15 clearFix">
      <li v-for="(item, index) in btnList" :key="index" @click="handleCommand(item.type)">
        {{ item.label }}
      </li>
    </div>

    <div class="table">
      <el-table 
        v-loading="loading" 
        :data="tableData" 
        border 
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column 
          v-for="col in tableColumns" 
          :key="col.prop" 
          v-bind="col" 
          align="center"
        >
          <template v-if="col.prop === 'status'" #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
          <template v-else-if="col.prop === 'difficulty'" #default="{ row }">
            {{ difficultyMap[row.difficulty] || row.difficulty }}
          </template>
          <template v-else-if="col.prop === 'examWeight'" #default="{ row }">
            {{ examWeightMap[row.examWeight] || row.examWeight }}
          </template>
        </el-table-column>

        <el-table-column label="操作" align="center" width="180">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleCommand('update', row)">修改</el-button>
            <el-button link type="info" @click="handleCommand('tree', row)">详情</el-button>
            <el-button link type="danger" @click="handleCommand('del', row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container mt15">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          layout="solt, prev, pager, next"
          :total="total"
          @current-change="getList"
        />
        <span class="custom-total">共 {{ total }} 条</span>
      </div>
    </div>

    <el-dialog 
      v-model="popFlag" 
      :title="popTitle" 
      width="500px"
      destroy-on-close
    >
      <el-form 
        v-if="['add', 'update'].includes(popType)" 
        ref="formRef" 
        :model="formModel" 
        :rules="formRules" 
        label-width="100px"
      >
        <el-form-item v-for="field in formConfig" :key="field.prop" :label="field.label" :prop="field.prop">
          <template v-if="field.prop === 'code'">
             <el-input v-model="formModel.code" placeholder="科目代码" @input="val => formModel.code = val.toUpperCase()" />
          </template>
          <el-input v-else v-model="formModel[field.prop]" :placeholder="'请输入' + field.label" />
        </el-form-item>

      </el-form>

      <p v-else-if="popType === 'del'" class="dialog-msg">
        <el-icon color="#ed6a0c"><WarningFilled /></el-icon> 是否确认删除选中的数据？
      </p>

      <div v-else-if="popType === 'import'" class="import-box">
        <el-upload
          ref="uploadRef"
          drag
          :action="''"
          :headers="uploadHeaders"
          :auto-upload="false"
          :file-list="uploadFileList"
          :on-change="handleFileChange"
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        </el-upload>
        <div v-if="uploadFileList.length > 0" class="upload-info">
          <span>已选择文件：{{ uploadFileList[0].name }}</span>
        </div>
      </div>

      <template #footer>
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="confirm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage } from 'element-plus';
import request from '@/utils/request'; 
import Subject from './subject.vue';
import { el } from 'element-plus/es/locales.mjs';

const getknowledgeListApi = (params) => request({ 
  url: '/admin/knowledge-points/list', 
  method: 'get', 
  params 
});
const addknowApi = (data) => request({ 
  url: '/admin/knowledge-points/add', 
  method: 'post', 
  data 
});
const updateknowApi = (id, data) => request({ 
  url: `/admin/knowledge-points/update/${id}`, 
  method: 'put', 
  data 
});
const delknowApi = (id) => request({ 
  url: `/admin/knowledge-points/delete/${id}`, 
  method: 'delete' 
});
const exportTemplateApi = () => request({ 
  url: '/admin/knowledge-points/template', 
  method: 'get', 
  responseType: 'blob'
});
const delBatchknowApi = (ids) => request({ 
  url: '/admin/knowledge-points/delete/batch', 
  method: 'delete', 
  data: ids 
});

const searchConfig = [
  { label: '科目名称', prop: 'subjectName', type: 'input', placeholder: '请输入科目' },
  { label: '知识点名称', prop: 'name', type: 'input', placeholder: '请输入知识点名称' },
  { 
    label: '难度', 
    prop: 'difficulty', 
    type: 'select', 
    placeholder: '请选择难度',
    options: [
      { label: '极简单', value: 1 },
      { label: '简单', value: 2 },
      { label: '中等', value: 3 },
      { label: '困难', value: 4 },
      { label: '极困难', value: 5 }
    ]
  },
  { 
    label: '重要程度', 
    prop: 'examWeight', 
    type: 'select', 
    placeholder: '请选择重要程度',
    options: [
      { label: '一般', value: 1 },
      { label: '重要', value: 2 },
      { label: '非常重要', value: 3 },
      { label: '关键', value: 4 },
      { label: '非常关键', value: 5 }
    ]
  }
];

const btnList = [
  { label: '新增', type: 'add' },
  { label: '删除', type: 'batchDel' },
  { label: '模板导出', type: 'export' },
  { label: '导入', type: 'import' }
];

const tableColumns = [
  { label: '知识点名称', prop: 'name', width: '150' },
  { label: '科目名称', prop: 'subjectName' },
  { label: '难度', prop: 'difficulty' },
  { label: '重要程度', prop: 'examWeight' },
  { label: '父级知识点', prop: 'parentName' }
];

const difficultyMap = {
  1: '极简单',
  2: '简单',
  3: '中等',
  4: '困难',
  5: '极困难'
};

const examWeightMap = {
  1: '一般',
  2: '重要',
  3: '非常重要',
  4: '关键',
  5: '非常关键'
};

const formConfig = [
  { label: '知识点名称', prop: 'name' },
  { label: '科目名称', prop: 'subjectName' },
  { label: '难度', prop: 'difficulty' },
  { label: '重要程度', prop: 'examWeight' },
  { label: '父级知识点', prop: 'parentName' }
];

const loading = ref(false);
const tableData = ref([]);
const total = ref(0);
const multipleSelection = ref([]);
const popFlag = ref(false);
const popType = ref('add');
const uploadRef = ref(null);
const uploadFileList = ref([]);
const selectedFile = ref(null);

const queryParams = reactive({ 
  pageNum: 1, 
  pageSize: 10, 
  name: '', 
  SubjectName: '',
  difficulty: '', 
  examWeight: '' 
});

const formModel = reactive({ 
  id: null, 
  subjectId: null,
  name: '', 
  difficulty: '', 
  examWeight: '', 
  parentId: null
});
const formRules = { name: [{ required: true, message: '必填', trigger: 'blur' }] };
const uploadHeaders = { Authorization: `Bearer ${localStorage.getItem('token')}` };

const popTitle = computed(() => {
  const map = { add: '新增知识点', update: '修改知识点', del: '系统提示', import: '导入数据' };
  return map[popType.value];
});

const getList = async () => {
  loading.value = true;
  try {
    const res = await getknowledgeListApi(queryParams);
    tableData.value = res.data.list;
    total.value = res.data.total;
  } finally {
    loading.value = false;
  }
};

const handleCommand = (type, row) => {
  popType.value = type;
  if (type === 'add') {
    resetForm();
    popFlag.value = true;
  } else if (type === 'update') {
    Object.assign(formModel, row);
    popFlag.value = true;
  } else if (type === 'del') {
    formModel.id = row.id;
    popFlag.value = true;
  } else if (type === 'batchDel') {
    if (multipleSelection.value.length === 0) return ElMessage.warning('请选择数据');
    popType.value = 'del';
    popFlag.value = true;
  } else if (type === 'import') {
    popFlag.value = true;
  }else if (type === 'export') {
    handleExport();
  }
};

const confirm = async () => {
  try {
    if (popType.value === 'add') {
      await addknowApi(formModel);
      ElMessage.success('新增成功');
    } else if (popType.value === 'update') {
      await updateknowApi(formModel.id, formModel);
      ElMessage.success('更新成功');
    } else if (popType.value === 'del') {
      if (multipleSelection.value.length > 0 && popType.value === 'del' && !formModel.id) {
        // 批量删除
        const ids = multipleSelection.value.map(item => item.id);
        await delBatchknowApi(ids);
        ElMessage.success('批量删除成功');
      } else {
        // 单条删除
        await delknowApi(formModel.id);
        ElMessage.success('删除成功');
      }
    } else if (popType.value === 'import') {
      await handleImportConfirm();
    }
    popFlag.value = false;
    getList();
  } catch (error) {
    console.error(error);
  }
};

const handleFileChange = (file) => {
  uploadFileList.value = [file];
  selectedFile.value = file.raw;
};

const handleImportConfirm = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择文件');
    return;
  }
  
  const formData = new FormData();
  formData.append('file', selectedFile.value);
  
  loading.value = true;
  try {
    const response = await request({
      url: '/admin/knowledge-points/import',
      method: 'post',
      data: formData,
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    });
    
    handleImportSuccess(response);
  } catch (error) {
    handleImportError(error);
  } finally {
    loading.value = false;
    resetUpload();
  }
};

const resetUpload = () => {
  uploadFileList.value = [];
  selectedFile.value = null;
  if (uploadRef.value) {
    uploadRef.value.clearFiles();
  }
};

const handleClose = () => {
  resetUpload();
  popFlag.value = false;
};

const handleExport = async () => {
  try {
    const response = await exportTemplateApi();
    const blob = new Blob([response.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
    const link = document.createElement('a');
    link.style.display = 'none';
    
    const url = window.URL.createObjectURL(blob);
    link.href = url;
    
    const fileName = `知识点导入模板_${new Date().getTime()}.xlsx`;
    link.setAttribute('download', fileName);
    
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url); // 释放内存
    
    ElMessage.success('模板导出成功');
  } catch (error) {
    console.error('导出失败', error);
    ElMessage.error('导出失败，请重试');
  }
};

const handleQuery = () => { queryParams.pageNum = 1; getList(); };

const resetQuery = () => { 
  queryParams.name = ''; 
  queryParams.subjectName = ''; 
  queryParams.difficulty = ''; 
  queryParams.examWeight = ''; 
  handleQuery(); 
};

const handleSelectionChange = (val) => { multipleSelection.value = val; };
const resetForm = () => { Object.assign(formModel, { id: null, code: '', name: '', category: '', status: 1 }); };
const handleImportSuccess = (response) => { 
  if (response && response.data) {
    const { total, success, fail } = response.data;
    if (success === total && total > 0) {
      ElMessage.success(`导入成功，共 ${total} 条，成功 ${success} 条，失败 ${fail} 条`);
    } else if (fail > 0 && success > 0) {
      ElMessage.warning(`导入完成，共 ${total} 条，成功 ${success} 条，失败 ${fail} 条`);
    } else if (fail > 0 && success === 0) {
      ElMessage.error(`导入失败，共 ${total} 条，全部失败`);
    } else {
      ElMessage.success(`导入成功，共 ${total || success} 条`);
    }
  } else {
    ElMessage.success('导入成功');
  }
};
const handleImportError = (error) => { 
  console.error('导入失败', error);
  ElMessage.error('导入失败，请重试'); 
};

onMounted(getList);
</script>

<style scoped>
.wrapper { padding: 20px; background: #ffffff; }
.card { padding: 10px; }

.table {
  padding-bottom: 60px; 
}

.pagination-container {
  position: fixed;
  bottom: 0;
  right: 20px; 
  left: 220px; 
  height: 50px;
  background-color: #fff;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  border-top: 1px solid #eee; 
  padding-right: 20px;
}

.btn-list { list-style: none; padding: 0; margin: 15px 0; }
.btn-list li {
  display: inline-block;
  padding: 6px 16px;
  margin-right: 10px;
  background-color: #409eff;
  color: #fff;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: opacity 0.2s;
}
.btn-list li:hover { opacity: 0.8; }

.search-box :deep(.el-input),
.search-box :deep(.el-select) {
  width: 200px; 
}

.search-box :deep(.el-select .el-input__wrapper) {
  width: 100%;
}

.dialog-msg { display: flex; align-items: center; gap: 10px; font-size: 16px; }
.clearFix::after { content: ""; display: block; clear: both; }
.mt15 { margin-top: 15px; }
.mb15 { margin-bottom: 15px; }

.upload-info {
  margin-top: 15px;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;
  color: #606266;
  font-size: 14px;
}
</style>