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
        <a-input-number
          v-model="queryForm.contractId"
          :min="1"
          placeholder="合同ID"
          hide-button
          style="width: 160px"
          @change="search"
        />
        <a-select
          v-model="queryForm.status"
          :options="SETTLEMENT_STATUS_OPTIONS"
          placeholder="规则状态"
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
        <a-space>
          <a-button v-permission="['labor:settlement:create']" type="primary" @click="onAdd">
            <template #icon><icon-plus /></template>
            <template #default>新增</template>
          </a-button>
          <a-input-number v-model="queryContractId" :min="1" placeholder="目标合同ID" hide-button style="width: 150px" />
          <a-date-picker v-model="activeDate" value-format="YYYY-MM-DD" style="width: 150px" />
          <a-button v-permission="['labor:settlement:version']" @click="queryVersion">版本列表</a-button>
          <a-button v-permission="['labor:settlement:active']" @click="queryActive">有效规则</a-button>
        </a-space>
      </template>
      <template #ruleType="{ record }">
        {{ getRuleTypeLabel(record.ruleType) }}
      </template>
      <template #status="{ record }">
        <a-tag :color="SETTLEMENT_STATUS_COLOR_MAP[record.status] ?? 'arcoblue'">
          {{ getSettlementStatusLabel(record.status) }}
        </a-tag>
      </template>
      <template #action="{ record }">
        <a-space>
          <a-link v-permission="['labor:settlement:update']" @click="onUpdate(record)">修改</a-link>
          <a-link v-permission="['labor:settlement:publish']" @click="onPublish(record)">发布</a-link>
          <a-link v-permission="['labor:settlement:deactivate']" status="warning" @click="onDeactivate(record)">停用</a-link>
          <a-link v-permission="['labor:settlement:delete']" status="danger" @click="onDelete(record)">删除</a-link>
        </a-space>
      </template>
    </GiTable>

    <a-modal
      v-model:visible="formVisible"
      :title="formMode === 'create' ? '新增结算规则' : '修改结算规则'"
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
            <a-form-item field="ruleType" label="规则类型" :rules="[{ required: true, message: '请选择规则类型' }]">
              <a-select v-model="formState.ruleType" :options="SETTLEMENT_RULE_TYPE_OPTIONS" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="versionNo" label="版本号" :rules="[{ required: true, message: '请输入版本号' }]">
              <a-input-number v-model="formState.versionNo" :min="1" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="effectiveFrom" label="生效日期" :rules="[{ required: true, message: '请选择生效日期' }]">
              <a-date-picker v-model="formState.effectiveFrom" value-format="YYYY-MM-DD" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item field="rulePayload" label="规则内容(JSON)" :rules="[{ required: true, message: '请输入规则内容' }]">
              <a-textarea v-model="formState.rulePayload" :max-length="5000" show-word-limit :auto-size="{ minRows: 6 }" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>

    <a-drawer v-model:visible="versionVisible" title="规则版本列表" :width="900">
      <a-table :data="versionList" row-key="id" :pagination="false">
        <template #columns>
          <a-table-column title="ID" data-index="id" :width="90" />
          <a-table-column title="版本号" data-index="versionNo" :width="90" />
          <a-table-column title="规则类型" :width="120">
            <template #cell="{ record }">
              {{ getRuleTypeLabel(record.ruleType) }}
            </template>
          </a-table-column>
          <a-table-column title="生效日期" data-index="effectiveFrom" :width="120" />
          <a-table-column title="状态" :width="120">
            <template #cell="{ record }">
              {{ getSettlementStatusLabel(record.status) }}
            </template>
          </a-table-column>
          <a-table-column title="发布时间" data-index="publishedAt" :width="180" />
          <a-table-column title="停用时间" data-index="deactivatedAt" :width="180" />
        </template>
      </a-table>
    </a-drawer>

    <a-modal v-model:visible="activeVisible" title="有效规则" footer="false">
      <a-descriptions :column="1" bordered>
        <a-descriptions-item label="规则ID">{{ activeRule?.id || '-' }}</a-descriptions-item>
        <a-descriptions-item label="合同ID">{{ activeRule?.contractId || '-' }}</a-descriptions-item>
        <a-descriptions-item label="版本号">{{ activeRule?.versionNo || '-' }}</a-descriptions-item>
        <a-descriptions-item label="规则类型">{{ getRuleTypeLabel(activeRule?.ruleType) }}</a-descriptions-item>
        <a-descriptions-item label="生效日期">{{ activeRule?.effectiveFrom || '-' }}</a-descriptions-item>
        <a-descriptions-item label="状态">{{ getSettlementStatusLabel(activeRule?.status) }}</a-descriptions-item>
        <a-descriptions-item label="规则内容">{{ activeRule?.rulePayload || '-' }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </GiPageLayout>
</template>

<script setup lang="ts">
import type { FormInstance, TableInstance } from '@arco-design/web-vue'
import { Message } from '@arco-design/web-vue'
import {
  type SettlementRuleQuery,
  type SettlementRuleReq,
  type SettlementRuleResp,
  addSettlementRule,
  deactivateSettlementRule,
  deleteSettlementRule,
  getActiveSettlementRule,
  listSettlementRule,
  listSettlementRuleVersion,
  publishSettlementRule,
  updateSettlementRule,
} from '@/apis/labor/settlement'
import {
  SETTLEMENT_RULE_TYPE_LABEL_MAP,
  SETTLEMENT_RULE_TYPE_OPTIONS,
  SETTLEMENT_STATUS_COLOR_MAP,
  SETTLEMENT_STATUS_LABEL_MAP,
  SETTLEMENT_STATUS_OPTIONS,
  toLaborLabel,
} from '@/constant/labor'
import { useTable } from '@/hooks'
import has from '@/utils/has'

defineOptions({ name: 'LaborSettlement' })

const route = useRoute()

const getRuleTypeLabel = (value?: string) => toLaborLabel(SETTLEMENT_RULE_TYPE_LABEL_MAP, value)
const getSettlementStatusLabel = (value?: string) => toLaborLabel(SETTLEMENT_STATUS_LABEL_MAP, value)

const queryForm = reactive<SettlementRuleQuery>({
  contractId: route.query.contractId ? Number(route.query.contractId) : undefined,
  sort: ['createTime,desc'],
})

const {
  tableData: dataList,
  loading,
  pagination,
  search,
  handleDelete,
} = useTable((page) => listSettlementRule({ ...queryForm, ...page }), { immediate: true })

const columns: TableInstance['columns'] = [
  {
    title: '序号',
    width: 66,
    align: 'center',
    render: ({ rowIndex }) => h('span', {}, rowIndex + 1 + (pagination.current - 1) * pagination.pageSize),
  },
  { title: '规则ID', dataIndex: 'id', width: 90 },
  { title: '合同ID', dataIndex: 'contractId', width: 100 },
  { title: '版本号', dataIndex: 'versionNo', width: 90 },
  { title: '规则类型', dataIndex: 'ruleType', slotName: 'ruleType', width: 120 },
  { title: '生效日期', dataIndex: 'effectiveFrom', width: 120 },
  { title: '状态', dataIndex: 'status', slotName: 'status', width: 120, align: 'center' },
  { title: '发布时间', dataIndex: 'publishedAt', width: 180 },
  { title: '停用时间', dataIndex: 'deactivatedAt', width: 180 },
  { title: '创建时间', dataIndex: 'createTime', width: 180 },
  {
    title: '操作',
    dataIndex: 'action',
    slotName: 'action',
    width: 220,
    align: 'center',
    fixed: 'right',
    show: has.hasPermOr(['labor:settlement:update', 'labor:settlement:publish', 'labor:settlement:deactivate', 'labor:settlement:delete']),
  },
]

const formVisible = ref(false)
const saveLoading = ref(false)
const formMode = ref<'create' | 'update'>('create')
const formId = ref<string | number>('')
const formRef = ref<FormInstance>()

const getDefaultFormState = (): SettlementRuleReq => ({
  contractId: queryForm.contractId ?? 1,
  ruleType: 'RATE',
  versionNo: 1,
  effectiveFrom: '',
  rulePayload: '{"mode":"fixed","value":0}',
})

const formState = reactive<SettlementRuleReq>(getDefaultFormState())

const queryContractId = ref<number | undefined>(queryForm.contractId)
const activeDate = ref('')
const versionVisible = ref(false)
const versionList = ref<SettlementRuleResp[]>([])
const activeVisible = ref(false)
const activeRule = ref<SettlementRuleResp | null>(null)

const reset = () => {
  queryForm.contractId = undefined
  queryForm.status = undefined
  search()
}

const onAdd = () => {
  formMode.value = 'create'
  formId.value = ''
  Object.assign(formState, getDefaultFormState())
  formVisible.value = true
}

const onUpdate = (record: SettlementRuleResp) => {
  formMode.value = 'update'
  formId.value = record.id
  Object.assign(formState, {
    contractId: Number(record.contractId),
    ruleType: record.ruleType,
    versionNo: record.versionNo,
    effectiveFrom: record.effectiveFrom,
    rulePayload: record.rulePayload,
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
      await addSettlementRule(formState)
    } else {
      await updateSettlementRule(formState, formId.value)
    }
    Message.success('保存成功')
    formVisible.value = false
    search()
    return true
  } finally {
    saveLoading.value = false
  }
}

const onPublish = async (record: SettlementRuleResp) => {
  await publishSettlementRule(record.id)
  Message.success('发布成功')
  search()
}

const onDeactivate = async (record: SettlementRuleResp) => {
  await deactivateSettlementRule(record.id)
  Message.success('停用成功')
  search()
}

const onDelete = (record: SettlementRuleResp) => {
  return handleDelete(() => deleteSettlementRule(record.id), {
    content: `是否确定删除规则版本「${record.versionNo}」？`,
    showModal: true,
  })
}

const queryVersion = async () => {
  if (!queryContractId.value) {
    Message.warning('请先输入合同ID')
    return
  }
  const { data } = await listSettlementRuleVersion(queryContractId.value)
  versionList.value = data
  versionVisible.value = true
}

const queryActive = async () => {
  if (!queryContractId.value) {
    Message.warning('请先输入合同ID')
    return
  }
  const { data } = await getActiveSettlementRule(queryContractId.value, activeDate.value || undefined)
  activeRule.value = data
  activeVisible.value = true
}
</script>

<style scoped lang="scss"></style>
