<template>
  <GiPageLayout>
    <GiTable
      row-key="id"
      :data="dataList"
      :columns="columns"
      :loading="loading"
      :pagination="pagination"
      :scroll="{ x: '100%', y: '100%', minWidth: 1200 }"
      :disabled-tools="['size']"
      @refresh="search"
    >
      <template #toolbar-left>
        <a-input-search
          v-model="queryForm.description"
          placeholder="搜索线索编号/项目/联系人"
          allow-clear
          style="width: 280px"
          @search="search"
        />
        <a-select
          v-model="queryForm.cooperationStatus"
          :options="LEAD_STATUS_OPTIONS"
          placeholder="合作状态"
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
        <a-button v-permission="['labor:lead:create']" type="primary" @click="onAdd">
          <template #icon><icon-plus /></template>
          <template #default>新增</template>
        </a-button>
      </template>
      <template #cooperationStatus="{ record }">
        <a-tag :color="LEAD_STATUS_COLOR_MAP[record.cooperationStatus] ?? 'arcoblue'">
          {{ getLeadStatusLabel(record.cooperationStatus) }}
        </a-tag>
      </template>
      <template #action="{ record }">
        <a-space>
          <a-link v-permission="['labor:lead:get']" @click="onDetail(record)">详情</a-link>
          <a-link v-permission="['labor:lead:update']" @click="onUpdate(record)">修改</a-link>
          <a-dropdown>
            <a-button v-permission="['labor:lead:transition']" type="text" size="mini">
              <template #icon><icon-swap /></template>
            </a-button>
            <template #content>
              <a-doption
                v-for="status in LEAD_TRANSITION_STATUS_OPTIONS"
                :key="status"
                @click="onTransition(record, status)"
              >
                转为 {{ getLeadStatusLabel(status) }}
              </a-doption>
            </template>
          </a-dropdown>
          <a-link v-permission="['labor:lead:delete']" status="danger" @click="onDelete(record)">删除</a-link>
        </a-space>
      </template>
    </GiTable>

    <a-modal
      v-model:visible="formVisible"
      :title="formMode === 'create' ? '新增线索' : '修改线索'"
      :ok-loading="saveLoading"
      width="680px"
      @ok="onSave"
      @cancel="onCancel"
    >
      <a-form ref="formRef" :model="formState" auto-label-width>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item field="leadCode" label="线索编号" :rules="[{ required: true, message: '请输入线索编号' }]">
              <a-input v-model="formState.leadCode" :disabled="formMode === 'update'" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="projectName" label="项目名称" :rules="[{ required: true, message: '请输入项目名称' }]">
              <a-input v-model="formState.projectName" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="contactName" label="联系人">
              <a-input v-model="formState.contactName" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="contactPhone" label="联系方式">
              <a-input v-model="formState.contactPhone" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="industryType" label="行业类型" :rules="[{ required: true, message: '请输入行业类型' }]">
              <a-input v-model="formState.industryType" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="bizOwnerId" label="负责人ID" :rules="[{ required: true, message: '请输入负责人ID' }]">
              <a-input-number v-model="formState.bizOwnerId" :min="1" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="cooperationStatus" label="合作状态" :rules="[{ required: true, message: '请选择合作状态' }]">
              <a-select v-model="formState.cooperationStatus" :options="LEAD_STATUS_OPTIONS" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="depositStatus" label="保证金状态" :rules="[{ required: true, message: '请选择保证金状态' }]">
              <a-select v-model="formState.depositStatus" :options="DEPOSIT_STATUS_OPTIONS" />
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item field="tenderAt" label="招标日期">
              <a-date-picker v-model="formState.tenderAt" value-format="YYYY-MM-DD" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>
  </GiPageLayout>
</template>

<script setup lang="ts">
import type { FormInstance, TableInstance } from '@arco-design/web-vue'
import { Message } from '@arco-design/web-vue'
import {
  type LeadQuery,
  type LeadReq,
  type LeadResp,
  addLead,
  deleteLead,
  listLead,
  transitionLeadStatus,
  updateLead,
} from '@/apis/labor/lead'
import {
  DEPOSIT_STATUS_LABEL_MAP,
  DEPOSIT_STATUS_OPTIONS,
  LEAD_STATUS_COLOR_MAP,
  LEAD_STATUS_LABEL_MAP,
  LEAD_STATUS_OPTIONS,
  LEAD_TRANSITION_STATUS_OPTIONS,
  toLaborLabel,
} from '@/constant/labor'
import { useTable } from '@/hooks'
import has from '@/utils/has'

defineOptions({ name: 'LaborLead' })

const router = useRouter()

const getLeadStatusLabel = (value?: string) => toLaborLabel(LEAD_STATUS_LABEL_MAP, value)
const getDepositStatusLabel = (value?: string) => toLaborLabel(DEPOSIT_STATUS_LABEL_MAP, value)

const queryForm = reactive<LeadQuery>({
  sort: ['updateTime,desc'],
})

const {
  tableData: dataList,
  loading,
  pagination,
  search,
  handleDelete,
} = useTable((page) => listLead({ ...queryForm, ...page }), { immediate: true })

const columns: TableInstance['columns'] = [
  {
    title: '序号',
    width: 66,
    align: 'center',
    render: ({ rowIndex }) => h('span', {}, rowIndex + 1 + (pagination.current - 1) * pagination.pageSize),
  },
  { title: '线索编号', dataIndex: 'leadCode', width: 150, ellipsis: true, tooltip: true },
  { title: '项目名称', dataIndex: 'projectName', width: 220, ellipsis: true, tooltip: true },
  { title: '联系人', dataIndex: 'contactName', width: 120 },
  { title: '联系方式', dataIndex: 'contactPhone', width: 140 },
  { title: '行业', dataIndex: 'industryType', width: 120 },
  { title: '负责人ID', dataIndex: 'bizOwnerId', width: 110 },
  { title: '合作状态', dataIndex: 'cooperationStatus', slotName: 'cooperationStatus', width: 120, align: 'center' },
  {
    title: '保证金状态',
    dataIndex: 'depositStatus',
    width: 120,
    render: ({ record }) => h('span', {}, getDepositStatusLabel(record.depositStatus)),
  },
  { title: '更新时间', dataIndex: 'updateTime', width: 180 },
  {
    title: '操作',
    dataIndex: 'action',
    slotName: 'action',
    width: 230,
    align: 'center',
    fixed: 'right',
    show: has.hasPermOr(['labor:lead:get', 'labor:lead:update', 'labor:lead:transition', 'labor:lead:delete']),
  },
]

const formVisible = ref(false)
const saveLoading = ref(false)
const formMode = ref<'create' | 'update'>('create')
const formId = ref<string | number>('')
const formRef = ref<FormInstance>()

const getDefaultFormState = (): LeadReq => ({
  leadCode: '',
  projectName: '',
  contactName: '',
  contactPhone: '',
  industryType: '',
  bizOwnerId: 1,
  cooperationStatus: 'NEW',
  tenderAt: '',
  depositStatus: 'UNPAID',
})

const formState = reactive<LeadReq>(getDefaultFormState())

const reset = () => {
  queryForm.description = undefined
  queryForm.cooperationStatus = undefined
  search()
}

const onAdd = () => {
  formMode.value = 'create'
  formId.value = ''
  Object.assign(formState, getDefaultFormState())
  formVisible.value = true
}

const onUpdate = (record: LeadResp) => {
  formMode.value = 'update'
  formId.value = record.id
  Object.assign(formState, {
    leadCode: record.leadCode,
    projectName: record.projectName,
    contactName: record.contactName,
    contactPhone: record.contactPhone,
    industryType: record.industryType,
    bizOwnerId: Number(record.bizOwnerId),
    cooperationStatus: record.cooperationStatus,
    tenderAt: record.tenderAt,
    depositStatus: record.depositStatus,
  })
  formVisible.value = true
}

const onCancel = () => {
  formVisible.value = false
  formRef.value?.clearValidate()
}

const onSave = async () => {
  const error = await formRef.value?.validate()
  if (error) return false

  saveLoading.value = true
  try {
    if (formMode.value === 'create') {
      await addLead(formState)
    } else {
      await updateLead(formState, formId.value)
    }
    Message.success('保存成功')
    formVisible.value = false
    search()
    return true
  } finally {
    saveLoading.value = false
  }
}

const onDelete = (record: LeadResp) => {
  return handleDelete(() => deleteLead(record.id), {
    content: `是否确定删除线索「${record.leadCode}」？`,
    showModal: true,
  })
}

const onTransition = async (record: LeadResp, toStatus: string) => {
  await transitionLeadStatus(record.id, { toStatus })
  Message.success(`状态已流转为「${getLeadStatusLabel(toStatus)}」`)
  search()
}

const onDetail = (record: LeadResp) => {
  router.push({ path: '/labor/crm/lead/detail', query: { id: record.id } })
}
</script>

<style scoped lang="scss"></style>
