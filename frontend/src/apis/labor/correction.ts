import type * as T from './type'
import http from '@/utils/http'

export type * from './type'

const BASE_URL = '/labor/attendance/correction'

export function listCorrection(query: T.CorrectionPageQuery) {
  return http.get<PageRes<T.CorrectionResp[]>>(`${BASE_URL}`, query)
}

export function getCorrection(id: string | number) {
  return http.get<T.CorrectionResp>(`${BASE_URL}/${id}`)
}

export function addCorrection(data: T.CorrectionReq) {
  return http.post(`${BASE_URL}`, data)
}

export function approveCorrection(id: string | number) {
  return http.put<T.CorrectionResp>(`${BASE_URL}/${id}/approve`)
}

export function rejectCorrection(id: string | number) {
  return http.put<T.CorrectionResp>(`${BASE_URL}/${id}/reject`)
}
