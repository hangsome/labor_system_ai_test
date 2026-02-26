package com.labor.system.common.api;

import org.springframework.http.HttpStatus;

public class AppException extends RuntimeException {

  private final HttpStatus status;
  private final String code;

  public AppException(HttpStatus status, String code, String message) {
    super(message);
    this.status = status;
    this.code = code;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public String getCode() {
    return code;
  }

  public static AppException unauthorized(String message) {
    return new AppException(HttpStatus.UNAUTHORIZED, ErrorCodes.AUTH_UNAUTHORIZED, message);
  }

  public static AppException forbidden(String message) {
    return new AppException(HttpStatus.FORBIDDEN, ErrorCodes.AUTH_FORBIDDEN, message);
  }

  public static AppException badRequest(String message) {
    return new AppException(HttpStatus.BAD_REQUEST, ErrorCodes.REQUEST_INVALID, message);
  }
}
