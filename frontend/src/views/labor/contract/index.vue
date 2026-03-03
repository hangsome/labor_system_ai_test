<template>
  <GiPageLayout>
    <GiTable
      row-key="id"
      :data="dataList"
      :columns="columns"
      :loading="loading"
      :pagination="pagination"
      :scroll="{ x: '100%', y: '100%', minWidth: 1300 }"
      :disabled-tools="['size']"
      @refresh="search"
    >
      <template #toolbar-left>
        <a-input-search
          v-model="queryForm.description"
          placeholder="搜索合同编号/名称"
          allow-clear
          style="width: 260px"
          @search="search"
        />
        <a-select
          v-model="queryForm.status"
          :options="contractStatusOptions"
          placeholder="合同状态"
          allow-clear
          style="width: 160px"
          @change="search"
        />
        <a-button @click="reset">
          <template #icon><icon-refresh /></template>
          <template #default>重置</template>
        </a-button>
      </template>
      <template #toolbar-right>
        <a-button v-permission="['labor:contract:create']" type="primary" @click="onAdd">
          <template #icon><icon-plus /></template>
          <template #default>新增</template>
        </a-button>
      </template>
      <template #status="{ record }">
        <a-tag :color="statusColorMap[record.status] ?? 'arcoblue'">{{ record.status }}</a-tag>
      </template>
      <template #action="{ record }">
        <a-space>
          <a-link v-permission="['labor:contract:get']" @click="onDetail(record)">详情</a-link>
          <a-link v-permission="['labor:contract:update']" @click="onUpdate(record)">修改</a-link>
          <a-link v-permission="['labor:contract:sign']" @click="onSign(record)">签署</a-link>
          <a-link v-permission="['labor:contract:renew']" @click="openRenewModal(record)">续签</a-link>
          <a-link v-permission="['labor:contract:terminate']" status="warning" @click="openTerminateModal(record)">终止</a-link>
          <a-link v-permission="['labor:contract:delete']" status="danger" @click="onDelete(record)">删除</a-link>
        </a-space>
      </template>
    </GiTable>

    <a-modal
      v-model:visible="formVisible"
      :title="formMode === 'create' ? '新增合同' : '修改合同'"
      :ok-loading="saveLoading"
      width="760px"
      @ok="onSave"
      @cancel="onCancel"
    >
      <a-form ref="formRef" :model="formState" auto-label-width>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item field="contractNo" label="合同编号" :rules="[{ required: true, message: '请输入合同编号' }]">
              <a-input v-model="formState.contractNo" :disabled="formMode === 'update'" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="contractName" label="合同名称" :rules="[{ required: true, message: '请输入合同名称' }]">
              <a-input v-model="formState.contractName" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="employerUnitId" label="用工单位ID" :rules="[{ required: true, message: '请输入用工单位ID' }]">
              <a-input-number v-model="formState.employerUnitId" :min="1" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="contractType" label="合同类型" :rules="[{ required: true, message: '请选择合同类型' }]">
              <a-select v-model="formState.contractType" :options="contractTypeOptions" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="startDate" label="开始日期" :rules="[{ required: true, message: '请选择开始日期' }]">
              <a-date-picker v-model="formState.startDate" value-format="YYYY-MM-DD" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="endDate" label="结束日期" :rules="[{ required: true, message: '请选择结束日期' }]">
              <a-date-picker v-model="formState.endDate" value-format="YYYY-MM-DD" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="settlementCycle" label="结算周期" :rules="[{ required: true, message: '请输入结算周期' }]">
              <a-input v-model="formState.settlementCycle" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="taxRate" label="税率" :rules="[{ required: true, message: '请输入税率' }]">
              <a-input-number v-model="formState.taxRate" :min="0" :max="1" :step="0.0001" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>

    <a-modal
      v-model:visible="renewVisible"
      title="续签合同"
      :ok-loading="renewLoading"
      @ok="onRenew"
      @cancel="renewVisible = false"
    >
      <a-form ref="renewFormRef" :model="renewForm" auto-label-width>
        <a-form-item field="newEndDate" label="新结束日期" :rules="[{ required: true, message: '请选择新结束日期' }]">
          <a-date-picker v-model="renewForm.newEndDate" value-format="YYYY-MM-DD" style="width: 100%" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:visible="terminateVisible"
      title="终止合同"
      :ok-loading="terminateLoading"
      @ok="onTerminate"
      @cancel="terminateVisible = false"
    >
      <a-form ref="terminateFormRef" :model="terminateForm" auto-label-width>
        <a-form-item field="terminateDate" label="终止日期" :rules="[{ required: true, message: '请选择终止日期' }]">
          <a-date-picker v-model="terminateForm.terminateDate" value-format="YYYY-MM-DD" style="width: 100%" />
        </a-form-item>
        <a-form-item field="reason" label="终止原因">
          <a-textarea v-model="terminateForm.reason" :max-length="255" show-word-limit />
        </a-form-item>
      </a-form>
    </a-modal>
  </GiPageLayout>
</template>

<script setup lang="ts">
import type { FormInstance, TableInstance } from '@arco-design/web-vue'
import { Message } from '@arco-design/web-vue'
import {
  type ContractQuery,
  type ContractReq,
  type ContractResp,
  addContract,
  deleteContract,
  listContract,
  renewContract,
  signContract,
  terminateContract,
  updateContract,
} from '@/apis/labor/contract'
import { useTable } from '@/hooks'
import has from '@/utils/has'

defineOptions({ name: 'LaborContract' })

const router = useRouter()

const contractStatusOptions = [
  { label: 'DRAFT', value: 'DRAFT' },
  { label: 'SIGNED', value: 'SIGNED' },
  { label: 'TERMINATED', value: 'TERMINATED' },
]

const contractTypeOptions = [
  { label: 'A', value: 'A' },
  { label: 'B', value: 'B' },
]

const statusColorMap: Record<string, string> = {
  DRAFT: 'arcoblue',
  SIGNED: 'green',
  TERMINATED: 'gray',
}

const queryForm = reactive<ContractQuery>({
  sort: ['updateTime,desc'],
})

const {
  tableData: dataList,
  loading,
  pagination,
  search,
  handleDelete,
} = useTable((page) => listContract({ ...queryForm, ...page }), { immediate: true })

const columns: TableInstance['columns'] = [
  {
    title: '序号',
    width: 66,
    align: 'center',
    render: ({ rowIndex }) => h('span', {}, rowIndex + 1 + (pagination.current - 1) * pagination.pageSize),
  },
  { title: '合同编号', dataIndex: 'contractNo', width: 170, ellipsis: true, tooltip: true },
  { title: '合同名称', dataIndex: 'contractName', width: 220, ellipsis: true, tooltip: true },
  { title: '单位ID', dataIndex: 'employerUnitId', width: 100 },
  { title: '类型', dataIndex: 'contractType', width: 80, align: 'center' },
  { title: '开始日期', dataIndex: 'startDate', width: 120 },
  { title: '结束日期', dataIndex: 'endDate', width: 120 },
  { title: '结算周期', dataIndex: 'settlementCycle', width: 120 },
  { title: '状态', dataIndex: 'status', slotName: 'status', width: 120, align: 'center' },
  { title: '税率', dataIndex: 'taxRate', width: 90 },
  { title: '更新时间', dataIndex: 'updateTime', width: 180 },
  {
    title: '操作',
    dataIndex: 'action',
    slotName: 'action',
    width: 320,
    align: 'center',
    fixed: 'right',
    show: has.hasPermOr([
      'labor:contract:get',
      'labor:contract:update',
      'labor:contract:sign',
      'labor:contract:renew',
      'labor:contract:terminate',
      'labor:contract:delete',
    ]),
  },
]

const formVisible = ref(false)
const saveLoading = ref(false)
const formMode = ref<'create' | 'update'>('create')
const formId = ref<string | number>('')
const formRef = ref<FormInstance>()

const getDefaultFormState = (): ContractReq => ({
  contractNo: '',
  employerUnitId: 1,
  contractName: '',
  contractType: 'A',
  startDate: '',
  endDate: '',
  settlementCycle: 'MONTHLY',
  taxRate: 0.06,
})

const formState = reactive<ContractReq>(getDefaultFormState())

const renewVisible = ref(false)
const renewLoading = ref(false)
const renewFormRef = ref<FormInstance>()
const renewId = ref<string | number>('')
const renewForm = reactive({
  newEndDate: '',
})

const terminateVisible = ref(false)
const terminateLoading = ref(false)
const terminateFormRef = ref<FormInstance>()
const terminateId = ref<string | number>('')
const terminateForm = reactive({
  terminateDate: '',
  reason: '',
})

const reset = () => {
  queryForm.description = undefined
  queryForm.status = undefined
  search()
}

const onAdd = () => {
  formMode.value = 'create'
  formId.value = ''
  Object.assign(formState, getDefaultFormState())
  formVisible.value = true
}

const onUpdate = (record: ContractResp) => {
  formMode.value = 'update'
  formId.value = record.id
  Object.assign(formState, {
    contractNo: record.contractNo,
    employerUnitId: Number(record.employerUnitId),
    contractName: record.contractName,
    contractType: record.contractType,
    startDate: record.startDate,
    endDate: record.endDate,
    settlementCycle: record.settlementCycle,
    taxRate: Number(record.taxRate),
  })
  formVisible.value = true
}

const onCancel = () => {
  formVisible.value = false
  formRef.value?.clearValidate()
}

const onSave = async () => {
  const error = await formRef.value?.validate()
  if (error) {
    return false
  }

  saveLoading.value = true
  try {
    if (formMode.value === 'create') {
      await addContract(formState)
    } else {
      await updateContract(formState, formId.value)
    }
    Message.success('保存成功')
    formVisible.value = false
    search()
    return true
  } finally {
    saveLoading.value = false
  }
}

const onSign = async (record: ContractResp) => {
  await signContract(record.id)
  Message.success('签署成功')
  search()
}

const openRenewModal = (record: ContractResp) => {
  renewId.value = record.id
  renewForm.newEndDate = record.endDate
  renewVisible.value = true
}

const onRenew = async () => {
  const error = await renewFormRef.value?.validate()
  if (error) return false

  renewLoading.value = true
  try {
    await renewContract(renewId.value, { newEndDate: renewForm.newEndDate })
    Message.success('续签成功')
    renewVisible.value = false
    search()
    return true
  } finally {
    renewLoading.value = false
  }
}

const openTerminateModal = (record: ContractResp) => {
  terminateId.value = record.id
  terminateForm.terminateDate = record.endDate
  terminateForm.reason = ''
  terminateVisible.value = true
}

const onTerminate = async () => {
  const error = await terminateFormRef.value?.validate()
  if (error) return false

  terminateLoading.value = true
  try {
    await terminateContract(terminateId.value, {
      terminateDate: terminateForm.terminateDate,
      reason: terminateForm.reason,
    })
    Message.success('终止成功')
    terminateVisible.value = false
    search()
    return true
  } finally {
    terminateLoading.value = false
  }
}

const onDelete = (record: ContractResp) => {
  return handleDelete(() => deleteContract(record.id), {
    content: `是否确定删除合同「${record.contractNo}」？`,
    showModal: true,
  })
}

const onDetail = (record: ContractResp) => {
  router.push({ path: '/labor/contract/detail', query: { id: record.id } })
}
</script>

<style scoped lang="scss"></style>
