package com.labor.system.common.api;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.slf4j.MDC;

public record ApiResponse<T>(String code, String message, String traceId, String timestamp, T data) {

  private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(ErrorCodes.SUCCESS, "success", resolveTraceId(), now(), data);
  }

  public static <T> ApiResponse<T> error(String code, String message) {
    return new ApiResponse<>(code, message, resolveTraceId(), now(), null);
  }

  private static String resolveTraceId() {
    String traceId = MDC.get("traceId");
    return (traceId == null || traceId.isBlank()) ? UUID.randomUUID().toString() : traceId;
  }

  private static String now() {
    return OffsetDateTime.now(ZONE_ID).format(FORMATTER);
  }
}

