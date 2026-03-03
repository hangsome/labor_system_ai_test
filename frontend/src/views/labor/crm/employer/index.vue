<template>
  <GiPageLayout>
    <GiTable
      row-key="id"
      :data="dataList"
      :columns="columns"
      :loading="loading"
      :pagination="pagination"
      :scroll="{ x: '100%', y: '100%', minWidth: 1100 }"
      :disabled-tools="['size']"
      @refresh="search"
    >
      <template #toolbar-left>
        <a-input-search
          v-model="queryForm.description"
          placeholder="搜索单位编号/名称/地址"
          allow-clear
          style="width: 280px"
          @search="search"
        />
        <a-select
          v-model="queryForm.customerLevel"
          :options="customerLevelOptions"
          placeholder="客户等级"
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
        <a-button v-permission="['labor:employer:create']" type="primary" @click="onAdd">
          <template #icon><icon-plus /></template>
          <template #default>新增</template>
        </a-button>
      </template>
      <template #isOutsource="{ record }">
        <a-tag :color="record.isOutsource ? 'orangered' : 'arcoblue'">{{ record.isOutsource ? '是' : '否' }}</a-tag>
      </template>
      <template #action="{ record }">
        <a-space>
          <a-link v-permission="['labor:employer:update']" @click="onUpdate(record)">修改</a-link>
          <a-link v-permission="['labor:employer:deactivate']" status="warning" @click="onDeactivate(record)">停用</a-link>
          <a-link v-permission="['labor:employer:delete']" status="danger" @click="onDelete(record)">删除</a-link>
        </a-space>
      </template>
    </GiTable>

    <a-modal
      v-model:visible="formVisible"
      :title="formMode === 'create' ? '新增用工单位' : '修改用工单位'"
      :ok-loading="saveLoading"
      width="680px"
      @ok="onSave"
      @cancel="onCancel"
    >
      <a-form ref="formRef" :model="formState" auto-label-width>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item field="unitCode" label="单位编号" :rules="[{ required: true, message: '请输入单位编号' }]">
              <a-input v-model="formState.unitCode" :disabled="formMode === 'update'" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="unitName" label="单位名称" :rules="[{ required: true, message: '请输入单位名称' }]">
              <a-input v-model="formState.unitName" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="leadId" label="关联线索ID">
              <a-input-number v-model="formState.leadId" :min="1" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="customerLevel" label="客户等级" :rules="[{ required: true, message: '请输入客户等级' }]">
              <a-select v-model="formState.customerLevel" :options="customerLevelOptions" />
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item field="address" label="地址">
              <a-input v-model="formState.address" />
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item field="invoiceInfo" label="开票信息(JSON)">
              <a-textarea v-model="formState.invoiceInfo" :max-length="1000" show-word-limit />
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item field="isOutsource" label="是否外包">
              <a-switch v-model="formState.isOutsource" />
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
  type EmployerQuery,
  type EmployerReq,
  type EmployerResp,
  addEmployer,
  deactivateEmployer,
  deleteEmployer,
  listEmployer,
  updateEmployer,
} from '@/apis/labor/employer'
import { useTable } from '@/hooks'
import has from '@/utils/has'

defineOptions({ name: 'LaborEmployer' })

const customerLevelOptions = [
  { label: 'A', value: 'A' },
  { label: 'B', value: 'B' },
  { label: 'C', value: 'C' },
  { label: 'INACTIVE', value: 'INACTIVE' },
]

const queryForm = reactive<EmployerQuery>({
  sort: ['updateTime,desc'],
})

const {
  tableData: dataList,
  loading,
  pagination,
  search,
  handleDelete,
} = useTable((page) => listEmployer({ ...queryForm, ...page }), { immediate: true })

const columns: TableInstance['columns'] = [
  {
    title: '序号',
    width: 66,
    align: 'center',
    render: ({ rowIndex }) => h('span', {}, rowIndex + 1 + (pagination.current - 1) * pagination.pageSize),
  },
  { title: '单位编号', dataIndex: 'unitCode', width: 160 },
  { title: '单位名称', dataIndex: 'unitName', width: 240, ellipsis: true, tooltip: true },
  { title: '关联线索ID', dataIndex: 'leadId', width: 110 },
  { title: '客户等级', dataIndex: 'customerLevel', width: 120 },
  { title: '地址', dataIndex: 'address', ellipsis: true, tooltip: true },
  { title: '外包', dataIndex: 'isOutsource', slotName: 'isOutsource', width: 90, align: 'center' },
  { title: '更新时间', dataIndex: 'updateTime', width: 180 },
  {
    title: '操作',
    dataIndex: 'action',
    slotName: 'action',
    width: 190,
    align: 'center',
    fixed: 'right',
    show: has.hasPermOr(['labor:employer:update', 'labor:employer:deactivate', 'labor:employer:delete']),
  },
]

const formVisible = ref(false)
const saveLoading = ref(false)
const formMode = ref<'create' | 'update'>('create')
const formId = ref<string | number>('')
const formRef = ref<FormInstance>()

const getDefaultFormState = (): EmployerReq => ({
  unitCode: '',
  leadId: undefined,
  unitName: '',
  customerLevel: 'A',
  address: '',
  invoiceInfo: '',
  isOutsource: false,
})

const formState = reactive<EmployerReq>(getDefaultFormState())

const reset = () => {
  queryForm.description = undefined
  queryForm.customerLevel = undefined
  search()
}

const onAdd = () => {
  formMode.value = 'create'
  formId.value = ''
  Object.assign(formState, getDefaultFormState())
  formVisible.value = true
}

const onUpdate = (record: EmployerResp) => {
  formMode.value = 'update'
  formId.value = record.id
  Object.assign(formState, {
    unitCode: record.unitCode,
    leadId: record.leadId ? Number(record.leadId) : undefined,
    unitName: record.unitName,
    customerLevel: record.customerLevel,
    address: record.address,
    invoiceInfo: record.invoiceInfo,
    isOutsource: Boolean(record.isOutsource),
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
      await addEmployer(formState)
    } else {
      await updateEmployer(formState, formId.value)
    }
    Message.success('保存成功')
    formVisible.value = false
    search()
    return true
  } finally {
    saveLoading.value = false
  }
}

const onDeactivate = async (record: EmployerResp) => {
  await deactivateEmployer(record.id)
  Message.success('停用成功')
  search()
}

const onDelete = (record: EmployerResp) => {
  return handleDelete(() => deleteEmployer(record.id), {
    content: `是否确定删除单位「${record.unitName}」？`,
    showModal: true,
  })
}
</script>

<style scoped lang="scss"></style>
