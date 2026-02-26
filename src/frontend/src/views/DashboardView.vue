<template>
  <div class="page-shell" data-testid="dashboard-shell">
    <el-header class="top-bar" data-testid="dashboard-header">
      <div class="title-group">
        <h1>劳务系统管理后台</h1>
        <p>Stage 0 初始化骨架已就绪</p>
      </div>
    </el-header>

    <el-main class="content">
      <el-row :gutter="16">
        <el-col :xs="24" :md="8">
          <el-card shadow="hover" data-testid="dashboard-card">
            <template #header>客户线索</template>
            <p>支持多维筛选、跟进记录、转化分析。</p>
          </el-card>
        </el-col>
        <el-col :xs="24" :md="8">
          <el-card shadow="hover" data-testid="dashboard-card">
            <template #header>合同结算</template>
            <p>支持计时/按天/计件/按月等结算模型。</p>
          </el-card>
        </el-col>
        <el-col :xs="24" :md="8">
          <el-card shadow="hover" data-testid="dashboard-card">
            <template #header>资金台账</template>
            <p>应收、开票、核销、支付批次统一管理。</p>
          </el-card>
        </el-col>
      </el-row>
    </el-main>
  </div>
</template>

<style scoped>
.page-shell {
  min-height: 100vh;
  padding: 24px;
}

.top-bar {
  background: #fff;
  border-radius: 12px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  padding: 20px 24px;
}

.title-group h1 {
  margin: 0;
  font-size: 24px;
  color: #1a365d;
}

.title-group p {
  margin: 6px 0 0;
  color: #64748b;
}

.content {
  padding: 0;
}

.el-col {
  margin-bottom: 16px;
}
</style>
