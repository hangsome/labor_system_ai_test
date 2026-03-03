<template>
  <GiPageLayout>
    <a-card :loading="loading" :bordered="false">
      <template #title>合同详情</template>
      <template #extra>
        <a-space>
          <a-button @click="goSettlement">结算规则</a-button>
          <a-button @click="goBack">返回</a-button>
        </a-space>
      </template>
      <a-descriptions :column="2" bordered>
        <a-descriptions-item label="合同编号">{{ contractInfo.contractNo || '-' }}</a-descriptions-item>
        <a-descriptions-item label="合同名称">{{ contractInfo.contractName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="单位ID">{{ contractInfo.employerUnitId || '-' }}</a-descriptions-item>
        <a-descriptions-item label="合同类型">{{ contractInfo.contractType || '-' }}</a-descriptions-item>
        <a-descriptions-item label="开始日期">{{ contractInfo.startDate || '-' }}</a-descriptions-item>
        <a-descriptions-item label="结束日期">{{ contractInfo.endDate || '-' }}</a-descriptions-item>
        <a-descriptions-item label="结算周期">{{ contractInfo.settlementCycle || '-' }}</a-descriptions-item>
        <a-descriptions-item label="税率">{{ contractInfo.taxRate || '-' }}</a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="statusColorMap[contractInfo.status || ''] ?? 'arcoblue'">{{ contractInfo.status || '-' }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="更新时间">{{ contractInfo.updateTime || '-' }}</a-descriptions-item>
      </a-descriptions>
    </a-card>

    <a-card title="结算规则版本" :bordered="false" class="mt-3" :loading="versionLoading">
      <template #extra>
        <a-space>
          <a-date-picker v-model="activeDate" value-format="YYYY-MM-DD" style="width: 160px" />
          <a-button @click="loadActiveRule">查询有效规则</a-button>
        </a-space>
      </template>

      <a-alert v-if="activeRule" type="success" class="mb-3">
        当前有效规则：version {{ activeRule.versionNo }} / status {{ activeRule.status }} / effective {{ activeRule.effectiveFrom }}
      </a-alert>

      <a-table :data="versions" :pagination="false" row-key="id" :scroll="{ x: '100%', minWidth: 900 }">
        <template #columns>
          <a-table-column title="版本ID" data-index="id" :width="90" />
          <a-table-column title="版本号" data-index="versionNo" :width="90" />
          <a-table-column title="规则类型" data-index="ruleType" :width="120" />
          <a-table-column title="生效日期" data-index="effectiveFrom" :width="120" />
          <a-table-column title="状态" data-index="status" :width="120" />
          <a-table-column title="发布时间" data-index="publishedAt" :width="180" />
          <a-table-column title="停用时间" data-index="deactivatedAt" :width="180" />
          <a-table-column title="规则内容" :width="260">
            <template #cell="{ record }">
              <a-typography-paragraph :ellipsis="{ rows: 2 }" copyable>
                {{ record.rulePayload }}
              </a-typography-paragraph>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </a-card>
  </GiPageLayout>
</template>

<script setup lang="ts">
import { Message } from '@arco-design/web-vue'
import type { ContractResp } from '@/apis/labor/contract'
import { getContract } from '@/apis/labor/contract'
import {
  type SettlementRuleResp,
  getActiveSettlementRule,
  listSettlementRuleVersion,
} from '@/apis/labor/settlement'

defineOptions({ name: 'LaborContractDetail' })

const route = useRoute()
const router = useRouter()

const contractId = computed(() => String(route.query.id ?? ''))
const loading = ref(false)
const versionLoading = ref(false)

const statusColorMap: Record<string, string> = {
  DRAFT: 'arcoblue',
  SIGNED: 'green',
  TERMINATED: 'gray',
}

const contractInfo = reactive<Partial<ContractResp>>({})
const versions = ref<SettlementRuleResp[]>([])
const activeRule = ref<SettlementRuleResp | null>(null)
const activeDate = ref('')

const loadContract = async () => {
  if (!contractId.value) {
    Message.warning('缺少合同 ID')
    return
  }
  loading.value = true
  try {
    const { data } = await getContract(contractId.value)
    Object.assign(contractInfo, data)
  } finally {
    loading.value = false
  }
}

const loadVersions = async () => {
  if (!contractId.value) return
  versionLoading.value = true
  try {
    const { data } = await listSettlementRuleVersion(contractId.value)
    versions.value = data
  } finally {
    versionLoading.value = false
  }
}

const loadActiveRule = async () => {
  if (!contractId.value) return
  const { data } = await getActiveSettlementRule(contractId.value, activeDate.value || undefined)
  activeRule.value = data
}

const goBack = () => {
  router.push('/labor/contract')
}

const goSettlement = () => {
  router.push({ path: '/labor/contract/settlement', query: { contractId: contractId.value } })
}

watch(
  () => contractId.value,
  async () => {
    await loadContract()
    await loadVersions()
  },
  { immediate: true },
)
</script>

<style scoped lang="scss">
.mt-3 {
  margin-top: 16px;
}

.mb-3 {
  margin-bottom: 16px;
}
</style>
