<template>
  <div class="contract-detail-page" data-testid="contract-detail-view">
    <el-page-header @back="goBack" content="Contract Detail" />

    <el-row :gutter="16" class="layout-row">
      <el-col :xs="24" :md="10">
        <el-card shadow="hover" data-testid="contract-basic-card">
          <template #header>
            <div class="card-header">
              <h2>{{ contract.contractNo }}</h2>
              <el-tag :type="statusTagType" data-testid="contract-status">
                {{ contract.status }}
              </el-tag>
            </div>
          </template>
          <div class="field-item"><strong>Lead:</strong> {{ contract.leadCode }}</div>
          <div class="field-item"><strong>Employer:</strong> {{ contract.employerUnit }}</div>
          <div class="field-item"><strong>Effective:</strong> {{ contract.effectiveDate }}</div>
          <div class="field-item"><strong>Expire:</strong> {{ contract.expireDate }}</div>
        </el-card>

        <el-card shadow="never" class="action-card" data-testid="contract-action-card">
          <template #header>
            <h3>Lifecycle Actions</h3>
          </template>
          <div class="action-row">
            <el-button
              type="success"
              data-testid="contract-sign-button"
              :disabled="contract.status !== 'DRAFT'"
              @click="signContract"
            >
              Sign
            </el-button>
            <el-button
              type="primary"
              data-testid="contract-renew-button"
              :disabled="contract.status !== 'ACTIVE'"
              @click="renewContract"
            >
              Renew
            </el-button>
            <el-button
              type="danger"
              data-testid="contract-terminate-button"
              :disabled="contract.status !== 'ACTIVE'"
              @click="terminateContract"
            >
              Terminate
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
      </el-col>

      <el-col :xs="24" :md="14">
        <el-card shadow="hover" data-testid="contract-history-card">
          <template #header>
            <h3>Lifecycle History</h3>
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
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

type ContractStatus = 'DRAFT' | 'ACTIVE' | 'TERMINATED'
type HistoryItem = {
  id: number
  title: string
  desc: string
  time: string
}

const route = useRoute()
const router = useRouter()
const contractId = String(route.params.contractId ?? 'LC-2026-001')

const contract = reactive({
  contractNo: contractId,
  leadCode: 'LEAD-001',
  employerUnit: 'Huadong Hotel Group',
  status: 'DRAFT' as ContractStatus,
  effectiveDate: '2026-03-01',
  expireDate: '2027-02-28',
})

const history = ref<HistoryItem[]>([
  {
    id: 1,
    title: 'Draft Created',
    desc: 'Contract draft initialized from lead conversion.',
    time: '2026-02-27 10:00',
  },
])

const feedback = ref('')
const feedbackType = ref<'success' | 'error'>('success')

const statusTagType = computed(() => {
  if (contract.status === 'ACTIVE') {
    return 'success'
  }
  if (contract.status === 'TERMINATED') {
    return 'danger'
  }
  return 'info'
})

function appendHistory(title: string, desc: string) {
  history.value.unshift({
    id: Date.now(),
    title,
    desc,
    time: 'just now',
  })
}

function setFeedback(type: 'success' | 'error', message: string) {
  feedbackType.value = type
  feedback.value = message
}

function signContract() {
  if (contract.status !== 'DRAFT') {
    setFeedback('error', 'Only DRAFT contracts can be signed.')
    return
  }
  contract.status = 'ACTIVE'
  appendHistory('Contract Signed', 'Contract moved from DRAFT to ACTIVE.')
  setFeedback('success', 'Contract signed successfully.')
}

function renewContract() {
  if (contract.status !== 'ACTIVE') {
    setFeedback('error', 'Only ACTIVE contracts can be renewed.')
    return
  }
  contract.expireDate = '2028-02-28'
  appendHistory('Contract Renewed', 'Contract term has been extended by one year.')
  setFeedback('success', 'Contract renewed successfully.')
}

function terminateContract() {
  if (contract.status !== 'ACTIVE') {
    setFeedback('error', 'Only ACTIVE contracts can be terminated.')
    return
  }
  contract.status = 'TERMINATED'
  appendHistory('Contract Terminated', 'Contract has been terminated and locked.')
  setFeedback('success', 'Contract terminated successfully.')
}

function goBack() {
  router.push('/contracts/labor-contracts')
}
</script>

<style scoped>
.contract-detail-page {
  min-height: 100vh;
  padding: 24px;
}

.layout-row {
  margin-top: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.card-header h2 {
  margin: 0;
  font-size: 20px;
  color: #1f2937;
}

.field-item {
  margin: 10px 0;
  color: #374151;
}

.action-card {
  margin-top: 12px;
}

.action-row {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.feedback-tip {
  margin-top: 12px;
}
</style>
