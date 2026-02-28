<template>
  <div class="lead-detail-page" data-testid="lead-detail-view">
    <div class="page-actions">
      <el-page-header @back="goBack" content="客户线索详情" class="page-back" />
      <el-button @click="goBack">返回列表</el-button>
    </div>

    <section class="head-block">
      <div>
        <h1>{{ lead.projectName }}</h1>
        <p>线索编号：{{ lead.leadCode }} ｜ 负责人：{{ lead.bizOwner }}</p>
      </div>
      <el-tag :type="getLeadStatusTagType(lead.status)">{{ getLeadStatusText(lead.status) }}</el-tag>
    </section>

    <el-card shadow="never" class="base-card" data-testid="lead-basic-card">
      <template #header>
        <h3>员工派遣基础信息</h3>
      </template>
      <div class="info-grid">
        <div>
          <label>客户行业</label>
          <p>酒店</p>
        </div>
        <div>
          <label>客户类型</label>
          <p>A级（整资资金类）</p>
        </div>
        <div>
          <label>合作状态</label>
          <p>{{ getLeadStatusText(lead.status) }}</p>
        </div>
        <div>
          <label>最近更新时间</label>
          <p>{{ lead.updatedAt }}</p>
        </div>
      </div>
    </el-card>

    <el-row :gutter="14" class="detail-row">
      <el-col :xs="24" :lg="14">
        <el-card shadow="never" class="contact-card">
          <template #header>
            <h3>客户联系人</h3>
          </template>
          <el-table :data="contacts" border>
            <el-table-column prop="name" label="姓名" min-width="100" />
            <el-table-column prop="position" label="岗位" min-width="120" />
            <el-table-column prop="mobile" label="联系方式" min-width="150" />
            <el-table-column prop="email" label="邮箱" min-width="170" />
          </el-table>
        </el-card>

        <el-card shadow="never" class="timeline-card" data-testid="lead-followup-card">
          <template #header>
            <div class="card-title-row">
              <h3>线索跟进记录</h3>
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
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="10">
        <el-card shadow="never" class="followup-card">
          <template #header>
            <h3>新增跟进</h3>
          </template>
          <el-form label-position="top" data-testid="lead-followup-form">
            <el-form-item label="跟进内容">
              <el-input
                v-model.trim="followForm.content"
                type="textarea"
                :rows="4"
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

        <el-card shadow="never" class="remark-card">
          <template #header>
            <h3>备注信息</h3>
          </template>
          <p class="remark-text">
            客户关注结算周期与人员稳定性，要求提供三个月排班稳定方案及保障条款。
          </p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { leadStatusTagMap, leadStatusTextMap, type LeadStatusCode } from '../../constants/status'

type FollowUpItem = {
  id: number
  operator: string
  content: string
  time: string
}

type ContactItem = {
  name: string
  position: string
  mobile: string
  email: string
}

const route = useRoute()
const router = useRouter()
const leadId = String(route.params.leadId ?? 'LEAD-001')

const lead = reactive({
  leadCode: leadId,
  projectName: '上海浦东希尔顿酒店派遣项目',
  bizOwner: '王磊',
  status: 'FOLLOWING' as LeadStatusCode,
  updatedAt: '2026-02-27 09:10',
})

const contacts = ref<ContactItem[]>([
  { name: '张芳', position: '人力行政总监', mobile: '138****8890', email: 'hr@hilton.cn' },
  { name: '刘波', position: '酒店总经理', mobile: '139****5201', email: 'gm@hilton.cn' },
])

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

function getLeadStatusText(status: LeadStatusCode) {
  return leadStatusTextMap[status] ?? '未知状态'
}

function getLeadStatusTagType(status: LeadStatusCode) {
  return leadStatusTagMap[status] ?? 'info'
}

function goBack() {
  if (window.history.length > 1) {
    router.back()
    return
  }
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
  padding: 20px;
  background: #f3f5f9;
}

.page-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.page-back {
  margin-bottom: 10px;
}

.head-block {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 14px;
}

.head-block h1 {
  margin: 0;
  font-size: 30px;
  color: #1f2937;
}

.head-block p {
  margin: 8px 0 0;
  color: #64748b;
}

.base-card,
.contact-card,
.timeline-card,
.followup-card,
.remark-card {
  border-radius: 12px;
  border: 1px solid #edf0f6;
}

.base-card :deep(.el-card__header),
.contact-card :deep(.el-card__header),
.timeline-card :deep(.el-card__header),
.followup-card :deep(.el-card__header),
.remark-card :deep(.el-card__header) {
  padding: 14px 18px;
}

.base-card h3,
.contact-card h3,
.timeline-card h3,
.followup-card h3,
.remark-card h3 {
  margin: 0;
  font-size: 17px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.info-grid label {
  color: #9ca3af;
  font-size: 12px;
}

.info-grid p {
  margin: 8px 0 0;
  font-size: 14px;
  color: #1f2937;
}

.detail-row {
  margin-top: 14px;
}

.timeline-card,
.remark-card {
  margin-top: 14px;
}

.card-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.error-tip {
  margin-bottom: 12px;
}

.remark-text {
  margin: 0;
  color: #6b7280;
  line-height: 1.7;
}

@media (max-width: 1180px) {
  .info-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 720px) {
  .lead-detail-page {
    padding: 12px;
  }

  .head-block {
    flex-direction: column;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
