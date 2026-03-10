<template>
  <GiPageLayout>
    <GiTable
      row-key="id"
      :data="dataList"
      :columns="columns"
      :loading="loading"
      :pagination="pagination"
      :scroll="{ x: '100%', y: '100%', minWidth: 1180 }"
      :disabled-tools="['size']"
      @refresh="search"
    >
      <template #toolbar-left>
        <a-input-number
          v-model="queryForm.attendanceId"
          :min="1"
          placeholder="考勤ID"
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
          :options="CORRECTION_STATUS_OPTIONS"
          placeholder="审批状态"
          allow-clear
          style="width: 150px"
          @change="search"
        />
        <a-button @click="reset">
          <template #icon><icon-refresh /></template>
          <template #default>重置</template>
        </a-button>
      </template>
      <template #toolbar-right>
        <a-button v-permission="['labor:correction:create']" type="primary" @click="onAdd">
          <template #icon><icon-plus /></template>
          <template #default>提交补卡</template>
        </a-button>
      </template>
      <template #status="{ record }">
        <a-tag :color="CORRECTION_STATUS_COLOR_MAP[record.status] ?? 'arcoblue'">
          {{ getCorrectionStatusLabel(record.status) }}
        </a-tag>
      </template>
      <template #action="{ record }">
        <a-space>
          <a-link
            v-if="record.status === 'PENDING'"
            v-permission="['labor:correction:approve']"
            @click="onApprove(record)"
          >
            通过
          </a-link>
          <a-link
            v-if="record.status === 'PENDING'"
            v-permission="['labor:correction:reject']"
            status="danger"
            @click="onReject(record)"
          >
            驳回
          </a-link>
        </a-space>
      </template>
    </GiTable>

    <a-modal
      v-model:visible="formVisible"
      title="提交补卡申请"
      :ok-loading="saveLoading"
      width="640px"
      @ok="onSave"
      @cancel="onCancel"
    >
      <a-form ref="formRef" :model="formState" auto-label-width>
        <a-form-item field="attendanceId" label="考勤记录ID" :rules="[{ required: true, message: '请输入考勤记录ID' }]">
          <a-input-number v-model="formState.attendanceId" :min="1" style="width: 100%" />
        </a-form-item>
        <a-form-item field="employeeId" label="员工ID" :rules="[{ required: true, message: '请输入员工ID' }]">
          <a-input-number v-model="formState.employeeId" :min="1" style="width: 100%" />
        </a-form-item>
        <a-form-item field="reason" label="补卡原因" :rules="[{ required: true, message: '请输入补卡原因' }]">
          <a-textarea v-model="formState.reason" :max-length="512" show-word-limit />
        </a-form-item>
      </a-form>
    </a-modal>
  </GiPageLayout>
</template>

<script setup lang="ts">
import type { FormInstance, TableInstance } from '@arco-design/web-vue'
import { Message } from '@arco-design/web-vue'
import {
  type CorrectionQuery,
  type CorrectionReq,
  type CorrectionResp,
  addCorrection,
  approveCorrection,
  listCorrection,
  rejectCorrection,
} from '@/apis/labor/correction'
import {
  CORRECTION_STATUS_COLOR_MAP,
  CORRECTION_STATUS_LABEL_MAP,
  CORRECTION_STATUS_OPTIONS,
  toLaborLabel,
} from '@/constant/labor'
import { useTable } from '@/hooks'
import has from '@/utils/has'

defineOptions({ name: 'LaborAttendanceCorrection' })

const getCorrectionStatusLabel = (value?: string) => toLaborLabel(CORRECTION_STATUS_LABEL_MAP, value)

const queryForm = reactive<CorrectionQuery>({
  sort: ['updateTime,desc'],
})

const {
  tableData: dataList,
  loading,
  pagination,
  search,
} = useTable((page) => listCorrection({ ...queryForm, ...page }), { immediate: true })

const columns: TableInstance['columns'] = [
  {
    title: '序号',
    width: 66,
    align: 'center',
    render: ({ rowIndex }) => h('span', {}, rowIndex + 1 + (pagination.current - 1) * pagination.pageSize),
  },
  { title: '考勤记录ID', dataIndex: 'attendanceId', width: 120 },
  { title: '员工ID', dataIndex: 'employeeId', width: 100 },
  { title: '补卡原因', dataIndex: 'reason', ellipsis: true, tooltip: true },
  { title: '审批状态', dataIndex: 'status', slotName: 'status', width: 110, align: 'center' },
  { title: '审批人', dataIndex: 'reviewedBy', width: 100 },
  { title: '审批时间', dataIndex: 'reviewedAt', width: 180 },
  { title: '创建时间', dataIndex: 'createTime', width: 180 },
  {
    title: '操作',
    dataIndex: 'action',
    slotName: 'action',
    width: 160,
    align: 'center',
    fixed: 'right',
    show: has.hasPermOr(['labor:correction:approve', 'labor:correction:reject']),
  },
]

const formVisible = ref(false)
const saveLoading = ref(false)
const formRef = ref<FormInstance>()
const formState = reactive<CorrectionReq>({
  attendanceId: 1,
  employeeId: 1,
  reason: '',
})

const reset = () => {
  queryForm.attendanceId = undefined
  queryForm.employeeId = undefined
  queryForm.status = undefined
  search()
}

const onAdd = () => {
  formState.attendanceId = 1
  formState.employeeId = 1
  formState.reason = ''
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
    await addCorrection(formState)
    Message.success('提交成功')
    formVisible.value = false
    search()
    return true
  } finally {
    saveLoading.value = false
  }
}

const onApprove = async (record: CorrectionResp) => {
  await approveCorrection(record.id)
  Message.success('审批通过')
  search()
}

const onReject = async (record: CorrectionResp) => {
  await rejectCorrection(record.id)
  Message.success('已驳回')
  search()
}
</script>

<style scoped lang="scss"></style>
