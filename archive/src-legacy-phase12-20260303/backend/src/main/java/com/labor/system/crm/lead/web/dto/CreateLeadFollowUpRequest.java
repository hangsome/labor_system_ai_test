package com.labor.system.crm.lead.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class CreateLeadFollowUpRequest {

  @NotBlank(message = "content 不能为空")
  @Size(max = 512, message = "content 长度不能超过 512")
  private String content;

  private LocalDateTime nextContactAt;

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public LocalDateTime getNextContactAt() {
    return nextContactAt;
  }

  public void setNextContactAt(LocalDateTime nextContactAt) {
    this.nextContactAt = nextContactAt;
  }
}
