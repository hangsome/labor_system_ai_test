<template>
  <div class="contract-detail-page" data-testid="contract-detail-view">
    <el-page-header @back="goBack" content="劳务合同详情" class="back-header" />

    <section class="head-row">
      <div>
        <h1>{{ contract.contractNo }}</h1>
        <p>客户：{{ contract.employerUnit }} ｜ 线索编号：{{ contract.leadCode }}</p>
      </div>
      <el-tag :type="statusTagType" data-testid="contract-status">{{ statusText }}</el-tag>
    </section>

    <el-card shadow="never" class="action-card" data-testid="contract-action-card">
      <template #header>
        <h3>生命周期操作</h3>
      </template>
      <div class="action-row">
        <el-button
          type="success"
          data-testid="contract-sign-button"
          :disabled="contract.status !== 'DRAFT'"
          @click="signContract"
        >
          合同签署
        </el-button>
        <el-button
          type="primary"
          data-testid="contract-renew-button"
          :disabled="contract.status !== 'ACTIVE'"
          @click="renewContract"
        >
          合同续签
        </el-button>
        <el-button
          type="danger"
          data-testid="contract-terminate-button"
          :disabled="contract.status !== 'ACTIVE'"
          @click="terminateContract"
        >
          合同终止
        </el-button>
      </div>
      <el-alert
        v-if="feedback"
        :type="feedbackType"
        :title="feedback"
        :closable="false"
        show-icon
        class="feedback-tip"
        data-testid="contract-action-feedback"
      />
    </el-card>

    <el-row :gutter="14" class="layout-row">
      <el-col :xs="24" :lg="14">
        <el-card shadow="never" class="base-card" data-testid="contract-basic-card">
          <template #header>
            <h3>合同基本信息</h3>
          </template>
          <div class="form-grid">
            <div>
              <label>客户名称</label>
              <p>{{ contract.employerUnit }}</p>
            </div>
            <div>
              <label>客户类型</label>
              <p>酒店</p>
            </div>
            <div>
              <label>业务类别</label>
              <p>劳务派遣</p>
            </div>
            <div>
              <label>合同开始日期</label>
              <p>{{ contract.effectiveDate }}</p>
            </div>
            <div>
              <label>合同结束日期</label>
              <p>{{ contract.expireDate }}</p>
            </div>
            <div>
              <label>结算周期</label>
              <p>按月结算</p>
            </div>
            <div>
              <label>服务费率（元）</label>
              <p>5000.00</p>
            </div>
            <div>
              <label>企业最低用工数量（人）</label>
              <p>12000.00</p>
            </div>
          </div>
        </el-card>

        <el-card shadow="never" class="history-card" data-testid="contract-history-card">
          <template #header>
            <h3>生命周期记录</h3>
          </template>
          <el-timeline data-testid="contract-history-list">
            <el-timeline-item
              v-for="item in history"
              :key="item.id"
              :timestamp="item.time"
              placement="top"
            >
              <el-card shadow="never">
                <h4>{{ item.title }}</h4>
                <p>{{ item.desc }}</p>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="10">
        <el-card shadow="never" class="rule-card" data-testid="contract-rule-card">
          <template #header>
            <h3>结算规则版本</h3>
          </template>
          <el-table :data="ruleVersions" border stripe data-testid="contract-rule-table">
            <el-table-column prop="versionNo" label="版本号" min-width="120" />
            <el-table-column prop="effectiveDate" label="生效日期" min-width="120" />
            <el-table-column label="状态" min-width="110">
              <template #default="{ row }">
                <el-tag :type="getRuleStatusTagType(row.status)" effect="light">
                  {{ getRuleStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <el-card shadow="never" class="remark-card">
          <template #header>
            <h3>结算说明</h3>
          </template>
          <p>
            合同执行后按月 15 日前结算，结算金额按上月确认工时与结算单价计算；终止后仅允许通过调整单修正。
          </p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  contractStatusTagMap,
  contractStatusTextMap,
  ruleStatusTextMap,
  type ContractStatusCode,
} from '../../constants/status'

type ContractStatus = 'DRAFT' | 'ACTIVE' | 'TERMINATED'
type HistoryItem = {
  id: number
  title: string
  desc: string
  time: string
}
type RuleVersionRow = {
  versionNo: string
  effectiveDate: string
  status: 'ACTIVE' | 'DRAFT' | 'INACTIVE'
}

const route = useRoute()
const router = useRouter()
const contractId = String(route.params.contractId ?? 'LC-2026-001')

const contract = reactive({
  contractNo: contractId,
  leadCode: 'LEAD-001',
  employerUnit: 'XX国际酒店有限公司',
  status: 'DRAFT' as ContractStatus,
  effectiveDate: '2026-03-01',
  expireDate: '2027-02-28',
})

const history = ref<HistoryItem[]>([
  {
    id: 1,
    title: '合同草稿创建',
    desc: '由线索 LEAD-001 转化生成合同草稿。',
    time: '2026-02-27 10:00',
  },
])

const ruleVersions = ref<RuleVersionRow[]>([
  {
    versionNo: 'RV-2026-01',
    effectiveDate: '2026-03-01',
    status: 'ACTIVE',
  },
  {
    versionNo: 'RV-2026-00',
    effectiveDate: '2026-01-01',
    status: 'INACTIVE',
  },
])

const feedback = ref('')
const feedbackType = ref<'success' | 'error'>('success')

const statusText = computed(() => {
  return contractStatusTextMap[contract.status as ContractStatusCode] ?? '未知状态'
})

const statusTagType = computed(() => {
  return contractStatusTagMap[contract.status as ContractStatusCode] ?? 'info'
})

function getRuleStatusText(status: RuleVersionRow['status']) {
  return ruleStatusTextMap[status] ?? '未知状态'
}

function getRuleStatusTagType(status: RuleVersionRow['status']) {
  if (status === 'ACTIVE') {
    return 'success'
  }
  if (status === 'DRAFT') {
    return 'info'
  }
  return 'warning'
}

function appendHistory(title: string, desc: string) {
  history.value.unshift({
    id: Date.now(),
    title,
    desc,
    time: '刚刚',
  })
}

function setFeedback(type: 'success' | 'error', message: string) {
  feedbackType.value = type
  feedback.value = message
}

function signContract() {
  if (contract.status !== 'DRAFT') {
    setFeedback('error', '仅草稿状态可签署')
    return
  }
  contract.status = 'ACTIVE'
  appendHistory('合同签署完成', '合同从草稿变更为进行中。')
  setFeedback('success', '合同签署成功')
}

function renewContract() {
  if (contract.status !== 'ACTIVE') {
    setFeedback('error', '仅进行中合同可续签')
    return
  }
  contract.expireDate = '2028-02-28'
  appendHistory('合同续签完成', '合同期限已延长一年。')
  setFeedback('success', '合同续签成功')
}

function terminateContract() {
  if (contract.status !== 'ACTIVE') {
    setFeedback('error', '仅进行中合同可终止')
    return
  }
  contract.status = 'TERMINATED'
  appendHistory('合同终止完成', '合同已终止并进入锁定状态。')
  setFeedback('success', '合同终止成功')
}

function goBack() {
  const historyState = router.options.history.state as { back?: string | null } | null
  if (historyState?.back) {
    router.back()
    return
  }
  router.push('/contracts/labor-contracts')
}
</script>

<style scoped>
.contract-detail-page {
  padding: 18px;
}

.back-header {
  margin-bottom: 8px;
}

.head-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.head-row h1 {
  margin: 0;
  font-size: 30px;
  color: #1f2937;
}

.head-row p {
  margin: 8px 0 0;
  color: #6b7280;
}

.action-card,
.base-card,
.history-card,
.rule-card,
.remark-card {
  margin-top: 12px;
  border-radius: 12px;
  border: 1px solid #edf0f6;
}

.action-card h3,
.base-card h3,
.history-card h3,
.rule-card h3,
.remark-card h3 {
  margin: 0;
  font-size: 17px;
}

.action-row {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.feedback-tip {
  margin-top: 12px;
}

.layout-row {
  margin-top: 2px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.form-grid label {
  color: #9ca3af;
  font-size: 12px;
}

.form-grid p {
  margin: 8px 0 0;
  color: #1f2937;
  font-size: 14px;
}

.remark-card p {
  margin: 0;
  color: #6b7280;
  line-height: 1.7;
}

@media (max-width: 1120px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .contract-detail-page {
    padding: 12px;
  }

  .head-row {
    flex-direction: column;
  }
}
</style>
