import type * as T from './type'
import http from '@/utils/http'

export type * from './type'

const BASE_URL = '/labor/employer'

export function listEmployer(query: T.EmployerPageQuery) {
  return http.get<PageRes<T.EmployerResp[]>>(`${BASE_URL}`, query)
}

export function getEmployer(id: string | number) {
  return http.get<T.EmployerResp>(`${BASE_URL}/${id}`)
}

export function addEmployer(data: T.EmployerReq) {
  return http.post(`${BASE_URL}`, data)
}

export function updateEmployer(data: T.EmployerReq, id: string | number) {
  return http.put(`${BASE_URL}/${id}`, data)
}

export function deleteEmployer(id: string | number) {
  return http.del(`${BASE_URL}`, { ids: [id] })
}

export function deactivateEmployer(id: string | number) {
  return http.put<T.EmployerResp>(`${BASE_URL}/${id}/deactivate`)
}
