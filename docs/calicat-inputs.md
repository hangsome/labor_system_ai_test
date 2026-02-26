# Calicat 输入记录（Stage 0）

## 文件与画板

- `file_id`: `2009807574801920000`
- 设计画板链接节点：`46c3e7f6-a5bb-462c-b919-f698589dac34`
- 需求文档模式链接：同一 `file_id` 下 PRD 卡片

## 已读取数据

1. `get_meta_data(file_id, selected_layer_id=46c3e7f6-a5bb-462c-b919-f698589dac34)`  
   用于快速浏览图层骨架与页面分布。

2. `get_design_data(file_id, selected_layer_id=884932ad-f80c-4b1d-bb1d-fe23fa50e733)`  
   获取“客户线索列表筛选管理页”详细设计样式，供后续前端实现使用。

3. `get_prd_list(file_id)`  
   拉取 PRD 列表，识别主 PRD 和页面级 PRD。

4. `get_prd_full_content(file_id, prd_id=72c587c9-f768-48e3-812c-f8c4a9f40345)`  
   主 PRD：《劳务系统管理系统产品需求文档》。

5. `get_prd_full_content(file_id, prd_id=9f94b6ef-5563-49a9-892b-a5cffc0ce3e7)`  
   页面 PRD：《客户线索列表筛选管理页需求文档》。

## 说明

- 本阶段只做 Stage 0 规格化与骨架初始化，不直接进行页面代码生成。
- 进入实际前端开发时，将按图层粒度继续调用 `get_design_data` 获取详细数据，不使用 `get_meta_data` 直接生成代码。

