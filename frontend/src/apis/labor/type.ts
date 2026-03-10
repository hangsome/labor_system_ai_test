export interface LeadResp {
  id: string
  leadCode: string
  projectName: string
  contactName?: string
  contactPhone?: string
  industryType: string
  bizOwnerId: number
  cooperationStatus: string
  tenderAt?: string
  depositStatus: string
  createUserString?: string
  createTime?: string
  updateUserString?: string
  updateTime?: string
}

export interface LeadQuery {
  description?: string
  cooperationStatus?: string
  sort: Array<string>
}

export interface LeadPageQuery extends LeadQuery, PageQuery {}

export interface LeadReq {
  leadCode: string
  projectName: string
  contactName?: string
  contactPhone?: string
  industryType: string
  bizOwnerId: number
  cooperationStatus: string
  tenderAt?: string
  depositStatus: string
}

export interface LeadStatusTransitionReq {
  toStatus: string
  comment?: string
}

export interface LeadFollowUpReq {
  content: string
  nextContactAt?: string
}

export interface LeadFollowUpResp {
  id: string
  leadId: string
  action: string
  content?: string
  status?: string
  statusFrom?: string
  statusTo?: string
  nextContactAt?: string
  operatorId: string
  createTime?: string
}

export interface EmployerResp {
  id: string
  unitCode: string
  leadId?: string
  unitName: string
  customerLevel: string
  address?: string
  invoiceInfo?: string
  isOutsource: boolean
  createUserString?: string
  createTime?: string
  updateUserString?: string
  updateTime?: string
}

export interface EmployerQuery {
  description?: string
  customerLevel?: string
  sort: Array<string>
}

export interface EmployerPageQuery extends EmployerQuery, PageQuery {}

export interface EmployerReq {
  unitCode: string
  leadId?: number
  unitName: string
  customerLevel: string
  address?: string
  invoiceInfo?: string
  isOutsource: boolean
}

export interface ContractResp {
  id: string
  contractNo: string
  employerUnitId: string
  contractName: string
  contractType: string
  startDate: string
  endDate: string
  settlementCycle: string
  status: string
  taxRate: string
  createUserString?: string
  createTime?: string
  updateUserString?: string
  updateTime?: string
}

export interface ContractQuery {
  description?: string
  status?: string
  employerUnitId?: number
  sort: Array<string>
}

export interface ContractPageQuery extends ContractQuery, PageQuery {}

export interface ContractReq {
  contractNo: string
  employerUnitId: number
  contractName: string
  contractType: string
  startDate: string
  endDate: string
  settlementCycle: string
  taxRate: number | string
}

export interface ContractRenewReq {
  newEndDate: string
}

export interface ContractTerminateReq {
  terminateDate: string
  reason?: string
}

export interface SettlementRuleResp {
  id: string
  contractId: string
  ruleType: string
  versionNo: number
  effectiveFrom: string
  rulePayload: string
  status: string
  publishedAt?: string
  deactivatedAt?: string
  createTime?: string
}

export interface SettlementRuleQuery {
  contractId?: number
  status?: string
  sort: Array<string>
}

export interface SettlementRulePageQuery extends SettlementRuleQuery, PageQuery {}

export interface SettlementRuleReq {
  contractId: number
  ruleType: string
  versionNo: number
  effectiveFrom: string
  rulePayload: string
}

export interface EmployeeResp {
  id: string
  employeeNo: string
  name: string
  idNo?: string
  phone?: string
  deptId?: string
  status: string
  hiredAt: string
  offboardAt?: string
  accountName?: string
  bankName?: string
  bankNo?: string
  createUserString?: string
  createTime?: string
  updateUserString?: string
  updateTime?: string
}

export interface EmployeeQuery {
  description?: string
  status?: string
  deptId?: number
  sort: Array<string>
}

export interface EmployeePageQuery extends EmployeeQuery, PageQuery {}

export interface EmployeeReq {
  employeeNo: string
  name: string
  idNo?: string
  phone?: string
  deptId?: number
  hiredAt: string
  accountName: string
  bankName: string
  bankNo: string
}

export interface EmployeeOffboardReq {
  offboardAt: string
  reason?: string
}

export interface AssignmentResp {
  id: string
  contractId: string
  employeeId: string
  positionName?: string
  levelName?: string
  assignedAt: string
  unassignedAt?: string
  status: string
  createUserString?: string
  createTime?: string
  updateUserString?: string
  updateTime?: string
}

export interface AssignmentQuery {
  description?: string
  contractId?: number
  employeeId?: number
  status?: string
  sort: Array<string>
}

export interface AssignmentPageQuery extends AssignmentQuery, PageQuery {}

export interface AssignmentReq {
  contractId: number
  employeeId: number
  positionName?: string
  levelName?: string
  assignedAt: string
  unassignedAt?: string
}

export interface ShiftResp {
  id: string
  contractId: string
  shiftName: string
  startTime: string
  endTime: string
  createUserString?: string
  createTime?: string
  updateUserString?: string
  updateTime?: string
}

export interface ShiftQuery {
  shiftName?: string
  contractId?: number
  sort: Array<string>
}

export interface ShiftPageQuery extends ShiftQuery, PageQuery {}

export interface ShiftReq {
  contractId: number
  shiftName: string
  startTime: string
  endTime: string
}

export interface AttendanceResp {
  id: string
  contractId: string
  employeeId: string
  workDate: string
  shiftId: string
  checkInAt?: string
  checkOutAt?: string
  workMinutes: number
  overtimeMinutes: number
  status: string
  createUserString?: string
  createTime?: string
  updateUserString?: string
  updateTime?: string
}

export interface AttendanceQuery {
  contractId?: number
  employeeId?: number
  shiftId?: number
  workDate?: string
  status?: string
  sort: Array<string>
}

export interface AttendancePageQuery extends AttendanceQuery, PageQuery {}

export interface AttendanceReq {
  contractId: number
  employeeId: number
  workDate: string
  shiftId: number
  checkInAt?: string
  checkOutAt?: string
}

export interface CorrectionResp {
  id: string
  attendanceId: string
  employeeId: string
  reason: string
  status: string
  reviewedBy?: string
  reviewedAt?: string
  createUserString?: string
  createTime?: string
  updateUserString?: string
  updateTime?: string
}

export interface CorrectionQuery {
  attendanceId?: number
  employeeId?: number
  status?: string
  sort: Array<string>
}

export interface CorrectionPageQuery extends CorrectionQuery, PageQuery {}

export interface CorrectionReq {
  attendanceId: number
  employeeId: number
  reason: string
}
