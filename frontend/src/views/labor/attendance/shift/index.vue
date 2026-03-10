<template>
  <GiPageLayout>
    <GiTable
      row-key="id"
      :data="dataList"
      :columns="columns"
      :loading="loading"
      :pagination="pagination"
      :scroll="{ x: '100%', y: '100%', minWidth: 1080 }"
      :disabled-tools="['size']"
      @refresh="search"
    >
      <template #toolbar-left>
        <a-input
          v-model="queryForm.shiftName"
          placeholder="班次名称"
          allow-clear
          style="width: 220px"
          @change="search"
        />
        <a-input-number
          v-model="queryForm.contractId"
          :min="1"
          placeholder="合同ID"
          style="width: 140px"
          @change="search"
        />
        <a-button @click="reset">
          <template #icon><icon-refresh /></template>
          <template #default>重置</template>
        </a-button>
      </template>
      <template #toolbar-right>
        <a-button v-permission="['labor:shift:create']" type="primary" @click="onAdd">
          <template #icon><icon-plus /></template>
          <template #default>新增排班</template>
        </a-button>
      </template>
      <template #action="{ record }">
        <a-space>
          <a-link v-permission="['labor:shift:update']" @click="onUpdate(record)">修改</a-link>
          <a-link v-permission="['labor:shift:delete']" status="danger" @click="onDelete(record)">删除</a-link>
        </a-space>
      </template>
    </GiTable>

    <a-modal
      v-model:visible="formVisible"
      :title="formMode === 'create' ? '新增排班' : '修改排班'"
      :ok-loading="saveLoading"
      width="640px"
      @ok="onSave"
      @cancel="onCancel"
    >
      <a-form ref="formRef" :model="formState" auto-label-width>
        <a-row :gutter="16">
          <a-col :span="24">
            <a-form-item field="contractId" label="合同ID" :rules="[{ required: true, message: '请输入合同ID' }]">
              <a-input-number v-model="formState.contractId" :min="1" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item field="shiftName" label="班次名称" :rules="[{ required: true, message: '请输入班次名称' }]">
              <a-input v-model="formState.shiftName" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="startTime" label="开始时间" :rules="[{ required: true, message: '请选择开始时间' }]">
              <a-time-picker v-model="formState.startTime" format="HH:mm:ss" value-format="HH:mm:ss" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="endTime" label="结束时间" :rules="[{ required: true, message: '请选择结束时间' }]">
              <a-time-picker v-model="formState.endTime" format="HH:mm:ss" value-format="HH:mm:ss" style="width: 100%" />
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
  type ShiftQuery,
  type ShiftReq,
  type ShiftResp,
  addShift,
  deleteShift,
  listShift,
  updateShift,
} from '@/apis/labor/shift'
import { useTable } from '@/hooks'
import has from '@/utils/has'

defineOptions({ name: 'LaborShift' })

const queryForm = reactive<ShiftQuery>({
  sort: ['updateTime,desc'],
})

const {
  tableData: dataList,
  loading,
  pagination,
  search,
  handleDelete,
} = useTable((page) => listShift({ ...queryForm, ...page }), { immediate: true })

const columns: TableInstance['columns'] = [
  {
    title: '序号',
    width: 66,
    align: 'center',
    render: ({ rowIndex }) => h('span', {}, rowIndex + 1 + (pagination.current - 1) * pagination.pageSize),
  },
  { title: '合同ID', dataIndex: 'contractId', width: 120 },
  { title: '班次名称', dataIndex: 'shiftName', width: 200, ellipsis: true, tooltip: true },
  { title: '开始时间', dataIndex: 'startTime', width: 120 },
  { title: '结束时间', dataIndex: 'endTime', width: 120 },
  { title: '更新时间', dataIndex: 'updateTime', width: 180 },
  {
    title: '操作',
    dataIndex: 'action',
    slotName: 'action',
    width: 160,
    align: 'center',
    fixed: 'right',
    show: has.hasPermOr(['labor:shift:update', 'labor:shift:delete']),
  },
]

const formVisible = ref(false)
const saveLoading = ref(false)
const formMode = ref<'create' | 'update'>('create')
const formId = ref<string | number>('')
const formRef = ref<FormInstance>()

const getDefaultFormState = (): ShiftReq => ({
  contractId: 1,
  shiftName: '',
  startTime: '09:00:00',
  endTime: '18:00:00',
})

const formState = reactive<ShiftReq>(getDefaultFormState())

const reset = () => {
  queryForm.shiftName = undefined
  queryForm.contractId = undefined
  search()
}

const onAdd = () => {
  formMode.value = 'create'
  formId.value = ''
  Object.assign(formState, getDefaultFormState())
  formVisible.value = true
}

const onUpdate = (record: ShiftResp) => {
  formMode.value = 'update'
  formId.value = record.id
  Object.assign(formState, {
    contractId: Number(record.contractId),
    shiftName: record.shiftName,
    startTime: record.startTime,
    endTime: record.endTime,
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
      await addShift(formState)
    } else {
      await updateShift(formState, formId.value)
    }
    Message.success('保存成功')
    formVisible.value = false
    search()
    return true
  } finally {
    saveLoading.value = false
  }
}

const onDelete = (record: ShiftResp) => {
  return handleDelete(() => deleteShift(record.id), {
    content: `是否确定删除班次「${record.shiftName}」？`,
    showModal: true,
  })
}
</script>

<style scoped lang="scss"></style>
