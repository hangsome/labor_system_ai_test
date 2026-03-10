import type * as T from './type'
import http from '@/utils/http'

export type * from './type'

const BASE_URL = '/labor/attendance/shift'

export function listShift(query: T.ShiftPageQuery) {
  return http.get<PageRes<T.ShiftResp[]>>(`${BASE_URL}`, query)
}

export function getShift(id: string | number) {
  return http.get<T.ShiftResp>(`${BASE_URL}/${id}`)
}

export function addShift(data: T.ShiftReq) {
  return http.post(`${BASE_URL}`, data)
}

export function updateShift(data: T.ShiftReq, id: string | number) {
  return http.put(`${BASE_URL}/${id}`, data)
}

export function deleteShift(id: string | number) {
  return http.del(`${BASE_URL}`, { ids: [id] })
}
