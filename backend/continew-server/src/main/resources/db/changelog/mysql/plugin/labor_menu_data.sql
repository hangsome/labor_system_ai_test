-- liquibase formatted sql

-- changeset labor:2
-- comment Initialize labor menus and permission codes
INSERT IGNORE INTO `sys_menu`
(`id`, `title`, `parent_id`, `type`, `path`, `name`, `component`, `redirect`, `icon`, `is_external`, `is_cache`, `is_hidden`, `permission`, `sort`, `status`, `create_user`, `create_time`)
VALUES
(10000, 'Labor', 0, 1, '/labor', 'Labor', 'Layout', '/labor/crm/lead', 'apps', b'0', b'0', b'0', NULL, 10, 1, 1, NOW()),

(10100, 'Lead', 10000, 2, '/labor/crm/lead', 'LaborLead', 'labor/crm/lead/index', NULL, 'unordered-list', b'0', b'0', b'0', NULL, 1, 1, 1, NOW()),
(10101, 'List', 10100, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:lead:list', 1, 1, 1, NOW()),
(10102, 'Get', 10100, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:lead:get', 2, 1, 1, NOW()),
(10103, 'Create', 10100, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:lead:create', 3, 1, 1, NOW()),
(10104, 'Update', 10100, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:lead:update', 4, 1, 1, NOW()),
(10105, 'Delete', 10100, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:lead:delete', 5, 1, 1, NOW()),
(10106, 'Transition', 10100, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:lead:transition', 6, 1, 1, NOW()),
(10107, 'FollowUpList', 10100, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:lead:followUp:list', 7, 1, 1, NOW()),
(10108, 'FollowUpCreate', 10100, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:lead:followUp:create', 8, 1, 1, NOW()),
(10109, 'LeadDetail', 10100, 2, '/labor/crm/lead/detail', 'LaborLeadDetail', 'labor/crm/lead/detail', NULL, NULL, b'0', b'0', b'1', 'labor:lead:get', 9, 1, 1, NOW()),

(10200, 'Employer', 10000, 2, '/labor/crm/employer', 'LaborEmployer', 'labor/crm/employer/index', NULL, 'bank', b'0', b'0', b'0', NULL, 2, 1, 1, NOW()),
(10201, 'List', 10200, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:employer:list', 1, 1, 1, NOW()),
(10202, 'Get', 10200, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:employer:get', 2, 1, 1, NOW()),
(10203, 'Create', 10200, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:employer:create', 3, 1, 1, NOW()),
(10204, 'Update', 10200, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:employer:update', 4, 1, 1, NOW()),
(10205, 'Delete', 10200, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:employer:delete', 5, 1, 1, NOW()),
(10206, 'Deactivate', 10200, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:employer:deactivate', 6, 1, 1, NOW()),

(10300, 'Contract', 10000, 2, '/labor/contract', 'LaborContract', 'labor/contract/index', NULL, 'book', b'0', b'0', b'0', NULL, 3, 1, 1, NOW()),
(10301, 'List', 10300, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:contract:list', 1, 1, 1, NOW()),
(10302, 'Get', 10300, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:contract:get', 2, 1, 1, NOW()),
(10303, 'Create', 10300, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:contract:create', 3, 1, 1, NOW()),
(10304, 'Update', 10300, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:contract:update', 4, 1, 1, NOW()),
(10305, 'Delete', 10300, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:contract:delete', 5, 1, 1, NOW()),
(10306, 'Sign', 10300, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:contract:sign', 6, 1, 1, NOW()),
(10307, 'Renew', 10300, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:contract:renew', 7, 1, 1, NOW()),
(10308, 'Terminate', 10300, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:contract:terminate', 8, 1, 1, NOW()),
(10309, 'ContractDetail', 10300, 2, '/labor/contract/detail', 'LaborContractDetail', 'labor/contract/detail', NULL, NULL, b'0', b'0', b'1', 'labor:contract:get', 9, 1, 1, NOW()),

(10400, 'Settlement', 10000, 2, '/labor/contract/settlement', 'LaborSettlement', 'labor/contract/settlement/index', NULL, 'ordered-list', b'0', b'0', b'0', NULL, 4, 1, 1, NOW()),
(10401, 'List', 10400, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:settlement:list', 1, 1, 1, NOW()),
(10402, 'Get', 10400, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:settlement:get', 2, 1, 1, NOW()),
(10403, 'Create', 10400, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:settlement:create', 3, 1, 1, NOW()),
(10404, 'Update', 10400, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:settlement:update', 4, 1, 1, NOW()),
(10405, 'Delete', 10400, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:settlement:delete', 5, 1, 1, NOW()),
(10406, 'Publish', 10400, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:settlement:publish', 6, 1, 1, NOW()),
(10407, 'Deactivate', 10400, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:settlement:deactivate', 7, 1, 1, NOW()),
(10408, 'Version', 10400, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:settlement:version', 8, 1, 1, NOW()),
(10409, 'Active', 10400, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:settlement:active', 9, 1, 1, NOW());

INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
VALUES
(1, 10000),
(1, 10100), (1, 10101), (1, 10102), (1, 10103), (1, 10104), (1, 10105), (1, 10106), (1, 10107), (1, 10108), (1, 10109),
(1, 10200), (1, 10201), (1, 10202), (1, 10203), (1, 10204), (1, 10205), (1, 10206),
(1, 10300), (1, 10301), (1, 10302), (1, 10303), (1, 10304), (1, 10305), (1, 10306), (1, 10307), (1, 10308), (1, 10309),
(1, 10400), (1, 10401), (1, 10402), (1, 10403), (1, 10404), (1, 10405), (1, 10406), (1, 10407), (1, 10408), (1, 10409);

-- changeset labor:3-phase3-menu
-- comment Initialize workforce and attendance menus for labor phase 03
INSERT IGNORE INTO `sys_menu`
(`id`, `title`, `parent_id`, `type`, `path`, `name`, `component`, `redirect`, `icon`, `is_external`, `is_cache`, `is_hidden`, `permission`, `sort`, `status`, `create_user`, `create_time`)
VALUES
(10500, 'Workforce', 10000, 1, '/labor/workforce', 'LaborWorkforce', 'ParentView', '/labor/workforce/employee', 'team', b'0', b'0', b'0', NULL, 5, 1, 1, NOW()),
(10510, 'Employee', 10500, 2, '/labor/workforce/employee', 'LaborEmployee', 'labor/workforce/employee/index', NULL, 'user', b'0', b'0', b'0', NULL, 1, 1, 1, NOW()),
(10511, 'List', 10510, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:employee:list', 1, 1, 1, NOW()),
(10512, 'Get', 10510, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:employee:get', 2, 1, 1, NOW()),
(10513, 'Create', 10510, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:employee:create', 3, 1, 1, NOW()),
(10514, 'Update', 10510, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:employee:update', 4, 1, 1, NOW()),
(10515, 'Delete', 10510, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:employee:delete', 5, 1, 1, NOW()),
(10516, 'Offboard', 10510, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:employee:offboard', 6, 1, 1, NOW()),

(10520, 'Assignment', 10500, 2, '/labor/workforce/assignment', 'LaborAssignment', 'labor/workforce/assignment/index', NULL, 'unordered-list', b'0', b'0', b'0', NULL, 2, 1, 1, NOW()),
(10521, 'List', 10520, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:assignment:list', 1, 1, 1, NOW()),
(10522, 'Get', 10520, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:assignment:get', 2, 1, 1, NOW()),
(10523, 'Create', 10520, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:assignment:create', 3, 1, 1, NOW()),
(10524, 'Update', 10520, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:assignment:update', 4, 1, 1, NOW()),
(10525, 'Delete', 10520, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:assignment:delete', 5, 1, 1, NOW()),

(10600, 'Attendance', 10000, 1, '/labor/attendance', 'LaborAttendance', 'ParentView', '/labor/attendance/record', 'calendar', b'0', b'0', b'0', NULL, 6, 1, 1, NOW()),
(10610, 'Shift', 10600, 2, '/labor/attendance/shift', 'LaborShift', 'labor/attendance/shift/index', NULL, 'clock-circle', b'0', b'0', b'0', NULL, 1, 1, 1, NOW()),
(10611, 'List', 10610, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:shift:list', 1, 1, 1, NOW()),
(10612, 'Get', 10610, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:shift:get', 2, 1, 1, NOW()),
(10613, 'Create', 10610, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:shift:create', 3, 1, 1, NOW()),
(10614, 'Update', 10610, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:shift:update', 4, 1, 1, NOW()),
(10615, 'Delete', 10610, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:shift:delete', 5, 1, 1, NOW()),

(10620, 'Record', 10600, 2, '/labor/attendance/record', 'LaborAttendanceRecord', 'labor/attendance/record/index', NULL, 'schedule', b'0', b'0', b'0', NULL, 2, 1, 1, NOW()),
(10621, 'List', 10620, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:attendance:list', 1, 1, 1, NOW()),
(10622, 'Get', 10620, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:attendance:get', 2, 1, 1, NOW()),
(10623, 'Create', 10620, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:attendance:create', 3, 1, 1, NOW()),
(10624, 'Update', 10620, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:attendance:update', 4, 1, 1, NOW()),
(10625, 'Delete', 10620, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:attendance:delete', 5, 1, 1, NOW()),

(10630, 'Correction', 10600, 2, '/labor/attendance/correction', 'LaborAttendanceCorrection', 'labor/attendance/correction/index', NULL, 'edit', b'0', b'0', b'0', NULL, 3, 1, 1, NOW()),
(10631, 'List', 10630, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:correction:list', 1, 1, 1, NOW()),
(10632, 'Get', 10630, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:correction:get', 2, 1, 1, NOW()),
(10633, 'Create', 10630, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:correction:create', 3, 1, 1, NOW()),
(10634, 'Approve', 10630, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:correction:approve', 4, 1, 1, NOW()),
(10635, 'Reject', 10630, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'labor:correction:reject', 5, 1, 1, NOW());

INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
VALUES
(1, 10500),
(1, 10510), (1, 10511), (1, 10512), (1, 10513), (1, 10514), (1, 10515), (1, 10516),
(1, 10520), (1, 10521), (1, 10522), (1, 10523), (1, 10524), (1, 10525),
(1, 10600),
(1, 10610), (1, 10611), (1, 10612), (1, 10613), (1, 10614), (1, 10615),
(1, 10620), (1, 10621), (1, 10622), (1, 10623), (1, 10624), (1, 10625),
(1, 10630), (1, 10631), (1, 10632), (1, 10633), (1, 10634), (1, 10635);
