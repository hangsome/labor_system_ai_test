package com.labor.system.contract.web.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class TerminateLaborContractRequest {

  @NotNull(message = "terminateDate 不能为空")
  private LocalDate terminateDate;

  public LocalDate getTerminateDate() {
    return terminateDate;
  }

  public void setTerminateDate(LocalDate terminateDate) {
    this.terminateDate = terminateDate;
  }
}
