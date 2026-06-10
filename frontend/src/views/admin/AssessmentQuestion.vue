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
          <template v-if="col.prop === 'isActive'" #default="{ row }">
            <el-tag :type="row.isActive === 1 ? 'success' : 'info'">
              {{ row.isActive === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
          <template v-else-if="col.prop === 'questionType'" #default="{ row }">
            {{ questionTypeMap[row.questionType] || row.questionType }}
          </template>
          <template v-else-if="col.prop === 'subjectId'" #default="{ row }">
            {{ getSubjectNameById(row.subjectId) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" align="center" width="180">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleCommand('update', row)">修改</el-button>
            <el-button link type="info" @click="handleCommand('detail', row)">详情</el-button>
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
      width="600px"
      destroy-on-close
    >
      <el-form 
        v-if="['add', 'update'].includes(popType)" 
        ref="formRef" 
        :model="formModel" 
        :rules="formRules" 
        label-width="120px"
      >
        <el-form-item label="科目名称" prop="subjectId">
          <el-select v-model="formModel.subjectId" placeholder="请选择科目">
            <el-option
              v-for="item in subjectOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="题目编号" prop="questionCode">
          <el-input v-model="formModel.questionCode" placeholder="请输入题目编号" />
        </el-form-item>
        <el-form-item label="题目内容" prop="questionText">
          <el-input 
            v-model="formModel.questionText" 
            type="textarea"
            :rows="3"
            placeholder="请输入题目内容" 
          />
        </el-form-item>
        <el-form-item label="题目类型" prop="questionType">
          <el-select v-model="formModel.questionType" placeholder="请选择题目类型">
            <el-option label="单选题" value="single_choice" />
            <el-option label="多选题" value="multi_choice" />
            <el-option label="判断题" value="true_false" />
            <el-option label="简答题" value="short_answer" />
          </el-select>
        </el-form-item>
        <el-form-item label="映射字段" prop="mappingField">
          <el-input v-model="formModel.mappingField" placeholder="请输入映射字段" />
        </el-form-item>
        <el-form-item label="选项JSON" prop="optionsJson">
          <el-input 
            v-model="formModel.optionsJson" 
            type="textarea"
            :rows="4"
            placeholder='请输入选项JSON，例如：[{"code":"A","text":"选项A","score":1}]' 
          />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="formModel.defaultOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formModel.isActive">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <div v-else-if="popType === 'detail'" class="detail-content">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="题目编号">{{ detailData.questionCode }}</el-descriptions-item>
          <el-descriptions-item label="题目内容">{{ detailData.questionText }}</el-descriptions-item>
          <el-descriptions-item label="题目类型">{{ questionTypeMap[detailData.questionType] || detailData.questionType }}</el-descriptions-item>
          <el-descriptions-item label="科目名称">{{ getSubjectNameById(detailData.subjectId) }}</el-descriptions-item>
          <el-descriptions-item label="映射字段">{{ detailData.mappingField }}</el-descriptions-item>
          <el-descriptions-item label="排序">{{ detailData.defaultOrder }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="detailData.isActive === 1 ? 'success' : 'info'">
              {{ detailData.isActive === 1 ? '启用' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="选项JSON">
            <pre>{{ formatJson(detailData.optionsJson) }}</pre>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ detailData.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ detailData.updatedAt }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <p v-else-if="popType === 'del'" class="dialog-msg">
        <el-icon color="#ed6a0c"><WarningFilled /></el-icon> 是否确认删除选中的数据？
      </p>

      <template #footer>
        <el-button @click="popFlag = false">取消</el-button>
        <el-button type="primary" @click="confirm" v-if="['add', 'update', 'del'].includes(popType)">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { getAssessmentQuestionsList, addAssessmentQuestion, updateAssessmentQuestion, deleteAssessmentQuestion, deleteBatchAssessmentQuestion, getSubjectOptions } from '@/api/auth'; 

const searchConfig = [
  { label: '题目编号', prop: 'questionCode', type: 'input', placeholder: '请输入题目编号' },
  { label: '题目内容', prop: 'questionText', type: 'input', placeholder: '请输入题目内容' },
  { 
    label: '科目名称', 
    prop: 'subjectId', 
    type: 'select', 
    placeholder: '请选择科目',
    options: [] // 动态填充
  },
  { 
    label: '状态', 
    prop: 'isActive', 
    type: 'select', 
    placeholder: '请选择状态',
    options: [
      { label: '启用', value: 1 },
      { label: '禁用', value: 0 }
    ]
  }
];

const btnList = [
  { label: '新增', type: 'add' },
  { label: '删除', type: 'batchDel' }
];

const tableColumns = [
  { label: '题目编号', prop: 'questionCode', width: '120' },
  { label: '题目内容', prop: 'questionText' },
  { label: '题目类型', prop: 'questionType', width: '100' },
  { label: '科目名称', prop: 'subjectId', width: '120' },
  { label: '映射字段', prop: 'mappingField', width: '120' },
  { label: '排序', prop: 'defaultOrder', width: '80' },
  { label: '状态', prop: 'isActive', width: '80' }
];

const questionTypeMap = {
  'single_choice': '单选题',
  'multi_choice': '多选题',
  'true_false': '判断题',
  'short_answer': '简答题'
};

const subjectOptions = ref([]);

const loading = ref(false);
const tableData = ref([]);
const total = ref(0);
const multipleSelection = ref([]);
const popFlag = ref(false);
const popType = ref('add');
const detailData = ref({});

const queryParams = reactive({ 
  pageNum: 1, 
  pageSize: 10, 
  questionCode: '', 
  questionText: '',
  subjectId: '',  // 现在存储科目ID
  isActive: '' 
});

const formModel = reactive({ 
  id: null, 
  subjectId: null,
  questionCode: '', 
  questionText: '', 
  questionType: 'single_choice',
  mappingField: '',
  optionsJson: '',
  defaultOrder: 0,
  isActive: 1 
});
const formRules = { 
  subjectId: [{ required: true, message: '必填', trigger: 'blur' }],
  questionCode: [{ required: true, message: '必填', trigger: 'blur' }],
  questionText: [{ required: true, message: '必填', trigger: 'blur' }],
  questionType: [{ required: true, message: '必填', trigger: 'blur' }],
  mappingField: [{ required: true, message: '必填', trigger: 'blur' }]
};

// 根据科目ID获取科目名称
const getSubjectNameById = (id) => {
  const subject = subjectOptions.value.find(item => item.id == id);
  return subject ? subject.name : '未知科目';
};

const popTitle = computed(() => {
  const map = { add: '新增测试题', update: '修改测试题', del: '系统提示', detail: '题目详情' };
  return map[popType.value];
});

const getList = async () => {
  loading.value = true;
  try {
    const res = await getAssessmentQuestionsList(queryParams);
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
  } else if (type === 'detail') {
    detailData.value = row;
    popType.value = 'detail';
    popFlag.value = true;
  }
};

const confirm = async () => {
  try {
    if (popType.value === 'add') {
      await addAssessmentQuestion(formModel);
      ElMessage.success('新增成功');
    } else if (popType.value === 'update') {
      await updateAssessmentQuestion(formModel.id, formModel);
      ElMessage.success('更新成功');
    } else if (popType.value === 'del') {
      if (multipleSelection.value.length > 0 && popType.value === 'del' && !formModel.id) {
        // 批量删除
        const ids = multipleSelection.value.map(item => item.id);
        await deleteBatchAssessmentQuestion(ids);
        ElMessage.success('批量删除成功');
      } else {
        // 单条删除
        await deleteAssessmentQuestion(formModel.id);
        ElMessage.success('删除成功');
      }
    }
    popFlag.value = false;
    getList();
  } catch (error) {
    console.error(error);
  }
};

const handleQuery = () => { queryParams.pageNum = 1; getList(); };

const resetQuery = () => { 
  queryParams.questionCode = ''; 
  queryParams.questionText = ''; 
  queryParams.subjectId = ''; 
  queryParams.isActive = ''; 
  handleQuery(); 
};

const handleSelectionChange = (val) => { multipleSelection.value = val; };
const resetForm = () => { 
  Object.assign(formModel, { 
    id: null, 
    subjectId: null,
    questionCode: '', 
    questionText: '', 
    questionType: 'single_choice',
    mappingField: '',
    optionsJson: '',
    defaultOrder: 0,
    isActive: 1 
  }); 
};

const formatJson = (jsonStr) => {
  try {
    return JSON.stringify(JSON.parse(jsonStr), null, 2);
  } catch (e) {
    return jsonStr;
  }
};

// 加载科目选项
const loadSubjectOptions = async () => {
  try {
    const res = await getSubjectOptions();
    if (res.code === 200) {
      subjectOptions.value = res.data;
      // 更新搜索配置中的选项
      const subjectIndex = searchConfig.findIndex(item => item.prop === 'subjectId');
      if (subjectIndex !== -1) {
        searchConfig[subjectIndex].options = res.data.map(item => ({
          label: item.name,
          value: item.id
        }));
      }
    } else {
      console.error('获取科目选项失败:', res.message);
    }
  } catch (error) {
    console.error('获取科目选项异常:', error);
  }
};

onMounted(async () => {
  await loadSubjectOptions();
  getList();
});
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

.detail-content {
  max-height: 60vh;
  overflow-y: auto;
}

pre {
  white-space: pre-wrap;
  word-wrap: break-word;
  background: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
  font-size: 12px;
}
</style>