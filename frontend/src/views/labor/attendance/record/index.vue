<template>
  <GiPageLayout>
    <GiTable
      row-key="id"
      :data="dataList"
      :columns="columns"
      :loading="loading"
      :pagination="pagination"
      :scroll="{ x: '100%', y: '100%', minWidth: 1460 }"
      :disabled-tools="['size']"
      @refresh="search"
    >
      <template #toolbar-left>
        <a-input-number
          v-model="queryForm.contractId"
          :min="1"
          placeholder="合同ID"
          style="width: 120px"
          @change="search"
        />
        <a-input-number
          v-model="queryForm.employeeId"
          :min="1"
          placeholder="员工ID"
          style="width: 120px"
          @change="search"
        />
        <a-select
          v-model="queryForm.status"
          :options="ATTENDANCE_STATUS_OPTIONS"
          placeholder="考勤状态"
          allow-clear
          style="width: 150px"
          @change="search"
        />
        <a-date-picker
          v-model="queryForm.workDate"
          value-format="YYYY-MM-DD"
          placeholder="考勤日期"
          style="width: 160px"
          @change="search"
        />
        <a-button @click="reset">
          <template #icon><icon-refresh /></template>
          <template #default>重置</template>
        </a-button>
      </template>
      <template #toolbar-right>
        <a-button v-permission="['labor:attendance:create']" type="primary" @click="onAdd">
          <template #icon><icon-plus /></template>
          <template #default>新增考勤</template>
        </a-button>
      </template>
      <template #status="{ record }">
        <a-tag :color="ATTENDANCE_STATUS_COLOR_MAP[record.status] ?? 'arcoblue'">
          {{ getAttendanceStatusLabel(record.status) }}
        </a-tag>
      </template>
      <template #action="{ record }">
        <a-space>
          <a-link v-permission="['labor:attendance:update']" @click="onUpdate(record)">修改</a-link>
          <a-link v-permission="['labor:attendance:delete']" status="danger" @click="onDelete(record)">删除</a-link>
        </a-space>
      </template>
    </GiTable>

    <a-modal
      v-model:visible="formVisible"
      :title="formMode === 'create' ? '新增考勤记录' : '修改考勤记录'"
      :ok-loading="saveLoading"
      width="760px"
      @ok="onSave"
      @cancel="onCancel"
    >
      <a-form ref="formRef" :model="formState" auto-label-width>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item field="contractId" label="合同ID" :rules="[{ required: true, message: '请输入合同ID' }]">
              <a-input-number v-model="formState.contractId" :min="1" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="employeeId" label="员工ID" :rules="[{ required: true, message: '请输入员工ID' }]">
              <a-input-number v-model="formState.employeeId" :min="1" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="shiftId" label="班次ID" :rules="[{ required: true, message: '请输入班次ID' }]">
              <a-input-number v-model="formState.shiftId" :min="1" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="workDate" label="考勤日期" :rules="[{ required: true, message: '请选择考勤日期' }]">
              <a-date-picker v-model="formState.workDate" value-format="YYYY-MM-DD" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="checkInAt" label="签到时间">
              <a-date-picker
                v-model="formState.checkInAt"
                show-time
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="checkOutAt" label="签退时间">
              <a-date-picker
                v-model="formState.checkOutAt"
                show-time
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
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
  type AttendanceQuery,
  type AttendanceReq,
  type AttendanceResp,
  addAttendance,
  deleteAttendance,
  listAttendance,
  updateAttendance,
} from '@/apis/labor/attendance'
import {
  ATTENDANCE_STATUS_COLOR_MAP,
  ATTENDANCE_STATUS_LABEL_MAP,
  ATTENDANCE_STATUS_OPTIONS,
  toLaborLabel,
} from '@/constant/labor'
import { useTable } from '@/hooks'
import has from '@/utils/has'

defineOptions({ name: 'LaborAttendanceRecord' })

const getAttendanceStatusLabel = (value?: string) => toLaborLabel(ATTENDANCE_STATUS_LABEL_MAP, value)

const queryForm = reactive<AttendanceQuery>({
  sort: ['updateTime,desc'],
})

const {
  tableData: dataList,
  loading,
  pagination,
  search,
  handleDelete,
} = useTable((page) => listAttendance({ ...queryForm, ...page }), { immediate: true })

const columns: TableInstance['columns'] = [
  {
    title: '序号',
    width: 66,
    align: 'center',
    render: ({ rowIndex }) => h('span', {}, rowIndex + 1 + (pagination.current - 1) * pagination.pageSize),
  },
  { title: '合同ID', dataIndex: 'contractId', width: 90 },
  { title: '员工ID', dataIndex: 'employeeId', width: 90 },
  { title: '班次ID', dataIndex: 'shiftId', width: 90 },
  { title: '考勤日期', dataIndex: 'workDate', width: 120 },
  { title: '签到时间', dataIndex: 'checkInAt', width: 180 },
  { title: '签退时间', dataIndex: 'checkOutAt', width: 180 },
  { title: '工时(分)', dataIndex: 'workMinutes', width: 100 },
  { title: '加班(分)', dataIndex: 'overtimeMinutes', width: 100 },
  { title: '状态', dataIndex: 'status', slotName: 'status', width: 100, align: 'center' },
  {
    title: '操作',
    dataIndex: 'action',
    slotName: 'action',
    width: 160,
    align: 'center',
    fixed: 'right',
    show: has.hasPermOr(['labor:attendance:update', 'labor:attendance:delete']),
  },
]

const formVisible = ref(false)
const saveLoading = ref(false)
const formMode = ref<'create' | 'update'>('create')
const formId = ref<string | number>('')
const formRef = ref<FormInstance>()

const getDefaultFormState = (): AttendanceReq => ({
  contractId: 1,
  employeeId: 1,
  workDate: '',
  shiftId: 1,
  checkInAt: '',
  checkOutAt: '',
})

const formState = reactive<AttendanceReq>(getDefaultFormState())

const reset = () => {
  queryForm.contractId = undefined
  queryForm.employeeId = undefined
  queryForm.status = undefined
  queryForm.workDate = undefined
  search()
}

const onAdd = () => {
  formMode.value = 'create'
  formId.value = ''
  Object.assign(formState, getDefaultFormState())
  formVisible.value = true
}

const onUpdate = (record: AttendanceResp) => {
  formMode.value = 'update'
  formId.value = record.id
  Object.assign(formState, {
    contractId: Number(record.contractId),
    employeeId: Number(record.employeeId),
    workDate: record.workDate,
    shiftId: Number(record.shiftId),
    checkInAt: record.checkInAt || '',
    checkOutAt: record.checkOutAt || '',
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
      await addAttendance(formState)
    } else {
      await updateAttendance(formState, formId.value)
    }
    Message.success('保存成功')
    formVisible.value = false
    search()
    return true
  } finally {
    saveLoading.value = false
  }
}

const onDelete = (record: AttendanceResp) => {
  return handleDelete(() => deleteAttendance(record.id), {
    content: `是否确定删除员工 ${record.employeeId} 在 ${record.workDate} 的考勤记录？`,
    showModal: true,
  })
}
</script>

<style scoped lang="scss"></style>
