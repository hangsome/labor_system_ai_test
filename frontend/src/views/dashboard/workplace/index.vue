<template>
  <div class="gi_page labor-dashboard">
    <section class="dashboard-header">
      <h2>工作台</h2>
      <p>欢迎回来，{{ greetingName }}！以下是您的工作概览</p>
    </section>

    <section class="metric-grid">
      <a-card v-for="item in metricCards" :key="item.title" class="metric-card" :bordered="false">
        <div class="metric-head">
          <span class="metric-title">{{ item.title }}</span>
          <span class="metric-dot" :style="{ backgroundColor: item.dotColor }"></span>
        </div>
        <div class="metric-value">{{ item.value }}</div>
        <div class="metric-trend" :class="item.trendClass">
          <icon-arrow-rise v-if="item.trendClass === 'up'" />
          <icon-arrow-fall v-else />
          <span>{{ item.trend }}</span>
        </div>
      </a-card>
    </section>

    <section class="chart-grid">
      <a-card class="panel-card trend-card" :bordered="false" :loading="loading">
        <template #title>
          <span class="panel-title">收入趋势</span>
        </template>
        <template #extra>
          <a-radio-group v-model="trendRange" type="button" size="small">
            <a-radio value="week">周</a-radio>
            <a-radio value="month">月</a-radio>
            <a-radio value="season">季</a-radio>
            <a-radio value="year">年</a-radio>
          </a-radio-group>
        </template>
        <Chart :option="trendChartOption" style="height: 290px" />
      </a-card>

      <a-card class="panel-card pie-card" :bordered="false" :loading="loading">
        <template #title>
          <span class="panel-title">客户类型分布</span>
        </template>
        <Chart :option="customerPieOption" style="height: 290px" />
        <div class="pie-legend">
          <div v-for="item in customerTypeLegend" :key="item.name" class="legend-item">
            <span class="legend-dot" :style="{ backgroundColor: item.color }"></span>
            <span class="legend-name">{{ item.name }}</span>
            <span class="legend-value">({{ item.percent }}%)</span>
          </div>
        </div>
      </a-card>
    </section>

    <section class="bottom-grid">
      <a-card class="panel-card task-card" :bordered="false" :loading="loading">
        <template #title>
          <span class="panel-title">待处理任务</span>
        </template>
        <template #extra>
          <a-space>
            <a-button size="small">
              <template #icon>
                <icon-filter />
              </template>
              筛选
            </a-button>
            <a-button type="primary" size="small">
              <template #icon>
                <icon-plus />
              </template>
              新建任务
            </a-button>
          </a-space>
        </template>

        <div class="task-list">
          <div v-for="item in taskList" :key="item.key" class="task-item">
            <a-checkbox />
            <div class="task-main">
              <p class="task-title">{{ item.title }}</p>
              <p class="task-meta">
                <a-tag :color="item.tagColor" size="small">{{ item.priority }}</a-tag>
                <span>截止日期: {{ item.deadline }}</span>
                <span>{{ item.department }}</span>
              </p>
            </div>
            <icon-more-vertical class="task-more" />
          </div>
          <a-empty v-if="!taskList.length" description="暂无任务" />
          <a-link v-else class="all-task-link">查看全部任务</a-link>
        </div>
      </a-card>

      <a-card class="panel-card customer-card" :bordered="false" :loading="loading">
        <template #title>
          <span class="panel-title">最近客户</span>
        </template>
        <template #extra>
          <a-link>查看全部</a-link>
        </template>

        <div class="customer-list">
          <div v-for="item in recentCustomers" :key="item.id" class="customer-item">
            <a-avatar :size="38" class="customer-avatar">
              {{ item.name.slice(0, 1) }}
            </a-avatar>
            <div class="customer-main">
              <p class="customer-name">{{ item.name }}</p>
              <p class="customer-meta">{{ item.type }} | {{ item.level }}</p>
            </div>
            <a-tag :color="item.statusColor">{{ item.status }}</a-tag>
          </div>
          <a-empty v-if="!recentCustomers.length" description="暂无客户数据" />
        </div>

        <a-button class="add-customer-btn" long>
          <template #icon>
            <icon-plus />
          </template>
          添加新客户
        </a-button>
      </a-card>
    </section>
  </div>
</template>

<script setup lang="ts">
import type { EChartsOption } from 'echarts'
import dayjs from 'dayjs'
import { graphic } from 'echarts'
import { listContract, type ContractResp } from '@/apis/labor/contract'
import { listEmployer, type EmployerResp } from '@/apis/labor/employer'
import { listLead, type LeadResp } from '@/apis/labor/lead'
import { listSettlementRule, type SettlementRuleResp } from '@/apis/labor/settlement'
import {
  EMPLOYER_LEVEL_LABEL_MAP,
  toLaborLabel,
} from '@/constant/labor'
import { useChart } from '@/hooks'
import { useUserStore } from '@/stores'

defineOptions({ name: 'Workplace' })

interface TaskItem {
  key: string
  title: string
  priority: string
  tagColor: string
  deadline: string
  department: string
  sortTime: number
}

interface CustomerItem {
  id: string
  name: string
  type: string
  level: string
  status: string
  statusColor: string
}

const userStore = useUserStore()
const greetingName = computed(() => userStore.nickname || userStore.username || '管理员')
const trendRange = ref<'week' | 'month' | 'season' | 'year'>('month')
const loading = ref(false)

const leadList = ref<LeadResp[]>([])
const leadTotal = ref(0)
const employerList = ref<EmployerResp[]>([])
const employerTotal = ref(0)
const contractList = ref<ContractResp[]>([])
const settlementRuleList = ref<SettlementRuleResp[]>([])

const getDateOrNow = (dateStr?: string) => {
  const d = dateStr ? dayjs(dateStr) : dayjs()
  return d.isValid() ? d : dayjs()
}

const calcMonthCount = (data: { createTime?: string, updateTime?: string }[], monthOffset = 0) => {
  const target = dayjs().add(monthOffset, 'month')
  return data.filter((item) => {
    const checkDate = getDateOrNow(item.updateTime || item.createTime)
    return checkDate.year() === target.year() && checkDate.month() === target.month()
  }).length
}

const calcGrowthText = (current: number, prev: number) => {
  if (prev <= 0) {
    return current > 0 ? '新增' : '持平'
  }
  const rate = ((current - prev) / prev) * 100
  return `${Math.abs(rate).toFixed(1)}% 较上月`
}

const metricCards = computed(() => {
  const activeLeadCount = leadList.value.filter((item) => ['NEW', 'FOLLOWING'].includes(item.cooperationStatus)).length
  const signedContractCount = contractList.value.filter((item) => item.status === 'SIGNED').length
  const pendingTaskCount = taskList.value.length

  const currentCustomerMonth = calcMonthCount(employerList.value, 0)
  const prevCustomerMonth = calcMonthCount(employerList.value, -1)
  const customerTrendUp = currentCustomerMonth >= prevCustomerMonth

  const currentLeadMonth = calcMonthCount(leadList.value, 0)
  const prevLeadMonth = calcMonthCount(leadList.value, -1)
  const leadTrendUp = currentLeadMonth >= prevLeadMonth

  const currentContractMonth = calcMonthCount(contractList.value, 0)
  const prevContractMonth = calcMonthCount(contractList.value, -1)
  const contractTrendUp = currentContractMonth >= prevContractMonth

  return [
    {
      title: '总客户数',
      value: String(employerTotal.value),
      trend: calcGrowthText(currentCustomerMonth, prevCustomerMonth),
      trendClass: customerTrendUp ? 'up' : 'down',
      dotColor: '#dbeafe',
    },
    {
      title: '活跃线索数',
      value: String(activeLeadCount),
      trend: calcGrowthText(currentLeadMonth, prevLeadMonth),
      trendClass: leadTrendUp ? 'up' : 'down',
      dotColor: '#dcfce7',
    },
    {
      title: '已签合同数',
      value: String(signedContractCount),
      trend: calcGrowthText(currentContractMonth, prevContractMonth),
      trendClass: contractTrendUp ? 'up' : 'down',
      dotColor: '#f3e8ff',
    },
    {
      title: '待处理任务',
      value: String(pendingTaskCount),
      trend: `${pendingTaskCount} 项待处理`,
      trendClass: pendingTaskCount > 0 ? 'down' : 'up',
      dotColor: '#fef3c7',
    },
  ]
})

const taskList = computed<TaskItem[]>(() => {
  const leadTasks = leadList.value
    .filter((item) => ['NEW', 'FOLLOWING'].includes(item.cooperationStatus))
    .map((item) => {
      return {
        key: `lead-${item.id}`,
        title: `跟进线索「${item.projectName}」`,
        priority: item.cooperationStatus === 'FOLLOWING' ? '高优先级' : '中优先级',
        tagColor: item.cooperationStatus === 'FOLLOWING' ? 'orangered' : 'gold',
        deadline: item.tenderAt || '-',
        department: item.industryType || '业务部',
        sortTime: getDateOrNow(item.tenderAt).valueOf(),
      }
    })

  const settlementTasks = settlementRuleList.value
    .filter((item) => item.status === 'DRAFT')
    .map((item) => {
      return {
        key: `rule-${item.id}`,
        title: `发布结算规则 V${item.versionNo}（合同 ${item.contractId}）`,
        priority: '中优先级',
        tagColor: 'gold',
        deadline: item.effectiveFrom || '-',
        department: '结算管理',
        sortTime: getDateOrNow(item.effectiveFrom).valueOf(),
      }
    })

  return [...leadTasks, ...settlementTasks].sort((a, b) => a.sortTime - b.sortTime).slice(0, 5)
})

const recentCustomers = computed<CustomerItem[]>(() => {
  const leadMap = new Map(leadList.value.map((item) => [String(item.id), item]))
  const statusMap: Record<string, { label: string, color: string }> = {
    WON: { label: '已合作', color: 'green' },
    FOLLOWING: { label: '跟进中', color: 'arcoblue' },
    NEW: { label: '待跟进', color: 'gold' },
    LOST: { label: '已流失', color: 'gray' },
  }
  return [...employerList.value]
    .sort((a, b) => getDateOrNow(b.updateTime).valueOf() - getDateOrNow(a.updateTime).valueOf())
    .slice(0, 5)
    .map((item) => {
      const lead = item.leadId ? leadMap.get(String(item.leadId)) : undefined
      const leadStatus = lead?.cooperationStatus || 'NEW'
      const statusInfo = statusMap[leadStatus] || statusMap.NEW
      return {
        id: String(item.id),
        name: item.unitName || '-',
        type: toLaborLabel(EMPLOYER_LEVEL_LABEL_MAP, item.customerLevel),
        level: '客户',
        status: statusInfo.label,
        statusColor: statusInfo.color,
      }
    })
})

const customerTypeLegend = computed(() => {
  const levelCountMap = employerList.value.reduce<Record<string, number>>((acc, item) => {
    const key = item.customerLevel || 'UNKNOWN'
    acc[key] = (acc[key] || 0) + 1
    return acc
  }, {})
  const palette = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6']
  const total = Math.max(employerTotal.value, 1)
  return Object.entries(levelCountMap).map(([level, count], index) => {
    return {
      name: toLaborLabel(EMPLOYER_LEVEL_LABEL_MAP, level),
      value: count,
      percent: Number(((count / total) * 100).toFixed(0)),
      color: palette[index % palette.length],
    }
  })
})

const trendXAxis = ref<string[]>([])
const trendSeries = ref<number[]>([])

const rebuildTrendData = () => {
  const sourceList = [...contractList.value]
  const now = dayjs()
  const axis: string[] = []
  const data: number[] = []

  const pushByRange = (label: string, start: dayjs.Dayjs, end: dayjs.Dayjs) => {
    axis.push(label)
    data.push(sourceList.filter((item) => {
      const itemDate = getDateOrNow(item.startDate || item.createTime)
      return itemDate.isAfter(start.subtract(1, 'day')) && itemDate.isBefore(end.add(1, 'day'))
    }).length)
  }

  if (trendRange.value === 'week') {
    for (let i = 6; i >= 0; i -= 1) {
      const day = now.subtract(i, 'day')
      pushByRange(day.format('M/D'), day.startOf('day'), day.endOf('day'))
    }
  } else if (trendRange.value === 'season') {
    for (let i = 3; i >= 0; i -= 1) {
      const quarterBase = now.subtract(i * 3, 'month')
      const quarterMonth = Math.floor(quarterBase.month() / 3) * 3
      const quarterStart = quarterBase.month(quarterMonth).startOf('month')
      const quarterEnd = quarterStart.add(2, 'month').endOf('month')
      const quarter = Math.floor(quarterStart.month() / 3) + 1
      pushByRange(`${quarterStart.format('YYYY')}Q${quarter}`, quarterStart, quarterEnd)
    }
  } else if (trendRange.value === 'year') {
    for (let i = 4; i >= 0; i -= 1) {
      const start = now.subtract(i, 'year').startOf('year')
      const end = start.endOf('year')
      pushByRange(start.format('YYYY'), start, end)
    }
  } else {
    for (let i = 6; i >= 0; i -= 1) {
      const month = now.subtract(i, 'month')
      pushByRange(month.format('M月'), month.startOf('month'), month.endOf('month'))
    }
  }

  trendXAxis.value = axis
  trendSeries.value = data
}

watch([trendRange, contractList], rebuildTrendData, { immediate: true, deep: true })

const { chartOption: trendChartOption } = useChart((isDark: EChartsOption) => {
  return {
    grid: {
      left: 48,
      right: 16,
      top: 24,
      bottom: 30,
    },
    tooltip: {
      trigger: 'axis',
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: trendXAxis.value,
      axisLine: {
        lineStyle: {
          color: isDark ? '#454545' : '#d9dde3',
        },
      },
      axisLabel: {
        color: isDark ? '#C8CDD4' : '#6b7280',
      },
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        color: isDark ? '#C8CDD4' : '#6b7280',
      },
      splitLine: {
        lineStyle: {
          color: isDark ? '#3f3f3f' : '#edf0f4',
        },
      },
    },
    series: [
      {
        type: 'line',
        smooth: true,
        data: trendSeries.value,
        showSymbol: true,
        symbolSize: 8,
        color: '#3b82f6',
        areaStyle: {
          color: new graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(59, 130, 246, 0.25)' },
            { offset: 1, color: 'rgba(59, 130, 246, 0.03)' },
          ]),
        },
      },
    ],
  }
})

const { chartOption: customerPieOption } = useChart((isDark: EChartsOption) => {
  return {
    legend: { show: false },
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {d}%',
    },
    series: [
      {
        type: 'pie',
        radius: ['38%', '70%'],
        center: ['50%', '45%'],
        data: customerTypeLegend.value.map((item) => {
          return {
            name: item.name,
            value: item.value,
            itemStyle: {
              color: item.color,
              borderColor: isDark ? '#1f1f1f' : '#fff',
              borderWidth: 1,
            },
          }
        }),
        label: { show: false },
      },
    ],
  }
})

const loadDashboardData = async () => {
  loading.value = true
  try {
    const [leadRes, employerRes, contractRes, settlementRes] = await Promise.all([
      listLead({ page: 1, size: 500, sort: ['updateTime,desc'] }),
      listEmployer({ page: 1, size: 500, sort: ['updateTime,desc'] }),
      listContract({ page: 1, size: 500, sort: ['updateTime,desc'] }),
      listSettlementRule({ page: 1, size: 500, sort: ['createTime,desc'] }),
    ])

    leadList.value = leadRes.data.list || []
    leadTotal.value = Number(leadRes.data.total || leadList.value.length)

    employerList.value = employerRes.data.list || []
    employerTotal.value = Number(employerRes.data.total || employerList.value.length)

    contractList.value = contractRes.data.list || []
    settlementRuleList.value = settlementRes.data.list || []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadDashboardData()
})
</script>

<style scoped lang="scss">
.labor-dashboard {
  display: flex;
  flex-direction: column;
  gap: 14px;
  background: #f5f7fb;
}

.dashboard-header {
  h2 {
    margin: 0;
    font-size: 32px;
    color: #1f2937;
  }

  p {
    margin: 10px 0 0;
    color: #6b7280;
    font-size: 16px;
  }
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.metric-card {
  border-radius: 14px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.06);
}

.metric-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.metric-title {
  color: #6b7280;
  font-size: 16px;
}

.metric-dot {
  width: 34px;
  height: 34px;
  border-radius: 50%;
}

.metric-value {
  margin-top: 12px;
  color: #111827;
  font-size: 46px;
  font-weight: 700;
  line-height: 1.1;
}

.metric-trend {
  margin-top: 12px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
}

.metric-trend.up {
  color: #059669;
}

.metric-trend.down {
  color: #ef4444;
}

.panel-card {
  border-radius: 14px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.06);
}

.panel-title {
  font-size: 20px;
  font-weight: 600;
  color: #111827;
}

.chart-grid {
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(0, 1fr);
  gap: 14px;
}

.pie-legend {
  margin-top: 4px;
}

.legend-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  color: #4b5563;
  font-size: 14px;
}

.legend-dot {
  width: 10px;
  height: 10px;
  margin-right: 8px;
  border-radius: 50%;
}

.legend-name {
  margin-right: 4px;
}

.bottom-grid {
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(0, 1fr);
  gap: 14px;
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.task-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid #eef2f7;
}

.task-main {
  flex: 1;
}

.task-title {
  margin: 0 0 8px;
  color: #111827;
  font-size: 17px;
}

.task-meta {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
  color: #6b7280;
  font-size: 13px;
}

.task-more {
  color: #9ca3af;
  font-size: 18px;
}

.all-task-link {
  display: block;
  margin-top: 16px;
  text-align: center;
}

.customer-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.customer-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.customer-avatar {
  background: linear-gradient(135deg, #7dd3fc, #2563eb);
  color: #fff;
}

.customer-main {
  flex: 1;
  min-width: 0;
}

.customer-name {
  margin: 0;
  color: #111827;
  font-size: 15px;
  font-weight: 500;
}

.customer-meta {
  margin: 4px 0 0;
  color: #6b7280;
  font-size: 13px;
}

.add-customer-btn {
  margin-top: 14px;
}

@media (max-width: 1400px) {
  .metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 992px) {
  .chart-grid,
  .bottom-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .metric-grid {
    grid-template-columns: 1fr;
  }
}
</style>
