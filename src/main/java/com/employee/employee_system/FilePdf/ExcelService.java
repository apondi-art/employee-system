package com.employee.employee_system.FilePdf;

import com.employee.employee_system.Dto.EmployeeRequestDto;
import com.employee.employee_system.Dto.ImportResultDto;
import com.employee.employee_system.Exception.ExcelProcessingException;
import com.employee.employee_system.Exception.InvalidFileFormatException;
import com.employee.employee_system.entity.Employee;
import com.employee.employee_system.service.EmployeeService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ExcelService {

    private final EmployeeService employeeService;
    private final Validator validator; // Jakarta Bean Validator

    // ── IMPORT ───────────────────────────────────────────────
    @Transactional
    public ImportResultDto importEmployees(MultipartFile file) {

        String name = file.getOriginalFilename();
        if (name == null || !name.endsWith(".xlsx")) {
            throw new InvalidFileFormatException(
                    "Only .xlsx files accepted. Got: " + name);
        }

        int success = 0, fail = 0;
        List<String> errors = new ArrayList<>();

        try (XSSFWorkbook wb =
                     new XSSFWorkbook(file.getInputStream())) {

            Sheet sheet = wb.getSheetAt(0);

            // i = 1 skips row 0 (the header row)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    EmployeeRequestDto dto = rowToDto(row);

                    // Validate the DTO manually (not via @Valid here)
                    Set<ConstraintViolation<EmployeeRequestDto>> v =
                            validator.validate(dto);

                    if (!v.isEmpty()) {
                        final int rowNum = i;
                        v.forEach(cv -> errors.add(
                                "Row " + (rowNum+1) + ": " +
                                        cv.getPropertyPath() + " – " +
                                        cv.getMessage()));
                        fail++;
                    } else {
                        employeeService.create(dto);
                        success++;
                    }
                } catch (Exception e) {
                    errors.add("Row " + (i+1) + ": " + e.getMessage());
                    fail++;
                }
            }
        } catch (IOException e) {
            throw new ExcelProcessingException(
                    "Failed to read file", e);
        }

        return new ImportResultDto(success, fail, errors);
    }

    // ── EXPORT ───────────────────────────────────────────────
    public void exportToExcel(HttpServletResponse resp,
                              List<Employee> employees) throws IOException {

        String ts = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        resp.setContentType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        resp.setHeader("Content-Disposition",
                "attachment; filename=\"employees_" + ts + ".xlsx\"");

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet("Employees");

            // Header style — bold + dark background
            XSSFCellStyle hStyle = wb.createCellStyle();
            XSSFFont hFont = wb.createFont();
            hFont.setBold(true); hFont.setColor(IndexedColors.WHITE.getIndex());
            hStyle.setFont(hFont);
            hStyle.setFillForegroundColor(
                    new XSSFColor(new byte[]{(byte)31,(byte)73,(byte)125}));
            hStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            String[] heads = {"ID","First Name","Last Name",
                    "Email","Department","Salary",
                    "Join Date","Active","Created At","Updated At"};
            Row hRow = sheet.createRow(0);
            for (int i = 0; i < heads.length; i++) {
                Cell c = hRow.createCell(i);
                c.setCellValue(heads[i]); c.setCellStyle(hStyle);
            }

            // Alternating row colours
            XSSFCellStyle white = wb.createCellStyle();
            XSSFCellStyle blue  = wb.createCellStyle();
            blue.setFillForegroundColor(
                    new XSSFColor(new byte[]{(byte)220,(byte)230,(byte)241}));
            blue.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            for (int i = 0; i < employees.size(); i++) {
                Employee e = employees.get(i);
                Row row = sheet.createRow(i + 1);
                XSSFCellStyle s = (i % 2 == 0) ? white : blue;

                Cell c0 = row.createCell(0); c0.setCellValue(e.getId()); c0.setCellStyle(s);
                Cell c1 = row.createCell(1); c1.setCellValue(e.getFirstName()); c1.setCellStyle(s);
                Cell c2 = row.createCell(2); c2.setCellValue(e.getLastName()); c2.setCellStyle(s);
                Cell c3 = row.createCell(3); c3.setCellValue(e.getEmail()); c3.setCellStyle(s);
                Cell c4 = row.createCell(4); c4.setCellValue(e.getDepartment()); c4.setCellStyle(s);
                Cell c5 = row.createCell(5); c5.setCellValue(e.getSalary().doubleValue()); c5.setCellStyle(s);
                Cell c6 = row.createCell(6); c6.setCellValue(e.getDateOfJoining().toString()); c6.setCellStyle(s);
                Cell c7 = row.createCell(7); c7.setCellValue(e.getActive()); c7.setCellStyle(s);
                Cell c8 = row.createCell(8); c8.setCellValue(e.getCreatedAt().toString()); c8.setCellStyle(s);
                Cell c9 = row.createCell(9); c9.setCellValue(e.getUpdatedAt().toString()); c9.setCellStyle(s);
            }

            for (int i = 0; i < heads.length; i++) sheet.autoSizeColumn(i);

            wb.write(resp.getOutputStream()); // stream — no temp file
        }
    }

    // Map one Excel row → EmployeeRequestDto
    private EmployeeRequestDto rowToDto(Row row) {
        return EmployeeRequestDto.builder()
                .firstName(str(row, 0))
                .lastName(str(row, 1))
                .email(str(row, 2))
                .department(str(row, 3))
                .salary(new BigDecimal(str(row, 4)))
                .dateOfJoining(LocalDate.parse(str(row, 5)))
                .active(Boolean.parseBoolean(str(row, 6)))
                .build();
    }

    private String str(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return "";
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }
}
