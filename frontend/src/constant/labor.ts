const buildOptions = (labelMap: Record<string, string>) => {
  return Object.entries(labelMap).map(([value, label]) => ({ label, value }))
}

export const LEAD_STATUS_LABEL_MAP: Record<string, string> = {
  NEW: '新建',
  FOLLOWING: '跟进中',
  WON: '已成交',
  LOST: '已流失',
}

export const LEAD_STATUS_OPTIONS = buildOptions(LEAD_STATUS_LABEL_MAP)
export const LEAD_TRANSITION_STATUS_OPTIONS = ['FOLLOWING', 'WON', 'LOST']
export const LEAD_STATUS_COLOR_MAP: Record<string, string> = {
  NEW: 'arcoblue',
  FOLLOWING: 'orangered',
  WON: 'green',
  LOST: 'gray',
}

export const DEPOSIT_STATUS_LABEL_MAP: Record<string, string> = {
  UNPAID: '未缴纳',
  PAID: '已缴纳',
  REFUND: '已退款',
  UNKNOWN: '未知',
}
export const DEPOSIT_STATUS_OPTIONS = buildOptions(DEPOSIT_STATUS_LABEL_MAP)

export const LEAD_FOLLOW_UP_ACTION_LABEL_MAP: Record<string, string> = {
  FOLLOW_UP: '跟进记录',
  STATUS_TRANSITION: '状态流转',
}

export const EMPLOYER_LEVEL_LABEL_MAP: Record<string, string> = {
  A: 'A级',
  B: 'B级',
  C: 'C级',
  INACTIVE: '已停用',
}
export const EMPLOYER_LEVEL_OPTIONS = buildOptions(EMPLOYER_LEVEL_LABEL_MAP)

export const CONTRACT_STATUS_LABEL_MAP: Record<string, string> = {
  DRAFT: '草稿',
  SIGNED: '已签署',
  TERMINATED: '已终止',
}
export const CONTRACT_STATUS_OPTIONS = buildOptions(CONTRACT_STATUS_LABEL_MAP)
export const CONTRACT_STATUS_COLOR_MAP: Record<string, string> = {
  DRAFT: 'arcoblue',
  SIGNED: 'green',
  TERMINATED: 'gray',
}

export const CONTRACT_TYPE_LABEL_MAP: Record<string, string> = {
  A: '甲类',
  B: '乙类',
}
export const CONTRACT_TYPE_OPTIONS = buildOptions(CONTRACT_TYPE_LABEL_MAP)

export const SETTLEMENT_CYCLE_LABEL_MAP: Record<string, string> = {
  WEEKLY: '周结',
  MONTHLY: '月结',
  QUARTERLY: '季结',
}
export const SETTLEMENT_CYCLE_OPTIONS = buildOptions(SETTLEMENT_CYCLE_LABEL_MAP)

export const SETTLEMENT_RULE_TYPE_LABEL_MAP: Record<string, string> = {
  RATE: '费率规则',
  FIXED: '固定金额',
}
export const SETTLEMENT_RULE_TYPE_OPTIONS = buildOptions(SETTLEMENT_RULE_TYPE_LABEL_MAP)

export const SETTLEMENT_STATUS_LABEL_MAP: Record<string, string> = {
  DRAFT: '草稿',
  PUBLISHED: '已发布',
  DISABLED: '已停用',
}
export const SETTLEMENT_STATUS_OPTIONS = buildOptions(SETTLEMENT_STATUS_LABEL_MAP)
export const SETTLEMENT_STATUS_COLOR_MAP: Record<string, string> = {
  DRAFT: 'arcoblue',
  PUBLISHED: 'green',
  DISABLED: 'gray',
}

export const toLaborLabel = (labelMap: Record<string, string>, value?: string | null) => {
  if (!value) return '-'
  return labelMap[value] || value
}
