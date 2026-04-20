package com.employee.employee_system.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ImportResultDto {
    private int successCount;
    private int failCount;
    private List<String> errors;
}
