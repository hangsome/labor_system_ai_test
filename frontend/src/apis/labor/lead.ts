import type * as T from './type'
import http from '@/utils/http'

export type * from './type'

const BASE_URL = '/labor/lead'

export function listLead(query: T.LeadPageQuery) {
  return http.get<PageRes<T.LeadResp[]>>(`${BASE_URL}`, query)
}

export function getLead(id: string | number) {
  return http.get<T.LeadResp>(`${BASE_URL}/${id}`)
}

export function addLead(data: T.LeadReq) {
  return http.post(`${BASE_URL}`, data)
}

export function updateLead(data: T.LeadReq, id: string | number) {
  return http.put(`${BASE_URL}/${id}`, data)
}

export function deleteLead(id: string | number) {
  return http.del(`${BASE_URL}`, { ids: [id] })
}

export function batchDeleteLead(ids: Array<string | number>) {
  return http.del(`${BASE_URL}`, { ids })
}

export function transitionLeadStatus(id: string | number, data: T.LeadStatusTransitionReq) {
  return http.put<T.LeadResp>(`${BASE_URL}/${id}/status`, data)
}

export function createLeadFollowUp(id: string | number, data: T.LeadFollowUpReq) {
  return http.post(`${BASE_URL}/${id}/follow-up`, data)
}

export function listLeadFollowUp(id: string | number) {
  return http.get<T.LeadFollowUpResp[]>(`${BASE_URL}/${id}/follow-up`)
}
