package com.example.learningplatform.service;

import com.example.learningplatform.dto.*;
import com.example.learningplatform.entity.Subject;
import com.example.learningplatform.repository.SubjectRepository;
import com.example.learningplatform.dto.SubjectOptionDTO;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import jakarta.persistence.criteria.Predicate;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class SubjectService {

    @Autowired
    private SubjectRepository subjectRepository;

    // ==================== 分页查询 ====================
    public PageResult<SubjectDTO> page(int pageNum, int pageSize, String name, String category, String subCategory) {
        // 构建动态查询条件
        Specification<Subject> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (name != null && !name.trim().isEmpty()) {
                predicates.add(cb.like(root.get("name"), "%" + name + "%"));
            }
            
            if (category != null && !category.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            
            if (subCategory != null && !subCategory.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("subCategory"), subCategory));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 分页并排序
        Page<Subject> page = subjectRepository.findAll(spec, 
                PageRequest.of(pageNum - 1, pageSize, Sort.by("id").descending()));
        
        List<SubjectDTO> list = page.getContent().stream().map(this::toDTO).toList();
        return new PageResult<>(list, page.getTotalElements(), pageNum, pageSize);
    }

    //新增
    @Transactional
    public SubjectDTO create(SubjectDTO dto) {
        if (subjectRepository.existsByCode(dto.getCode())) {
            throw new RuntimeException("科目编码已存在");
        }
        
        Subject subject = new Subject();
        subject.setCode(dto.getCode());
        subject.setName(dto.getName());
        subject.setCategory(dto.getCategory());
        subject.setSubCategory(dto.getSubCategory());
        subject.setDescription(dto.getDescription());
        subject.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        subject.setCreatedAt(new Date());
        
        subject = subjectRepository.save(subject);
        return toDTO(subject);
    }

    //更新
    @Transactional
    public SubjectDTO update(Long id, SubjectDTO dto) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("科目不存在"));
        
        if (dto.getCode() != null && !subject.getCode().equals(dto.getCode())) {
            if (subjectRepository.existsByCode(dto.getCode())) {
                throw new RuntimeException("科目编码已存在");
            }
            subject.setCode(dto.getCode());
        }
        
        if (dto.getName() != null) subject.setName(dto.getName());
        if (dto.getCategory() != null) subject.setCategory(dto.getCategory());
        if (dto.getSubCategory() != null) subject.setSubCategory(dto.getSubCategory());
        if (dto.getDescription() != null) subject.setDescription(dto.getDescription());
        if (dto.getStatus() != null) subject.setStatus(dto.getStatus());
        
        subject = subjectRepository.save(subject);
        return toDTO(subject);
    }

    //删除
    @Transactional
    public void delete(Long id) {
        subjectRepository.deleteById(id);
    }

    //批量删除
    @Transactional
    public void deleteBatch(List<Long> ids) {
        subjectRepository.deleteAllById(ids);
    }

    //查询全部
    public List<SubjectDTO> listAll() {
        return subjectRepository.findAll().stream().map(this::toDTO).toList();
    }

    //CSV 导入
    @Transactional
    public BatchImportResultDTO importFromCsv(MultipartFile file) {
        List<String> errors = new ArrayList<>();
        int success = 0;
        int total = 0;
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                total++;
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                
                try {
                    String[] parts = line.split(",");
                    if (parts.length >= 3) {
                        Subject subject = new Subject();
                        subject.setCode(parts[0].trim());
                        subject.setName(parts[1].trim());
                        subject.setCategory(parts[2].trim());
                        subject.setSubCategory(parts.length > 3 ? parts[3].trim() : null);
                        subject.setDescription(parts.length > 4 ? parts[4].trim() : null);
                        subject.setStatus(1);
                        subject.setCreatedAt(new Date());
                        
                        if (!subjectRepository.existsByCode(subject.getCode())) {
                            subjectRepository.save(subject);
                            success++;
                        } else {
                            errors.add("第" + total + "行: 编码 " + subject.getCode() + " 已存在");
                        }
                    }
                } catch (Exception e) {
                    errors.add("第" + total + "行: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("CSV导入失败: " + e.getMessage());
        }
        
        return new BatchImportResultDTO(total - 1, success, total - 1 - success, errors);
    }

    @Transactional
    public BatchImportResultDTO importFromExcel(MultipartFile file) {
        List<String> errors = new ArrayList<>();
        int success = 0;
        int total = 0;
        
        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();
            
            for (int i = 1; i <= lastRowNum; i++) {
                total++;
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                try {
                    String code = getCellValueAsString(row.getCell(0));
                    String name = getCellValueAsString(row.getCell(1));
                    String category = getCellValueAsString(row.getCell(2));
                    String subCategory = getCellValueAsString(row.getCell(3));
                    String description = getCellValueAsString(row.getCell(4));
                    String statusStr = getCellValueAsString(row.getCell(5));
                    
                    if (code == null || code.trim().isEmpty()) {
                        errors.add("第" + (i + 1) + "行: 科目编码不能为空");
                        continue;
                    }
                    if (name == null || name.trim().isEmpty()) {
                        errors.add("第" + (i + 1) + "行: 科目名称不能为空");
                        continue;
                    }
                    
                    Subject subject = new Subject();
                    subject.setCode(code.trim());
                    subject.setName(name.trim());
                    subject.setCategory(category != null ? category.trim() : null);
                    subject.setSubCategory(subCategory != null ? subCategory.trim() : null);
                    subject.setDescription(description != null ? description.trim() : null);
                    
                    Integer status = 1;
                    if (statusStr != null && !statusStr.trim().isEmpty()) {
                        try {
                            status = Integer.parseInt(statusStr.trim());
                        } catch (NumberFormatException e) {
                            status = 1;
                        }
                    }
                    subject.setStatus(status);
                    subject.setCreatedAt(new Date());
                    
                    if (!subjectRepository.existsByCode(subject.getCode())) {
                        subjectRepository.save(subject);
                        success++;
                    } else {
                        errors.add("第" + (i + 1) + "行: 编码 " + subject.getCode() + " 已存在");
                    }
                    
                } catch (Exception e) {
                    errors.add("第" + (i + 1) + "行: " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Excel导入失败: " + e.getMessage());
        }
        
        return new BatchImportResultDTO(total, success, total - success, errors);
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double numValue = cell.getNumericCellValue();
                    if (numValue == (long) numValue) {
                        return String.valueOf((long) numValue);
                    } else {
                        return String.valueOf(numValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return null;
        }
    }
    
    //导出导入模板
    public void exportTemplate(HttpServletResponse response) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("科目导入模板");
            
            // 创建表头样式
            CellStyle headerStyle = createHeaderStyle(workbook);
            
            // 创建表头行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"科目编码*", "科目名称*", "分类", "二级分类", "描述", "状态"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                int columnWidth = sheet.getColumnWidth(i);
                sheet.setColumnWidth(i, columnWidth + 1024);
            }
            
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", 
                    "attachment; filename=subject_template_" + System.currentTimeMillis() + ".xlsx");
            
            workbook.write(response.getOutputStream());
            response.getOutputStream().flush();
            
        } catch (Exception e) {
            throw new RuntimeException("导出模板失败: " + e.getMessage(), e);
        }
    }

    // 创建表头样式 
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    public SubjectDTO getById(Long id) {
        return subjectRepository.findById(id)
                .map(this::toDTO)
                .orElse(null);
    }
    private SubjectDTO toDTO(Subject subject) {
        SubjectDTO dto = new SubjectDTO();
        dto.setId(subject.getId());
        dto.setCode(subject.getCode());
        dto.setName(subject.getName());
        dto.setCategory(subject.getCategory());
        dto.setSubCategory(subject.getSubCategory());
        dto.setDescription(subject.getDescription());
        dto.setStatus(subject.getStatus());
        dto.setCreatedAt(subject.getCreatedAt());
        return dto;
    }

    public List<SubjectOptionDTO> listOptions() {
    return subjectRepository.findAll().stream()
            .map(s -> new SubjectOptionDTO(s.getId(), s.getName()))
            .toList();
}
}