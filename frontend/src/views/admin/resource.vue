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
          <el-select 
            v-else-if="item.type === 'select'" 
            v-model="queryParams[item.prop]" 
            :placeholder="item.placeholder" 
            clearable
          >
            <el-option v-for="opt in item.options" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item class="search-buttons">
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
        scroll-x
      >
        <el-table-column type="selection" width="55" align="center" fixed="left" />
        <el-table-column 
          v-for="col in tableColumns" 
          :key="col.prop" 
          v-bind="col" 
          align="center"
        />
        <el-table-column label="操作" align="center" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleCommand('update', row)">修改</el-button>
            <el-button link type="danger" @click="handleCommand('del', row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          layout="slot, prev, pager, next"
          :total="total"
          @current-change="getList"
        >
          <span class="custom-total">共 {{ total }} 条</span>
        </el-pagination>
      </div>
    </div>

    <el-dialog 
      v-model="popFlag" 
      :title="popTitle" 
      width="550px"
      destroy-on-close
    >
      <el-form 
        v-if="['add', 'update'].includes(popType)" 
        ref="formRef" 
        :model="formModel" 
        :rules="formRules" 
        label-width="100px"
      >
        <el-form-item label="资源标题" prop="title">
          <el-input v-model="formModel.title" placeholder="请输入资源标题" />
        </el-form-item>
        <el-form-item label="作者" prop="author">
          <el-input v-model="formModel.author" placeholder="请输入作者" />
        </el-form-item>
        <el-form-item label="观看次数" prop="views">
          <el-input v-model.number="formModel.views" type="number" placeholder="请输入观看次数" />
        </el-form-item>
        <el-form-item label="发布时间" prop="publishTime">
          <el-input v-model="formModel.publishTime" type="datetime" placeholder="请输入发布时间" />
        </el-form-item>
        <el-form-item label="评论数" prop="reviewCount">
          <el-input v-model.number="formModel.reviewCount" type="number" placeholder="请输入评论数" />
        </el-form-item>
        <el-form-item label="时长(分钟)" prop="duration">
          <el-input v-model.number="formModel.duration" type="number" placeholder="请输入时长" />
        </el-form-item>
        <el-form-item label="资源链接" prop="resourceUrl">
          <el-input v-model="formModel.resourceUrl" placeholder="https://" />
        </el-form-item>
        <el-form-item label="话题" prop="topic">
          <el-input v-model="formModel.topic" placeholder="请输入话题" />
        </el-form-item>
        <el-form-item label="所属平台" prop="platform">
          <el-select v-model="formModel.platform" placeholder="请选择平台" style="width: 100%">
            <el-option label="B站" value="B站" />
            <el-option label="小红书" value="小红书" />
            <el-option label="抖音" value="抖音" />
            <el-option label="知乎" value="知乎" />
          </el-select>
        </el-form-item>
        <el-form-item label="科目" prop="subjectId">
          <el-input v-model.number="formModel.subjectId" type="number" placeholder="请输入科目ID" />
        </el-form-item>
        <el-form-item label="知识点" prop="knowledgePointId">
          <el-input v-model.number="formModel.knowledgePointId" type="number" placeholder="请输入知识点ID" />
        </el-form-item>
      </el-form>

      <p v-else-if="popType === 'del'" class="dialog-msg">
        <el-icon color="#ed6a0c"><WarningFilled /></el-icon> 是否确认删除该资源？
      </p>

      <div v-else-if="popType === 'import'" class="import-box">
        <el-upload
          drag
          action="/api/admin/resources/import"
          :headers="uploadHeaders"
          :on-success="handleImportSuccess"
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        </el-upload>
      </div>

      <template #footer>
        <el-button @click="popFlag = false">取消</el-button>
        <el-button v-if="['add', 'update', 'del'].includes(popType)" type="primary" @click="confirm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage } from 'element-plus';
import request from '@/utils/request'; 

const getResourceApi = (params) => request({ url: '/admin/resources/list', method: 'get', params });
const addResourceApi = (data) => request({ url: '/admin/resources/add', method: 'post', data });
const updateResourceApi = (id, data) => request({ url: `/admin/resources/update/${id}`, method: 'put', data });
const delResourceApi = (id) => request({ url: `/admin/resources/delete/${id}`, method: 'delete' });
const batchDelResourceApi = (ids) => request({ url: '/admin/resources/delete/batch', method: 'delete', data: ids });
const exportTemplateApi = () => request({ url: '/admin/resources/template', method: 'get', responseType: 'blob' });

const searchConfig = [
  { label: '资源标题', prop: 'title', type: 'input', placeholder: '请输入资源标题'},
  { label: '作者', prop: 'author', type: 'input', placeholder: '请输入作者'},
  { label: '科目', prop: 'subjectId', type: 'input', placeholder: '请输入科目ID'},
  { label: '知识点', prop: 'knowledgePointId', type: 'input', placeholder: '请输入知识点ID'},
  { label: '平台', prop: 'platform', type: 'select', placeholder: '请选择平台', options: [
    { label: 'B站', value: 'B站' },
    { label: '小红书', value: '小红书' },
    { label: '抖音', value: '抖音' },
    { label: '知乎', value: '知乎' },
  ]}
];

const btnList = [
  { label: '新增资源', type: 'add' },
  { label: '批量删除', type: 'batchDel' },
  { label: '导出模板', type: 'export' },
  { label: '导入', type: 'import' },
];

const tableColumns = [
  { label: '资源标题', prop: 'title' , width: '250'},
  { label: '作者', prop: 'author', width: '120' },
  { label: '观看次数（万次）', prop: 'views', width: '100' },
  { label: '发布时间', prop: 'publishTime', width: '150' },
  { label: '评论数（万次）', prop: 'reviewCount', width: '100' },
  { label: '时长(分钟)', prop: 'duration', width: '100' },
  { label: '资源链接', prop: 'resourceUrl', width: '250' },
  { label: '话题', prop: 'topic', width: '150' },
  { label: '科目', prop: 'subjectName', width: '120' },
  { label: '知识点', prop: 'knowledgePointName', width: '150' },
  { label: '平台', prop: 'platform', width: '120' }
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
  subjectId: '', 
  knowledgePointId: '', 
  platform: '' 
});

const formModel = reactive({ 
  id: null, 
  title: '', 
  author: '',
  views: 0,
  publishTime: '',
  reviewCount: 0,
  duration: 0,
  resourceUrl: '',
  topic: '',
  platform: '',
  subjectId: 1, 
  knowledgePointId: 1 
});

const formRules = { 
  title: [{ required: true, message: '请输入资源标题', trigger: 'blur' }],
  author: [{ required: true, message: '请输入作者', trigger: 'blur' }],
  resourceUrl: [{ required: true, message: '请输入资源链接', trigger: 'blur' }],
  duration: [{ required: true, message: '请输入时长', trigger: 'blur' }],
  subjectId: [{ required: true, message: '请输入科目ID', trigger: 'blur' }],
  knowledgePointId: [{ required: true, message: '请输入知识点ID', trigger: 'blur' }]
};

const uploadHeaders = { Authorization: `Bearer ${localStorage.getItem('token')}` };

const popTitle = computed(() => {
  const map = { add: '新增资源', update: '修改资源', del: '系统提示', import: '导入数据' };
  return map[popType.value];
});

const getList = async () => {
  loading.value = true;
  try {
    const params = {};
    Object.keys(queryParams).forEach(key => {
      const value = queryParams[key];
      if (value !== '' && value !== undefined && value !== null) {
        params[key] = value;
      }
    });
    const res = await getResourceApi(params);
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
  } else if (type === 'export') {
    handleExport();
  }
};

const confirm = async () => {
  try {
    if (popType.value === 'add') {
      await addResourceApi(formModel);
      ElMessage.success('新增成功');
    } else if (popType.value === 'update') {
      await updateResourceApi(formModel.id, formModel);
      ElMessage.success('更新成功');
    } else if (popType.value === 'del') {
      if (multipleSelection.value.length > 0) {
        const ids = multipleSelection.value.map(item => item.id);
        await batchDelResourceApi(ids);
      } else {
        await delResourceApi(formModel.id);
      }
      ElMessage.success('删除成功');
    }
    popFlag.value = false;
    getList();
  } catch (error) {
    console.error(error);
  }
};

const handleQuery = () => { queryParams.pageNum = 1; getList(); };
const resetQuery = () => { 
  queryParams.title = ''; 
  queryParams.author = '';
  queryParams.subjectId = ''; 
  queryParams.knowledgePointId = ''; 
  queryParams.platform = ''; 
  handleQuery(); 
};
const handleSelectionChange = (val) => { multipleSelection.value = val; };
const resetForm = () => { 
  Object.assign(formModel, { 
    id: null, 
    title: '', 
    author: '',
    views: 0,
    publishTime: '',
    reviewCount: 0,
    duration: 0,
    resourceUrl: '',
    topic: '',
    platform: '',
    subjectId: 1, 
    knowledgePointId: 1 
  }); 
};

const handleExport = async () => {
  try {
    const response = await exportTemplateApi();
    const blob = new Blob([response.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
    const link = document.createElement('a');
    link.style.display = 'none';
    
    const url = window.URL.createObjectURL(blob);
    link.href = url;
    
    const fileName = `资源导入模板_${new Date().getTime()}.xlsx`;
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

const handleImportSuccess = () => { ElMessage.success('导入成功'); popFlag.value = false; getList(); };

onMounted(getList);
</script>

<style scoped>
.wrapper { padding: 20px; background: #fff; }
.card { padding: 10px; }

.table { padding-bottom: 60px; }

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

.custom-total { margin-right: 10px; font-size: 14px; color: #606266; }

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
}
.btn-list li:hover { opacity: 0.8; }

.search-box :deep(.el-input),
.search-box :deep(.el-select) {
  width: 200px; 
}

.search-box :deep(.search-buttons) {
  margin-left: 68% !important;
}

.dialog-msg { display: flex; align-items: center; gap: 10px; font-size: 16px; }
.clearFix::after { content: ""; display: block; clear: both; }
.mt15 { margin-top: 15px; }
.mb15 { margin-bottom: 15px; }
</style>