import { ref } from 'vue'
import { defineStore } from 'pinia'
import type { RouteRecordRaw } from 'vue-router'
import { mapTree, toTreeArray } from 'xe-utils'
import { cloneDeep, omit } from 'lodash-es'
import { constantRoutes, systemRoutes } from '@/router/route'
import { type RouteItem, getUserRoute } from '@/apis'
import { transformPathToName } from '@/utils'
import { asyncRouteModules } from '@/router/asyncModules'

const layoutComponentMap = {
  Layout: () => import('@/layout/index.vue'),
  ParentView: () => import('@/components/ParentView/index.vue'),
}

const laborTitleMap: Record<string, string> = {
  Labor: '劳务管理',
  Lead: '线索管理',
  Employer: '雇主管理',
  Contract: '合同管理',
  Settlement: '结算管理',
  List: '列表',
  Get: '详情',
  Create: '新增',
  Update: '修改',
  Delete: '删除',
  Transition: '状态流转',
  FollowUpList: '跟进列表',
  FollowUpCreate: '新增跟进',
  LeadDetail: '线索详情',
  Deactivate: '停用',
  Sign: '签署',
  Renew: '续签',
  Terminate: '终止',
  ContractDetail: '合同详情',
  Publish: '发布',
  Version: '版本',
  Active: '生效',
}

const localizeLaborTitle = (item: RouteItem) => {
  const title = item.title
  if (!title) return title
  if (!item.path?.startsWith('/labor') && !item.permission?.startsWith('labor:')) return title
  return laborTitleMap[title] || title
}

/** 将component由字符串转成真正的模块 */
const transformComponentView = (component: string) => {
  return layoutComponentMap[component as keyof typeof layoutComponentMap] || asyncRouteModules[component]
}

/**
 * @description 前端来做排序、格式化
 * @params {menus} 后端返回的路由数据，已经根据当前用户角色过滤掉了没权限的路由
 * 1. 对后端返回的路由数据进行排序，格式化
 * 2. 同时将component由字符串转成真正的模块
 */
const formatAsyncRoutes = (menus: RouteItem[]) => {
  if (!menus.length) return []

  const pathMap = new Map()
  return mapTree(menus, (item) => {
    pathMap.set(item.id, item.path)

    if (item.children?.length) {
      item.children.sort((a, b) => (a?.sort ?? 0) - (b?.sort ?? 0))
    }

    // 部分子菜单，例如：通知公告新增、查看详情，需要选中其父菜单
    if (item.parentId && item.type === 2) {
      const parentPath = pathMap.get(item.parentId)
      if ((item.permission || item.isHidden) && !item.activeMenu) {
        item.activeMenu = parentPath
      }
    }

    return {
      path: item.path,
      name: item.name ?? transformPathToName(item.path),
      component: transformComponentView(item.component),
      redirect: item.redirect,
      meta: {
        title: localizeLaborTitle(item),
        hidden: item.isHidden,
        keepAlive: item.isCache,
        icon: item.icon,
        showInTabs: item.isHidden ? false : item.showInTabs,
        activeMenu: item.activeMenu,
      },
    }
  }) as unknown as RouteRecordRaw[]
}

/** 判断路由层级是否大于 2 */
export const isMultipleRoute = (route: RouteRecordRaw) => {
  return route.children?.some((child) => child.children?.length) ?? false
}

/** 路由降级（把三级及其以上的路由转化为二级路由） */
export const flatMultiLevelRoutes = (routes: RouteRecordRaw[]) => {
  return cloneDeep(routes).map((route) => {
    if (!isMultipleRoute(route)) return route

    return {
      ...route,
      children: toTreeArray(route.children).map((item) => omit(item, 'children')) as RouteRecordRaw[],
    }
  })
}

/**
 * 在混合布局下，将 labor 一级菜单挂载到仪表盘左侧菜单层级
 */
const mountLaborUnderDashboardMenu = (allRoutes: RouteRecordRaw[]) => {
  const dashboardRoute = allRoutes.find((item) => item.path === '/' && item.name === 'Dashboard')
  const laborRouteIndex = allRoutes.findIndex((item) => item.path === '/labor')
  if (!dashboardRoute || laborRouteIndex < 0) return allRoutes

  const [laborRoute] = allRoutes.splice(laborRouteIndex, 1)
  dashboardRoute.children = dashboardRoute.children || []
  if (!dashboardRoute.children.some((item) => item.path === laborRoute.path)) {
    dashboardRoute.children.push(laborRoute)
    dashboardRoute.children.sort((a, b) => (a.meta?.sort ?? 0) - (b.meta?.sort ?? 0))
  }
  return allRoutes
}

const storeSetup = () => {
  // 所有路由(常驻路由 + 动态路由)
  const routes = ref<RouteRecordRaw[]>([])
  // 动态路由(异步路由)
  const asyncRoutes = ref<RouteRecordRaw[]>([])

  // 合并路由
  const setRoutes = (data: RouteRecordRaw[]) => {
    // 合并路由并排序
    const staticRoutes = cloneDeep([...constantRoutes, ...systemRoutes])
    const dynamicRoutes = cloneDeep(data)
    const mergedRoutes = staticRoutes.concat(dynamicRoutes)
      .sort((a, b) => (a.meta?.sort ?? 0) - (b.meta?.sort ?? 0))
    routes.value = mountLaborUnderDashboardMenu(mergedRoutes)
    asyncRoutes.value = data
  }

  // 生成路由
  const generateRoutes = async (): Promise<RouteRecordRaw[]> => {
    const { data } = await getUserRoute()
    const asyncRoutes = formatAsyncRoutes(data)
    const flatRoutes = flatMultiLevelRoutes(cloneDeep(asyncRoutes))
    setRoutes(asyncRoutes)
    return flatRoutes
  }

  return {
    routes,
    asyncRoutes,
    generateRoutes,
  }
}

export const useRouteStore = defineStore('route', storeSetup, { persist: true })
