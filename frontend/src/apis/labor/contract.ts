import type * as T from './type'
import http from '@/utils/http'

export type * from './type'

const BASE_URL = '/labor/contract'

export function listContract(query: T.ContractPageQuery) {
  return http.get<PageRes<T.ContractResp[]>>(`${BASE_URL}`, query)
}

export function getContract(id: string | number) {
  return http.get<T.ContractResp>(`${BASE_URL}/${id}`)
}

export function addContract(data: T.ContractReq) {
  return http.post(`${BASE_URL}`, data)
}

export function updateContract(data: T.ContractReq, id: string | number) {
  return http.put(`${BASE_URL}/${id}`, data)
}

export function deleteContract(id: string | number) {
  return http.del(`${BASE_URL}`, { ids: [id] })
}

export function signContract(id: string | number) {
  return http.put<T.ContractResp>(`${BASE_URL}/${id}/sign`)
}

export function renewContract(id: string | number, data: T.ContractRenewReq) {
  return http.put<T.ContractResp>(`${BASE_URL}/${id}/renew`, data)
}

export function terminateContract(id: string | number, data: T.ContractTerminateReq) {
  return http.put<T.ContractResp>(`${BASE_URL}/${id}/terminate`, data)
}
