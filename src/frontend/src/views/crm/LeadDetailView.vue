<template>
  <div class="lead-detail-page" data-testid="lead-detail-view">
    <el-page-header @back="goBack" content="线索详情" />

    <el-row :gutter="16" class="layout-row">
      <el-col :xs="24" :md="10">
        <el-card shadow="hover" data-testid="lead-basic-card">
          <template #header>
            <div class="card-header">
              <h2>{{ lead.projectName }}</h2>
              <el-tag type="primary">{{ lead.status }}</el-tag>
            </div>
          </template>
          <div class="field-item"><strong>线索编号：</strong>{{ lead.leadCode }}</div>
          <div class="field-item"><strong>负责人：</strong>{{ lead.bizOwner }}</div>
          <div class="field-item"><strong>最近更新：</strong>{{ lead.updatedAt }}</div>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="14">
        <el-card shadow="hover" data-testid="lead-followup-card">
          <template #header>
            <div class="card-header">
              <h2>跟进时间线</h2>
              <el-tag type="success">共 {{ followUps.length }} 条</el-tag>
            </div>
          </template>

          <el-timeline data-testid="lead-timeline">
            <el-timeline-item
              v-for="item in followUps"
              :key="item.id"
              :timestamp="item.time"
              placement="top"
            >
              <el-card shadow="never">
                <h4>{{ item.operator }}</h4>
                <p>{{ item.content }}</p>
              </el-card>
            </el-timeline-item>
          </el-timeline>

          <el-divider />

          <el-form label-position="top" data-testid="lead-followup-form">
            <el-form-item label="跟进内容">
              <el-input
                v-model.trim="followForm.content"
                type="textarea"
                :rows="3"
                maxlength="200"
                show-word-limit
                placeholder="输入本次跟进内容"
              />
            </el-form-item>
            <el-form-item label="下次联系时间（可选）">
              <el-date-picker
                v-model="followForm.nextContactAt"
                type="datetime"
                placeholder="选择下次联系时间"
                style="width: 100%"
              />
            </el-form-item>
            <el-alert
              v-if="formError"
              type="error"
              :title="formError"
              :closable="false"
              show-icon
              class="error-tip"
              data-testid="lead-followup-error"
            />
            <el-button type="primary" data-testid="lead-followup-submit" @click="submitFollowUp">
              保存跟进
            </el-button>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

type FollowUpItem = {
  id: number
  operator: string
  content: string
  time: string
}

const route = useRoute()
const router = useRouter()
const leadId = String(route.params.leadId ?? 'LEAD-001')

const lead = reactive({
  leadCode: leadId,
  projectName: '华东酒店劳务外包项目',
  bizOwner: '王磊',
  status: 'FOLLOWING',
  updatedAt: '2026-02-27 09:10',
})

const followUps = ref<FollowUpItem[]>([
  {
    id: 1,
    operator: '王磊',
    content: '客户确认下周评估现场排班方案。',
    time: '2026-02-27 09:00',
  },
  {
    id: 2,
    operator: '李婷',
    content: '已发送报价清单，等待财务确认。',
    time: '2026-02-26 16:20',
  },
])

const followForm = reactive({
  content: '',
  nextContactAt: '',
})
const formError = ref('')

function goBack() {
  router.push('/crm/leads')
}

function submitFollowUp() {
  if (!followForm.content) {
    formError.value = '请填写跟进内容'
    return
  }

  formError.value = ''
  followUps.value.unshift({
    id: Date.now(),
    operator: '当前用户',
    content: followForm.content,
    time: '刚刚',
  })
  followForm.content = ''
  followForm.nextContactAt = ''
}
</script>

<style scoped>
.lead-detail-page {
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

.error-tip {
  margin-bottom: 12px;
}
</style>
