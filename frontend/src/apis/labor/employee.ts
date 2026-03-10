import type * as T from './type'
import http from '@/utils/http'

export type * from './type'

const BASE_URL = '/labor/workforce/employee'

export function listEmployee(query: T.EmployeePageQuery) {
  return http.get<PageRes<T.EmployeeResp[]>>(`${BASE_URL}`, query)
}

export function getEmployee(id: string | number) {
  return http.get<T.EmployeeResp>(`${BASE_URL}/${id}`)
}

export function addEmployee(data: T.EmployeeReq) {
  return http.post(`${BASE_URL}`, data)
}

export function updateEmployee(data: T.EmployeeReq, id: string | number) {
  return http.put(`${BASE_URL}/${id}`, data)
}

export function deleteEmployee(id: string | number) {
  return http.del(`${BASE_URL}`, { ids: [id] })
}

export function offboardEmployee(id: string | number, data: T.EmployeeOffboardReq) {
  return http.put(`${BASE_URL}/${id}/offboard`, data)
}
