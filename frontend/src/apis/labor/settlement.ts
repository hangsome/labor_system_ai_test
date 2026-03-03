import type * as T from './type'
import http from '@/utils/http'

export type * from './type'

const BASE_URL = '/labor/settlement'

export function listSettlementRule(query: T.SettlementRulePageQuery) {
  return http.get<PageRes<T.SettlementRuleResp[]>>(`${BASE_URL}`, query)
}

export function getSettlementRule(id: string | number) {
  return http.get<T.SettlementRuleResp>(`${BASE_URL}/${id}`)
}

export function addSettlementRule(data: T.SettlementRuleReq) {
  return http.post(`${BASE_URL}`, data)
}

export function updateSettlementRule(data: T.SettlementRuleReq, id: string | number) {
  return http.put(`${BASE_URL}/${id}`, data)
}

export function deleteSettlementRule(id: string | number) {
  return http.del(`${BASE_URL}`, { ids: [id] })
}

export function publishSettlementRule(id: string | number) {
  return http.put<T.SettlementRuleResp>(`${BASE_URL}/${id}/publish`)
}

export function deactivateSettlementRule(id: string | number) {
  return http.put<T.SettlementRuleResp>(`${BASE_URL}/${id}/deactivate`)
}

export function listSettlementRuleVersion(contractId: string | number) {
  return http.get<T.SettlementRuleResp[]>(`${BASE_URL}/version/${contractId}`)
}

export function getActiveSettlementRule(contractId: string | number, onDate?: string) {
  return http.get<T.SettlementRuleResp>(`${BASE_URL}/active/${contractId}`, { onDate })
}
