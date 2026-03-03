export type CommonStatusTag = 'success' | 'warning' | 'info' | 'danger' | 'primary'

export type LeadStatusCode = 'NEW' | 'FOLLOWING' | 'WON' | 'LOST'
export const leadStatusTextMap: Record<LeadStatusCode, string> = {
  NEW: '新线索',
  FOLLOWING: '跟进中',
  WON: '已转化',
  LOST: '已流失',
}
export const leadStatusTagMap: Record<LeadStatusCode, CommonStatusTag> = {
  NEW: 'info',
  FOLLOWING: 'warning',
  WON: 'success',
  LOST: 'danger',
}

export type ContractStatusCode = 'DRAFT' | 'ACTIVE' | 'ARCHIVED' | 'EXPIRING' | 'TERMINATED'
export const contractStatusTextMap: Record<ContractStatusCode, string> = {
  DRAFT: '草稿',
  ACTIVE: '进行中',
  ARCHIVED: '已归档',
  EXPIRING: '即将到期',
  TERMINATED: '已终止',
}
export const contractStatusTagMap: Record<ContractStatusCode, CommonStatusTag> = {
  DRAFT: 'info',
  ACTIVE: 'success',
  ARCHIVED: 'info',
  EXPIRING: 'warning',
  TERMINATED: 'danger',
}

export const ruleStatusTextMap: Record<'ACTIVE' | 'DRAFT' | 'INACTIVE', string> = {
  ACTIVE: '生效中',
  DRAFT: '草稿',
  INACTIVE: '已停用',
}

export const auditResultTextMap: Record<'SUCCESS' | 'FAILED', string> = {
  SUCCESS: '成功',
  FAILED: '失败',
}
export const auditResultTagMap: Record<'SUCCESS' | 'FAILED', CommonStatusTag> = {
  SUCCESS: 'success',
  FAILED: 'danger',
}

export const scopeTextMap: Record<'ALL' | 'DEPT' | 'PROJECT' | 'CLIENT' | 'SELF', string> = {
  ALL: '全部数据',
  DEPT: '部门数据',
  PROJECT: '项目数据',
  CLIENT: '客户数据',
  SELF: '仅本人',
}

export function withFallback(text: string | undefined | null): string {
  if (!text) {
    return '—'
  }
  return text
}
