<template>
  <div class="dashboard-page" data-testid="dashboard-shell">
    <section class="welcome-section">
      <h1>工作台</h1>
      <p>欢迎回来，张明！以下是您的工作概览</p>
    </section>

    <section class="kpi-grid">
      <el-card shadow="never" class="kpi-card" data-testid="dashboard-card">
        <div class="kpi-head">
          <span>总客户数</span>
          <i class="kpi-dot kpi-dot-blue" />
        </div>
        <h2>24</h2>
        <p class="kpi-trend positive">↑ 12.5% 较上月</p>
      </el-card>

      <el-card shadow="never" class="kpi-card" data-testid="dashboard-card">
        <div class="kpi-head">
          <span>活跃员工数</span>
          <i class="kpi-dot kpi-dot-green" />
        </div>
        <h2>156</h2>
        <p class="kpi-trend positive">↑ 8.3% 较上月</p>
      </el-card>

      <el-card shadow="never" class="kpi-card" data-testid="dashboard-card">
        <div class="kpi-head">
          <span>本月合同金额</span>
          <i class="kpi-dot kpi-dot-purple" />
        </div>
        <h2>¥286,500</h2>
        <p class="kpi-trend positive">↑ 15.2% 较上月</p>
      </el-card>

      <el-card shadow="never" class="kpi-card" data-testid="dashboard-card">
        <div class="kpi-head">
          <span>待处理任务</span>
          <i class="kpi-dot kpi-dot-orange" />
        </div>
        <h2>8</h2>
        <p class="kpi-trend negative">↓ 3 较昨日</p>
      </el-card>
    </section>

    <section class="chart-grid">
      <el-card shadow="never" class="trend-card">
        <template #header>
          <div class="card-title-row">
            <h3>收入趋势</h3>
            <div class="time-tabs">
              <button
                v-for="item in trendTabs"
                :key="item.value"
                class="time-tab"
                :class="{ active: activeTrendTab === item.value }"
                @click="switchTrendTab(item.value)"
              >
                {{ item.label }}
              </button>
            </div>
          </div>
        </template>
        <svg viewBox="0 0 620 240" class="trend-svg" aria-hidden="true">
          <g stroke="#e5e7eb" stroke-width="1">
            <line x1="40" y1="190" x2="580" y2="190" />
            <line x1="40" y1="150" x2="580" y2="150" />
            <line x1="40" y1="110" x2="580" y2="110" />
            <line x1="40" y1="70" x2="580" y2="70" />
            <line x1="40" y1="30" x2="580" y2="30" />
          </g>
          <polygon
            :points="currentTrend.areaPoints"
            fill="rgba(59,130,246,0.2)"
          />
          <polyline
            :points="currentTrend.linePoints"
            fill="none"
            stroke="#3b82f6"
            stroke-width="3"
            stroke-linecap="round"
          />
        </svg>
      </el-card>

      <el-card shadow="never" class="donut-card">
        <template #header>
          <h3>客户类型分布</h3>
        </template>
        <div class="donut-chart" />
        <ul class="donut-legend">
          <li><i class="legend-dot legend-blue" />五星级酒店 (45%)</li>
          <li><i class="legend-dot legend-green" />连锁酒店 (30%)</li>
          <li><i class="legend-dot legend-yellow" />单体酒店 (15%)</li>
          <li><i class="legend-dot legend-red" />其他 (10%)</li>
        </ul>
      </el-card>
    </section>

    <section class="task-grid">
      <el-card shadow="never" class="todo-card">
        <template #header>
          <div class="card-title-row">
            <h3>待处理任务</h3>
            <el-button type="primary" size="small" @click="addTask">新建任务</el-button>
          </div>
        </template>
        <ul class="todo-list">
          <li v-for="item in tasks" :key="item.id">
            <span class="todo-status" :class="item.levelClass">{{ item.levelText }}</span>
            {{ item.content }}
          </li>
        </ul>
      </el-card>

      <el-card shadow="never" class="recent-card">
        <template #header>
          <div class="card-title-row">
            <h3>最近客户</h3>
            <router-link class="view-all" to="/crm/leads">查看全部</router-link>
          </div>
        </template>
        <ul class="customer-list">
          <li>
            <span class="customer-avatar">希</span>
            <div>
              <strong>希尔顿酒店</strong>
              <p>五星级酒店 | A 级客户</p>
            </div>
            <em class="badge success">已合作</em>
          </li>
          <li>
            <span class="customer-avatar">万</span>
            <div>
              <strong>万豪酒店</strong>
              <p>五星级酒店 | A 级客户</p>
            </div>
            <em class="badge progress">跟进中</em>
          </li>
          <li>
            <span class="customer-avatar">洲</span>
            <div>
              <strong>洲际酒店</strong>
              <p>五星级酒店 | A 级客户</p>
            </div>
            <em class="badge success">已合作</em>
          </li>
          <li>
            <span class="customer-avatar">如</span>
            <div>
              <strong>如家快捷酒店</strong>
              <p>连锁酒店 | B 级客户</p>
            </div>
            <em class="badge success">已合作</em>
          </li>
        </ul>
      </el-card>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'

type TrendTab = 'week' | 'month' | 'quarter' | 'year'

type TaskRow = {
  id: number
  content: string
  levelText: string
  levelClass: 'urgent' | 'medium' | 'low'
}

const trendTabs: Array<{ label: string; value: TrendTab }> = [
  { label: '周', value: 'week' },
  { label: '月', value: 'month' },
  { label: '季', value: 'quarter' },
  { label: '年', value: 'year' },
]

const trendMap: Record<TrendTab, { linePoints: string; areaPoints: string }> = {
  week: {
    linePoints: '60,182 140,176 220,168 300,160 380,150 460,146 540,142',
    areaPoints: '60,182 140,176 220,168 300,160 380,150 460,146 540,142 540,190 60,190',
  },
  month: {
    linePoints: '60,170 140,148 220,125 300,102 380,86 460,72 540,68',
    areaPoints: '60,170 140,148 220,125 300,102 380,86 460,72 540,68 540,190 60,190',
  },
  quarter: {
    linePoints: '60,180 140,162 220,150 300,138 380,124 460,116 540,104',
    areaPoints: '60,180 140,162 220,150 300,138 380,124 460,116 540,104 540,190 60,190',
  },
  year: {
    linePoints: '60,188 140,184 220,174 300,164 380,156 460,142 540,126',
    areaPoints: '60,188 140,184 220,174 300,164 380,156 460,142 540,126 540,190 60,190',
  },
}

const activeTrendTab = ref<TrendTab>('month')
const tasks = ref<TaskRow[]>([
  { id: 1, levelText: '高优先级', levelClass: 'urgent', content: '完成与希尔顿酒店的合同签署' },
  { id: 2, levelText: '中优先级', levelClass: 'medium', content: '审核 5 月薪资表' },
  { id: 3, levelText: '普通', levelClass: 'low', content: '安排新员工入职培训' },
  { id: 4, levelText: '高优先级', levelClass: 'urgent', content: '跟进万豪酒店招标项目' },
])

const currentTrend = computed(() => trendMap[activeTrendTab.value])

function switchTrendTab(tab: TrendTab) {
  activeTrendTab.value = tab
}

function addTask() {
  const id = Date.now()
  tasks.value.unshift({
    id,
    levelText: '普通',
    levelClass: 'low',
    content: `新增任务 #${String(id).slice(-4)}（待补充详情）`,
  })
  ElMessage.success('已新增待处理任务')
}
</script>

<style scoped>
.dashboard-page {
  padding: 22px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.welcome-section h1 {
  margin: 0;
  font-size: 34px;
  line-height: 1.2;
}

.welcome-section p {
  margin: 8px 0 0;
  color: #6b7280;
  font-size: 14px;
}

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.kpi-card { border-radius: 14px; border: 1px solid #edf0f6; }

.kpi-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #6b7280;
  font-size: 13px;
}

.kpi-dot { width: 20px; height: 20px; border-radius: 50%; }
.kpi-dot-blue { background: #dbeafe; }
.kpi-dot-green { background: #dcfce7; }
.kpi-dot-purple { background: #ede9fe; }
.kpi-dot-orange { background: #fef3c7; }

.kpi-card h2 { margin: 14px 0 9px; font-size: 38px; line-height: 1; }
.kpi-trend { margin: 0; font-size: 13px; font-weight: 600; }
.kpi-trend.positive { color: #22c55e; }
.kpi-trend.negative { color: #ef4444; }

.chart-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 14px;
}

.trend-card, .donut-card, .todo-card, .recent-card {
  border-radius: 14px;
  border: 1px solid #edf0f6;
}

.card-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.card-title-row h3 { margin: 0; font-size: 16px; }

.time-tabs {
  display: inline-flex;
  border-radius: 8px;
  padding: 2px;
  background: #f3f4f6;
}

.time-tab {
  border: 0;
  background: transparent;
  color: #6b7280;
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 6px;
  cursor: pointer;
}

.time-tab.active { color: #1f2937; background: #fff; }

.trend-svg { width: 100%; height: 250px; display: block; }

.donut-chart {
  width: 170px;
  height: 170px;
  margin: 8px auto 16px;
  border-radius: 50%;
  background: conic-gradient(#2563eb 0 45%, #22c55e 45% 75%, #f59e0b 75% 90%, #ef4444 90% 100%);
  position: relative;
}

.donut-chart::after {
  content: '';
  position: absolute;
  inset: 34px;
  border-radius: 50%;
  background: #fff;
}

.donut-legend {
  margin: 0; padding: 0; list-style: none;
  display: flex; flex-direction: column; gap: 10px;
  color: #374151; font-size: 13px;
}

.legend-dot {
  width: 8px; height: 8px; border-radius: 50%;
  display: inline-block; margin-right: 6px;
}

.legend-blue { background: #2563eb; }
.legend-green { background: #22c55e; }
.legend-yellow { background: #f59e0b; }
.legend-red { background: #ef4444; }

.task-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 14px;
}

.todo-list, .customer-list { margin: 0; padding: 0; list-style: none; }

.todo-list li {
  display: flex; align-items: center; gap: 10px;
  padding: 11px 0; border-bottom: 1px solid #f3f4f6; color: #111827;
}

.todo-status {
  font-size: 11px; font-weight: 600;
  padding: 3px 8px; border-radius: 999px;
}

.todo-status.urgent { color: #b91c1c; background: #fee2e2; }
.todo-status.medium { color: #92400e; background: #fef3c7; }
.todo-status.low { color: #1e40af; background: #dbeafe; }

.view-all { font-size: 13px; color: #2563eb; text-decoration: none; }

.customer-list li {
  display: grid;
  grid-template-columns: 34px 1fr auto;
  align-items: center; gap: 10px;
  padding: 10px 0; border-bottom: 1px solid #f3f4f6;
}

.customer-avatar {
  width: 34px; height: 34px; border-radius: 10px;
  background: linear-gradient(135deg, #bfdbfe, #dbeafe);
  color: #1e3a8a; font-size: 14px; font-weight: 700;
  display: inline-flex; align-items: center; justify-content: center;
}

.customer-list strong { display: block; color: #1f2937; font-size: 14px; }
.customer-list p { margin: 4px 0 0; color: #6b7280; font-size: 12px; }

.badge {
  font-style: normal; font-size: 12px;
  padding: 4px 8px; border-radius: 999px; font-weight: 600;
}

.badge.success { color: #15803d; background: #dcfce7; }
.badge.progress { color: #1d4ed8; background: #dbeafe; }

@media (max-width: 1260px) {
  .kpi-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .chart-grid, .task-grid { grid-template-columns: 1fr; }
}

@media (max-width: 640px) {
  .dashboard-page { padding: 14px; }
  .welcome-section h1 { font-size: 28px; }
  .kpi-grid { grid-template-columns: 1fr; }
  .kpi-card h2 { font-size: 30px; }
}
</style>
