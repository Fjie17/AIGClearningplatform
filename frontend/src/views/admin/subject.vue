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
        </el-table-column>

        <el-table-column label="操作" align="center" width="180">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleCommand('update', row)">修改</el-button>
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
        
        <el-form-item label="状态">
          <el-radio-group v-model="formModel.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <p v-else-if="popType === 'del'" class="dialog-msg">
        <el-icon color="#ed6a0c"><WarningFilled /></el-icon> 是否确认删除选中的数据？
      </p>

      <div v-else-if="popType === 'import'" class="import-box">
        <el-upload
          drag
          action="/api/admin/subjects/import"
          :headers="uploadHeaders"
          :on-success="handleImportSuccess"
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        </el-upload>
      </div>

      <template #footer>
        <el-button @click="popFlag = false">取消</el-button>
        <el-button type="primary" @click="confirm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage } from 'element-plus';
import request from '@/utils/request'; 

const getSubjectListApi = (params) => request({ 
  url: '/admin/subjects/list', 
  method: 'get', 
  params 
});
const addSubApi = (data) => request({ 
  url: '/admin/subjects/add', 
  method: 'post', 
  data 
});
const updateSubApi = (id, data) => request({ 
  url: `/admin/subjects/update/${id}`, 
  method: 'put', 
  data 
});
const delSubApi = (id) => request({ 
  url: `/admin/subjects/delete/${id}`, 
  method: 'delete' 
});
const exportTemplateApi = () => request({ 
  url: '/admin/subjects/template', 
  method: 'get', 
  responseType: 'blob'
});

const searchConfig = [
  { label: '科目名称', prop: 'name', type: 'input', placeholder: '请输入科目' },
  { label: '所属分类', prop: 'category', type: 'input', placeholder: '请输入分类' },
  { label: '二级分类', prop: 'subCategory', type: 'input', placeholder: '请输入二级分类' },
  { label: '状态', prop:'status', type:'select', placeholder: '请选择状态' , options: [
    { label: '启用', value: 1 },
    { label: '禁用', value: 0 }
  ] }
];

const btnList = [
  { label: '新增', type: 'add' },
  { label: '删除', type: 'batchDel' },
  { label: '模板导出', type: 'export' },
  { label: '导入', type: 'import' }
];

const tableColumns = [
  { label: '科目代码', prop: 'code', width: '150' },
  { label: '科目名称', prop: 'name' },
  { label: '分类', prop: 'category' },
  { label: '二级分类', prop: 'subCategory' },
  { label: '状态', prop: 'status', width: '100' }
];

const formConfig = [
  { label: '科目代码', prop: 'code' },
  { label: '科目名称', prop: 'name' },
  { label: '所属分类', prop: 'category' },
  { label: '二级分类', prop: 'subCategory' }
];

const loading = ref(false);
const tableData = ref([]);
const total = ref(0);
const multipleSelection = ref([]);
const popFlag = ref(false);
const popType = ref('add');

const queryParams = reactive({ 
  pageNum: 1, 
  pageSize: 10, 
  name: '', 
  category: '', 
  subCategory: '' 
});

const formModel = reactive({ id: null, code: '', name: '', category: '', status: 1 });
const formRules = { name: [{ required: true, message: '必填', trigger: 'blur' }] };
const uploadHeaders = { Authorization: `Bearer ${localStorage.getItem('token')}` };

const popTitle = computed(() => {
  const map = { add: '新增科目', update: '修改科目', del: '系统提示', import: '导入数据' };
  return map[popType.value];
});

const getList = async () => {
  loading.value = true;
  try {
    const res = await getSubjectListApi(queryParams);
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
      await addSubApi(formModel);
      ElMessage.success('新增成功');
    } else if (popType.value === 'update') {
      await updateSubApi(formModel.id, formModel);
      ElMessage.success('更新成功');
    } else if (popType.value === 'del') {
      // 如果是批量删除，这里逻辑可能需要根据后端接口调整，目前是单条删除
      await delSubApi(formModel.id);
      ElMessage.success('删除成功');
    }
    popFlag.value = false;
    getList();
  } catch (error) {
    console.error(error);
  }
};

const handleExport = async () => {
  try {
    const response = await exportTemplateApi();
    const blob = new Blob([response.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
    const link = document.createElement('a');
    link.style.display = 'none';
    
    const url = window.URL.createObjectURL(blob);
    link.href = url;
    
    const fileName = `科目导入模板_${new Date().getTime()}.xlsx`;
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
  queryParams.category = ''; 
  queryParams.subCategory = ''; 
  handleQuery(); 
};

const handleSelectionChange = (val) => { multipleSelection.value = val; };
const resetForm = () => { Object.assign(formModel, { id: null, code: '', name: '', category: '', status: 1 }); };
const handleImportSuccess = () => { ElMessage.success('导入成功'); popFlag.value = false; getList(); };

onMounted(getList);
</script>

<style scoped>
.wrapper { padding: 20px; background: #fff; }
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
</style>