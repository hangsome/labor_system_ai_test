package com.labor.system.rbac.web;

import com.labor.system.common.api.ApiResponse;
import com.labor.system.rbac.datascope.DataScopeContext;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/v1/system/data-scope")
class DataScopeTestController {

  @GetMapping("/query")
  public ApiResponse<Map<String, Object>> query(
      @RequestParam(value = "scopeRef", required = false) String scopeRef,
      @RequestParam(value = "userId", required = false) Long userId) {
    DataScopeContext context = DataScopeContext.required();
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("requestScopeRef", scopeRef);
    data.put("requestUserId", userId);
    data.put("userId", context.userId());
    data.put("scopeType", context.scopeType());
    data.put("scopeRef", context.scopeRef());
    return ApiResponse.success(data);
  }
}
