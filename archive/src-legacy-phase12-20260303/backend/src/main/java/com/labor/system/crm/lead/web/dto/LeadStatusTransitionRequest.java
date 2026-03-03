package com.labor.system.crm.lead.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LeadStatusTransitionRequest {

  @NotBlank(message = "toStatus 不能为空")
  @Size(max = 32, message = "toStatus 长度不能超过 32")
  private String toStatus;

  @Size(max = 512, message = "comment 长度不能超过 512")
  private String comment;

  public String getToStatus() {
    return toStatus;
  }

  public void setToStatus(String toStatus) {
    this.toStatus = toStatus;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }
}
