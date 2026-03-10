import type * as T from './type'
import http from '@/utils/http'

export type * from './type'

const BASE_URL = '/labor/attendance/record'

export function listAttendance(query: T.AttendancePageQuery) {
  return http.get<PageRes<T.AttendanceResp[]>>(`${BASE_URL}`, query)
}

export function getAttendance(id: string | number) {
  return http.get<T.AttendanceResp>(`${BASE_URL}/${id}`)
}

export function addAttendance(data: T.AttendanceReq) {
  return http.post(`${BASE_URL}`, data)
}

export function updateAttendance(data: T.AttendanceReq, id: string | number) {
  return http.put(`${BASE_URL}/${id}`, data)
}

export function deleteAttendance(id: string | number) {
  return http.del(`${BASE_URL}`, { ids: [id] })
}
