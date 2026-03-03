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
