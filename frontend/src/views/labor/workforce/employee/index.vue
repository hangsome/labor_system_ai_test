<template>
  <GiPageLayout>
    <GiTable
      row-key="id"
      :data="dataList"
      :columns="columns"
      :loading="loading"
      :pagination="pagination"
      :scroll="{ x: '100%', y: '100%', minWidth: 1280 }"
      :disabled-tools="['size']"
      @refresh="search"
    >
      <template #toolbar-left>
        <a-input-search
          v-model="queryForm.description"
          placeholder="搜索员工编号/姓名/手机号"
          allow-clear
          style="width: 280px"
          @search="search"
        />
        <a-select
          v-model="queryForm.status"
          :options="EMPLOYEE_STATUS_OPTIONS"
          placeholder="员工状态"
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
        <a-button v-permission="['labor:employee:create']" type="primary" @click="onAdd">
          <template #icon><icon-plus /></template>
          <template #default>新增员工</template>
        </a-button>
      </template>
      <template #status="{ record }">
        <a-tag :color="EMPLOYEE_STATUS_COLOR_MAP[record.status] ?? 'arcoblue'">
          {{ getEmployeeStatusLabel(record.status) }}
        </a-tag>
      </template>
      <template #action="{ record }">
        <a-space>
          <a-link v-permission="['labor:employee:update']" @click="onUpdate(record)">修改</a-link>
          <a-link
            v-if="record.status === 'ACTIVE'"
            v-permission="['labor:employee:offboard']"
            status="warning"
            @click="openOffboardModal(record)"
          >
            离职
          </a-link>
          <a-link v-permission="['labor:employee:delete']" status="danger" @click="onDelete(record)">删除</a-link>
        </a-space>
      </template>
    </GiTable>

    <a-modal
      v-model:visible="formVisible"
      :title="formMode === 'create' ? '新增员工' : '修改员工'"
      :ok-loading="saveLoading"
      width="760px"
      @ok="onSave"
      @cancel="onCancel"
    >
      <a-form ref="formRef" :model="formState" auto-label-width>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item field="employeeNo" label="员工编号" :rules="[{ required: true, message: '请输入员工编号' }]">
              <a-input v-model="formState.employeeNo" :disabled="formMode === 'update'" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="name" label="姓名" :rules="[{ required: true, message: '请输入姓名' }]">
              <a-input v-model="formState.name" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="idNo" label="证件号">
              <a-input v-model="formState.idNo" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="phone" label="手机号">
              <a-input v-model="formState.phone" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="deptId" label="部门ID">
              <a-input-number v-model="formState.deptId" :min="1" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="hiredAt" label="入职日期" :rules="[{ required: true, message: '请选择入职日期' }]">
              <a-date-picker v-model="formState.hiredAt" value-format="YYYY-MM-DD" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="accountName" label="开户名" :rules="[{ required: true, message: '请输入开户名' }]">
              <a-input v-model="formState.accountName" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="bankName" label="开户行" :rules="[{ required: true, message: '请输入开户行' }]">
              <a-input v-model="formState.bankName" />
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item field="bankNo" label="银行卡号" :rules="[{ required: true, message: '请输入银行卡号' }]">
              <a-input v-model="formState.bankNo" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>

    <a-modal
      v-model:visible="offboardVisible"
      title="员工离职"
      :ok-loading="offboardLoading"
      @ok="onOffboard"
      @cancel="offboardVisible = false"
    >
      <a-form ref="offboardFormRef" :model="offboardForm" auto-label-width>
        <a-form-item field="offboardAt" label="离职日期" :rules="[{ required: true, message: '请选择离职日期' }]">
          <a-date-picker v-model="offboardForm.offboardAt" value-format="YYYY-MM-DD" style="width: 100%" />
        </a-form-item>
        <a-form-item field="reason" label="离职原因">
          <a-textarea v-model="offboardForm.reason" :max-length="255" show-word-limit />
        </a-form-item>
      </a-form>
    </a-modal>
  </GiPageLayout>
</template>

<script setup lang="ts">
import type { FormInstance, TableInstance } from '@arco-design/web-vue'
import { Message } from '@arco-design/web-vue'
import {
  type EmployeeOffboardReq,
  type EmployeeQuery,
  type EmployeeReq,
  type EmployeeResp,
  addEmployee,
  deleteEmployee,
  getEmployee,
  listEmployee,
  offboardEmployee,
  updateEmployee,
} from '@/apis/labor/employee'
import {
  EMPLOYEE_STATUS_COLOR_MAP,
  EMPLOYEE_STATUS_LABEL_MAP,
  EMPLOYEE_STATUS_OPTIONS,
  toLaborLabel,
} from '@/constant/labor'
import { useTable } from '@/hooks'
import has from '@/utils/has'

defineOptions({ name: 'LaborEmployee' })

const getEmployeeStatusLabel = (value?: string) => toLaborLabel(EMPLOYEE_STATUS_LABEL_MAP, value)

const queryForm = reactive<EmployeeQuery>({
  sort: ['updateTime,desc'],
})

const {
  tableData: dataList,
  loading,
  pagination,
  search,
  handleDelete,
} = useTable((page) => listEmployee({ ...queryForm, ...page }), { immediate: true })

const columns: TableInstance['columns'] = [
  {
    title: '序号',
    width: 66,
    align: 'center',
    render: ({ rowIndex }) => h('span', {}, rowIndex + 1 + (pagination.current - 1) * pagination.pageSize),
  },
  { title: '员工编号', dataIndex: 'employeeNo', width: 150 },
  { title: '姓名', dataIndex: 'name', width: 120 },
  { title: '手机号', dataIndex: 'phone', width: 140 },
  { title: '部门ID', dataIndex: 'deptId', width: 100 },
  { title: '状态', dataIndex: 'status', slotName: 'status', width: 100, align: 'center' },
  { title: '入职日期', dataIndex: 'hiredAt', width: 120 },
  { title: '离职日期', dataIndex: 'offboardAt', width: 120 },
  { title: '更新时间', dataIndex: 'updateTime', width: 180 },
  {
    title: '操作',
    dataIndex: 'action',
    slotName: 'action',
    width: 220,
    align: 'center',
    fixed: 'right',
    show: has.hasPermOr(['labor:employee:update', 'labor:employee:offboard', 'labor:employee:delete']),
  },
]

const formVisible = ref(false)
const saveLoading = ref(false)
const formMode = ref<'create' | 'update'>('create')
const formId = ref<string | number>('')
const formRef = ref<FormInstance>()

const getDefaultFormState = (): EmployeeReq => ({
  employeeNo: '',
  name: '',
  idNo: '',
  phone: '',
  deptId: undefined,
  hiredAt: '',
  accountName: '',
  bankName: '',
  bankNo: '',
})

const formState = reactive<EmployeeReq>(getDefaultFormState())

const offboardVisible = ref(false)
const offboardLoading = ref(false)
const offboardFormRef = ref<FormInstance>()
const offboardId = ref<string | number>('')
const offboardForm = reactive<EmployeeOffboardReq>({
  offboardAt: '',
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

const onUpdate = async (record: EmployeeResp) => {
  const { data } = await getEmployee(record.id)
  formMode.value = 'update'
  formId.value = record.id
  Object.assign(formState, {
    employeeNo: data.employeeNo,
    name: data.name,
    idNo: data.idNo,
    phone: data.phone,
    deptId: data.deptId ? Number(data.deptId) : undefined,
    hiredAt: data.hiredAt,
    accountName: data.accountName || '',
    bankName: data.bankName || '',
    bankNo: data.bankNo || '',
  })
  formVisible.value = true
}

const openOffboardModal = (record: EmployeeResp) => {
  offboardId.value = record.id
  offboardForm.offboardAt = ''
  offboardForm.reason = ''
  offboardVisible.value = true
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
      await addEmployee(formState)
    } else {
      await updateEmployee(formState, formId.value)
    }
    Message.success('保存成功')
    formVisible.value = false
    search()
    return true
  } finally {
    saveLoading.value = false
  }
}

const onOffboard = async () => {
  const error = await offboardFormRef.value?.validate()
  if (error) return false

  offboardLoading.value = true
  try {
    await offboardEmployee(offboardId.value, offboardForm)
    Message.success('离职成功')
    offboardVisible.value = false
    search()
    return true
  } finally {
    offboardLoading.value = false
  }
}

const onDelete = (record: EmployeeResp) => {
  return handleDelete(() => deleteEmployee(record.id), {
    content: `是否确定删除员工「${record.name}」？`,
    showModal: true,
  })
}
</script>

<style scoped lang="scss"></style>
