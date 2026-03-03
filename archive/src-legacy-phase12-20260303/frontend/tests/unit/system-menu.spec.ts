import { RouterLinkStub, mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import SystemRoleShellView from '../../src/views/system/SystemRoleShellView.vue'

describe('system-menu', () => {
  it('should render expected menu entries and routes', () => {
    const wrapper = mount(SystemRoleShellView, {
      global: {
        plugins: [ElementPlus],
        stubs: {
          RouterLink: RouterLinkStub,
        },
      },
    })

    const menuItems = wrapper.findAll('[data-testid=\"system-menu-item\"]')
    expect(menuItems).toHaveLength(3)

    const links = wrapper.findAllComponents(RouterLinkStub)
    expect(links).toHaveLength(2)
    expect(links[0].props('to')).toBe('/system/roles')
    expect(links[1].props('to')).toBe('/system/audit-logs')
  })

  it('should show disabled state for unavailable menu item', () => {
    const wrapper = mount(SystemRoleShellView, {
      global: {
        plugins: [ElementPlus],
        stubs: {
          RouterLink: RouterLinkStub,
        },
      },
    })

    const disabledItems = wrapper.findAll('[data-testid=\"system-menu-disabled\"]')
    expect(disabledItems).toHaveLength(1)
    expect(disabledItems[0].text()).toContain('数据权限策略')
  })
})
