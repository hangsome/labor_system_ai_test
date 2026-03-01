package com.labor.system.contract.web.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class RenewLaborContractRequest {

  @NotNull(message = "newEndDate 不能为空")
  private LocalDate newEndDate;

  public LocalDate getNewEndDate() {
    return newEndDate;
  }

  public void setNewEndDate(LocalDate newEndDate) {
    this.newEndDate = newEndDate;
  }
}
