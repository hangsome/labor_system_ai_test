<template>
  <GiPageLayout>
    <a-card :loading="loading" :bordered="false" class="mb-3">
      <template #title>
        线索详情
      </template>
      <template #extra>
        <a-button @click="goBack">返回</a-button>
      </template>
      <a-descriptions :column="2" bordered>
        <a-descriptions-item label="线索编号">{{ leadInfo.leadCode || '-' }}</a-descriptions-item>
        <a-descriptions-item label="项目名称">{{ leadInfo.projectName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="联系人">{{ leadInfo.contactName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="联系方式">{{ leadInfo.contactPhone || '-' }}</a-descriptions-item>
        <a-descriptions-item label="行业类型">{{ leadInfo.industryType || '-' }}</a-descriptions-item>
        <a-descriptions-item label="负责人ID">{{ leadInfo.bizOwnerId || '-' }}</a-descriptions-item>
        <a-descriptions-item label="合作状态">
          <a-tag :color="statusColorMap[leadInfo.cooperationStatus] ?? 'arcoblue'">{{ leadInfo.cooperationStatus || '-' }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="保证金状态">{{ leadInfo.depositStatus || '-' }}</a-descriptions-item>
        <a-descriptions-item label="招标日期">{{ leadInfo.tenderAt || '-' }}</a-descriptions-item>
        <a-descriptions-item label="更新时间">{{ leadInfo.updateTime || '-' }}</a-descriptions-item>
      </a-descriptions>
    </a-card>

    <a-row :gutter="16">
      <a-col :span="12">
        <a-card title="状态流转" :bordered="false">
          <a-space wrap>
            <a-button
              v-for="status in transitionStatusOptions"
              :key="status"
              v-permission="['labor:lead:transition']"
              @click="onTransition(status)"
            >
              转为 {{ status }}
            </a-button>
          </a-space>
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card title="新增跟进" :bordered="false">
          <a-form ref="followUpFormRef" :model="followUpForm" auto-label-width>
            <a-form-item field="content" label="跟进内容" :rules="[{ required: true, message: '请输入跟进内容' }]">
              <a-textarea v-model="followUpForm.content" :max-length="512" show-word-limit />
            </a-form-item>
            <a-form-item field="nextContactAt" label="下次联系时间">
              <a-date-picker
                v-model="followUpForm.nextContactAt"
                value-format="YYYY-MM-DD HH:mm:ss"
                show-time
                style="width: 100%"
              />
            </a-form-item>
            <a-space>
              <a-button v-permission="['labor:lead:followUp:create']" type="primary" :loading="followUpLoading" @click="submitFollowUp">
                提交
              </a-button>
              <a-button @click="resetFollowUp">重置</a-button>
            </a-space>
          </a-form>
        </a-card>
      </a-col>
    </a-row>

    <a-card title="跟进记录" :bordered="false" class="mt-3" :loading="timelineLoading">
      <a-timeline>
        <a-timeline-item v-for="item in followUps" :key="item.id">
          <div class="follow-up-item">
            <div>
              <strong>{{ item.action }}</strong>
              <a-tag v-if="item.status" class="ml-2">{{ item.status }}</a-tag>
            </div>
            <div class="text-secondary">{{ item.content || '-' }}</div>
            <div class="text-secondary">
              {{ item.statusFrom ? `from: ${item.statusFrom}` : '' }}
              {{ item.statusTo ? ` to: ${item.statusTo}` : '' }}
              {{ item.nextContactAt ? ` next: ${item.nextContactAt}` : '' }}
            </div>
            <div class="text-secondary">{{ item.createTime || '-' }}</div>
          </div>
        </a-timeline-item>
      </a-timeline>
      <a-empty v-if="!followUps.length" description="暂无跟进记录" />
    </a-card>
  </GiPageLayout>
</template>

<script setup lang="ts">
import type { FormInstance } from '@arco-design/web-vue'
import { Message } from '@arco-design/web-vue'
import {
  type LeadFollowUpReq,
  type LeadFollowUpResp,
  type LeadResp,
  createLeadFollowUp,
  getLead,
  listLeadFollowUp,
  transitionLeadStatus,
} from '@/apis/labor/lead'

defineOptions({ name: 'LaborLeadDetail' })

const route = useRoute()
const router = useRouter()

const leadId = computed(() => String(route.query.id ?? ''))
const loading = ref(false)
const timelineLoading = ref(false)
const followUpLoading = ref(false)

const transitionStatusOptions = ['FOLLOWING', 'WON', 'LOST']
const statusColorMap: Record<string, string> = {
  NEW: 'arcoblue',
  FOLLOWING: 'orangered',
  WON: 'green',
  LOST: 'gray',
}

const leadInfo = reactive<Partial<LeadResp>>({})
const followUps = ref<LeadFollowUpResp[]>([])

const followUpFormRef = ref<FormInstance>()
const getDefaultFollowUp = (): LeadFollowUpReq => ({
  content: '',
  nextContactAt: '',
})
const followUpForm = reactive<LeadFollowUpReq>(getDefaultFollowUp())

const loadLead = async () => {
  if (!leadId.value) {
    Message.warning('缺少线索 ID')
    return
  }
  loading.value = true
  try {
    const { data } = await getLead(leadId.value)
    Object.assign(leadInfo, data)
  } finally {
    loading.value = false
  }
}

const loadFollowUp = async () => {
  if (!leadId.value) return
  timelineLoading.value = true
  try {
    const { data } = await listLeadFollowUp(leadId.value)
    followUps.value = data
  } finally {
    timelineLoading.value = false
  }
}

const onTransition = async (toStatus: string) => {
  if (!leadId.value) return
  await transitionLeadStatus(leadId.value, { toStatus })
  Message.success('状态流转成功')
  await loadLead()
  await loadFollowUp()
}

function resetFollowUp() {
  Object.assign(followUpForm, getDefaultFollowUp())
  followUpFormRef.value?.clearValidate()
}

const submitFollowUp = async () => {
  if (!leadId.value) return
  const error = await followUpFormRef.value?.validate()
  if (error) return

  followUpLoading.value = true
  try {
    await createLeadFollowUp(leadId.value, followUpForm)
    Message.success('跟进新增成功')
    resetFollowUp()
    await loadFollowUp()
  } finally {
    followUpLoading.value = false
  }
}

const goBack = () => {
  router.push('/labor/crm/lead')
}

watch(
  () => leadId.value,
  async () => {
    await loadLead()
    await loadFollowUp()
  },
  { immediate: true },
)
</script>

<style scoped lang="scss">
.mb-3 {
  margin-bottom: 16px;
}

.mt-3 {
  margin-top: 16px;
}

.ml-2 {
  margin-left: 8px;
}

.text-secondary {
  color: var(--color-text-3);
  margin-top: 4px;
}
</style>
