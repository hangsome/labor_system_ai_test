import type * as T from './type'
import http from '@/utils/http'

export type * from './type'

const BASE_URL = '/labor/workforce/assignment'

export function listAssignment(query: T.AssignmentPageQuery) {
  return http.get<PageRes<T.AssignmentResp[]>>(`${BASE_URL}`, query)
}

export function getAssignment(id: string | number) {
  return http.get<T.AssignmentResp>(`${BASE_URL}/${id}`)
}

export function addAssignment(data: T.AssignmentReq) {
  return http.post(`${BASE_URL}`, data)
}

export function updateAssignment(data: T.AssignmentReq, id: string | number) {
  return http.put(`${BASE_URL}/${id}`, data)
}

export function deleteAssignment(id: string | number) {
  return http.del(`${BASE_URL}`, { ids: [id] })
}
