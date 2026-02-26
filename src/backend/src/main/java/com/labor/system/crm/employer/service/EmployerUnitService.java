package com.labor.system.crm.employer.service;

import com.labor.system.common.api.AppException;
import com.labor.system.crm.employer.domain.EmployerUnit;
import com.labor.system.crm.employer.repository.EmployerUnitRepository;
import com.labor.system.crm.employer.web.dto.CreateEmployerUnitRequest;
import com.labor.system.crm.employer.web.dto.EmployerUnitPageResponse;
import com.labor.system.crm.employer.web.dto.EmployerUnitResponse;
import com.labor.system.crm.employer.web.dto.UpdateEmployerUnitRequest;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployerUnitService {

  private final EmployerUnitRepository employerUnitRepository;

  public EmployerUnitService(EmployerUnitRepository employerUnitRepository) {
    this.employerUnitRepository = employerUnitRepository;
  }

  @Transactional
  public EmployerUnitResponse createEmployerUnit(CreateEmployerUnitRequest request) {
    String unitCode = normalizeRequiredText(request.getUnitCode(), "unitCode");
    if (employerUnitRepository.existsByUnitCode(unitCode)) {
      throw AppException.badRequest("unitCode 已存在");
    }

    EmployerUnit employerUnit = new EmployerUnit();
    applyCreateRequest(employerUnit, request);
    employerUnit.setUnitCode(unitCode);
    EmployerUnit saved = employerUnitRepository.save(employerUnit);
    return toResponse(saved);
  }

  public EmployerUnitResponse getEmployerUnit(Long employerUnitId) {
    return toResponse(findEmployerUnitOrThrow(employerUnitId));
  }

  public EmployerUnitPageResponse listEmployerUnits(
      String keyword, String customerLevel, int page, int pageSize) {
    if (page < 1) {
      throw AppException.badRequest("page 必须大于等于 1");
    }
    if (pageSize < 1 || pageSize > 100) {
      throw AppException.badRequest("pageSize 必须在 1-100 之间");
    }

    String normalizedKeyword = normalizeOptionalText(keyword);
    String normalizedCustomerLevel = normalizeOptionalText(customerLevel);
    PageRequest pageRequest = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "id"));
    Page<EmployerUnit> result =
        employerUnitRepository.search(normalizedKeyword, normalizedCustomerLevel, pageRequest);
    List<EmployerUnitResponse> records = result.getContent().stream().map(this::toResponse).toList();
    return new EmployerUnitPageResponse(records, result.getTotalElements(), page, pageSize);
  }

  @Transactional
  public EmployerUnitResponse updateEmployerUnit(Long employerUnitId, UpdateEmployerUnitRequest request) {
    EmployerUnit employerUnit = findEmployerUnitOrThrow(employerUnitId);
    String unitCode = normalizeRequiredText(request.getUnitCode(), "unitCode");
    if (!unitCode.equals(employerUnit.getUnitCode())
        && employerUnitRepository.existsByUnitCodeAndIdNot(unitCode, employerUnitId)) {
      throw AppException.badRequest("unitCode 已存在");
    }

    applyUpdateRequest(employerUnit, request);
    employerUnit.setUnitCode(unitCode);
    EmployerUnit updated = employerUnitRepository.save(employerUnit);
    return toResponse(updated);
  }

  @Transactional
  public EmployerUnitResponse deactivateEmployerUnit(Long employerUnitId) {
    EmployerUnit employerUnit = findEmployerUnitOrThrow(employerUnitId);
    employerUnit.setCustomerLevel("INACTIVE");
    EmployerUnit updated = employerUnitRepository.save(employerUnit);
    return toResponse(updated);
  }

  private EmployerUnit findEmployerUnitOrThrow(Long employerUnitId) {
    return employerUnitRepository
        .findById(employerUnitId)
        .orElseThrow(() -> AppException.badRequest("用工单位不存在"));
  }

  private void applyCreateRequest(EmployerUnit employerUnit, CreateEmployerUnitRequest request) {
    employerUnit.setLeadId(request.getLeadId());
    employerUnit.setUnitName(normalizeRequiredText(request.getUnitName(), "unitName"));
    employerUnit.setCustomerLevel(normalizeRequiredText(request.getCustomerLevel(), "customerLevel"));
    employerUnit.setAddress(normalizeOptionalText(request.getAddress()));
    employerUnit.setInvoiceInfo(normalizeOptionalText(request.getInvoiceInfo()));
    employerUnit.setOutsource(Boolean.TRUE.equals(request.getOutsource()));
  }

  private void applyUpdateRequest(EmployerUnit employerUnit, UpdateEmployerUnitRequest request) {
    employerUnit.setLeadId(request.getLeadId());
    employerUnit.setUnitName(normalizeRequiredText(request.getUnitName(), "unitName"));
    employerUnit.setCustomerLevel(normalizeRequiredText(request.getCustomerLevel(), "customerLevel"));
    employerUnit.setAddress(normalizeOptionalText(request.getAddress()));
    employerUnit.setInvoiceInfo(normalizeOptionalText(request.getInvoiceInfo()));
    employerUnit.setOutsource(Boolean.TRUE.equals(request.getOutsource()));
  }

  private EmployerUnitResponse toResponse(EmployerUnit employerUnit) {
    return new EmployerUnitResponse(
        employerUnit.getId(),
        employerUnit.getUnitCode(),
        employerUnit.getLeadId(),
        employerUnit.getUnitName(),
        employerUnit.getCustomerLevel(),
        employerUnit.getAddress(),
        employerUnit.getInvoiceInfo(),
        employerUnit.getOutsource(),
        toDateTimeText(employerUnit.getCreatedAt()),
        toDateTimeText(employerUnit.getUpdatedAt()));
  }

  private String normalizeRequiredText(String value, String fieldName) {
    if (value == null || value.trim().isEmpty()) {
      throw AppException.badRequest(fieldName + " 不能为空");
    }
    return value.trim();
  }

  private String normalizeOptionalText(String value) {
    if (value == null) {
      return null;
    }
    String normalized = value.trim();
    return normalized.isEmpty() ? null : normalized;
  }

  private String toDateTimeText(LocalDateTime value) {
    return value == null ? null : value.toString();
  }
}
