package com.example.learningplatform.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.learningplatform.dto.BatchImportResultDTO;
import com.example.learningplatform.dto.KnowledgePointDTO;
import com.example.learningplatform.dto.KnowledgePointSimpleDTO;
import com.example.learningplatform.dto.PageResult;
import com.example.learningplatform.dto.SubjectKnowledgePointTreeDTO;
import com.example.learningplatform.entity.KnowledgePoint;
import com.example.learningplatform.entity.Subject;
import com.example.learningplatform.repository.KnowledgePointRepository;
import com.example.learningplatform.repository.SubjectRepository;

import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class KnowledgePointService {

    @Autowired
    private KnowledgePointRepository knowledgePointRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    // 分页查询
    public PageResult<KnowledgePointDTO> page(int pageNum, int pageSize, 
            String name, String subjectName,
            Integer difficulty, Integer examWeight) {
		// 构建动态查询条件
		Specification<KnowledgePoint> spec = (root, query, cb) -> {
		List<Predicate> predicates = new ArrayList<>();
	
	// 1. 知识点名称模糊查询
	if (name != null && !name.trim().isEmpty()) {
		predicates.add(cb.like(root.get("name"), "%" + name.trim() + "%"));
	}
	
	// 2. 难度精确查询
	if (difficulty != null) {
		predicates.add(cb.equal(root.get("difficulty"), difficulty));
	}
	
	// 3. 考试权重精确查询
	if (examWeight != null) {
		predicates.add(cb.equal(root.get("examWeight"), examWeight));
	}
	
	// 4. 科目名称模糊查询（需要关联 Subject 表）
	if (subjectName != null && !subjectName.trim().isEmpty()) {
		// 先查询出符合条件的科目ID列表
		List<Subject> subjects = subjectRepository.findByNameContaining(subjectName.trim());
	if (!subjects.isEmpty()) {
		List<Long> subjectIds = subjects.stream().map(Subject::getId).toList();
		predicates.add(root.get("subjectId").in(subjectIds));
	} else {
		// 没有匹配的科目，返回空结果
		predicates.add(cb.isNull(root.get("id")));
		}
	}
		
		return cb.and(predicates.toArray(new Predicate[0]));
	};
	
		Page<KnowledgePoint> page = knowledgePointRepository.findAll(spec,
		PageRequest.of(pageNum - 1, pageSize, Sort.by("id").ascending()));
	
		List<KnowledgePointDTO> list = page.getContent().stream()
		.map(this::toDTO)
		.collect(Collectors.toList());
		return new PageResult<>(list, page.getTotalElements(), pageNum, pageSize);
	}

    // 树形结构查询
    public List<KnowledgePointDTO> tree(Long subjectId) {
        List<KnowledgePoint> all = knowledgePointRepository.findBySubjectId(subjectId);
        Map<Long, KnowledgePointDTO> dtoMap = all.stream()
                .map(this::toDTO)
                .collect(Collectors.toMap(KnowledgePointDTO::getId, dto -> dto));
        
        List<KnowledgePointDTO> roots = new ArrayList<>();
        for (KnowledgePointDTO dto : dtoMap.values()) {
            if (dto.getParentId() == null) {
                roots.add(dto);
            } else {
                KnowledgePointDTO parent = dtoMap.get(dto.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(dto);
                }
            }
        }
        return roots;
    }

    // 简化树形结构查询（返回所有科目及其知识点树形结构，仅返回id和name）
    public List<SubjectKnowledgePointTreeDTO> treeSimple() {
        List<Subject> subjects = subjectRepository.findAll();
        List<KnowledgePoint> allPoints = knowledgePointRepository.findAll();
        
        Map<Long, List<KnowledgePoint>> pointsBySubject = allPoints.stream()
                .collect(Collectors.groupingBy(KnowledgePoint::getSubjectId));
        
        List<SubjectKnowledgePointTreeDTO> result = new ArrayList<>();
        
        for (Subject subject : subjects) {
            SubjectKnowledgePointTreeDTO subjectTree = new SubjectKnowledgePointTreeDTO(
                    subject.getId(), subject.getName());
            
            List<KnowledgePoint> subjectPoints = pointsBySubject.getOrDefault(subject.getId(), List.of());
            
            Map<Long, KnowledgePointSimpleDTO> dtoMap = subjectPoints.stream()
                    .map(kp -> new KnowledgePointSimpleDTO(kp.getId(), kp.getName()))
                    .collect(Collectors.toMap(KnowledgePointSimpleDTO::getId, dto -> dto));
            
            List<KnowledgePointSimpleDTO> roots = new ArrayList<>();
            for (KnowledgePointSimpleDTO dto : dtoMap.values()) {
                KnowledgePoint kp = subjectPoints.stream()
                        .filter(k -> k.getId().equals(dto.getId()))
                        .findFirst().orElse(null);
                if (kp != null && kp.getParentId() == null) {
                    roots.add(dto);
                } else if (kp != null) {
                    KnowledgePointSimpleDTO parent = dtoMap.get(kp.getParentId());
                    if (parent != null) {
                        parent.getChildren().add(dto);
                    }
                }
            }
            
            subjectTree.setChildren(roots);
            result.add(subjectTree);
        }
        
        return result;
    }

    // 根据ID查询
    public KnowledgePointDTO getById(Long id) {
        return knowledgePointRepository.findById(id).map(this::toDTO).orElse(null);
    }

    // 新增
    @Transactional
    public KnowledgePointDTO create(KnowledgePointDTO dto) {
        KnowledgePoint kp = new KnowledgePoint();
        BeanUtils.copyProperties(dto, kp);
        kp.setCreatedAt(LocalDateTime.now());
        kp = knowledgePointRepository.save(kp);
        return toDTO(kp);
    }

    // 更新
    @Transactional
    public KnowledgePointDTO update(Long id, KnowledgePointDTO dto) {
        KnowledgePoint kp = knowledgePointRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("知识点不存在"));
        BeanUtils.copyProperties(dto, kp, "id", "createdAt");
        kp = knowledgePointRepository.save(kp);
        return toDTO(kp);
    }

    // 删除
    @Transactional
    public void delete(Long id) {
        knowledgePointRepository.deleteById(id);
    }

    // 批量删除
    @Transactional
    public void deleteBatch(List<Long> ids) {
        knowledgePointRepository.deleteAllById(ids);
    }

    // 批量导入
    @Transactional
    public BatchImportResultDTO importFromExcel(MultipartFile file) {
        List<String> errors = new ArrayList<>();
        int success = 0;
        int total = 0;
        
        try (Workbook workbook = getWorkbook(file)) {
            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();
            
            for (int i = 1; i <= lastRowNum; i++) {
                total++;
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                try {
                    // 列顺序: 0=subject_id, 1=name, 2=difficulty, 3=exam_weight, 4=parent_id
                    Long subjectId = getCellValueAsLong(row.getCell(0));
                    String name = getCellValueAsString(row.getCell(1));
                    
                    if (subjectId == null || name.isEmpty()) {
                        errors.add("第" + (i+1) + "行: 科目ID和名称不能为空");
                        continue;
                    }
                    
                    if (knowledgePointRepository.findBySubjectIdAndName(subjectId, name).isPresent()) {
                        errors.add("第" + (i+1) + "行: 已存在科目ID为" + subjectId + "且名称为'" + name + "'的知识点");
                        continue;
                    }
                    
                    KnowledgePoint kp = new KnowledgePoint();
                    kp.setSubjectId(subjectId);
                    kp.setName(name);
                    kp.setDifficulty(getCellValueAsInteger(row.getCell(2), 3));
                    kp.setExamWeight(getCellValueAsInteger(row.getCell(3), 1));
                    kp.setParentId(getCellValueAsLong(row.getCell(4)));
                    kp.setCreatedAt(LocalDateTime.now());
                    
                    knowledgePointRepository.save(kp);
                    success++;
                } catch (Exception e) {
                    errors.add("第" + (i+1) + "行: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("导入失败: " + e.getMessage());
        }
        
        return new BatchImportResultDTO(total, success, total - success, errors);
    }

    private Long getCellValueAsLong(Cell cell) {
        String value = getCellValueAsString(cell);
        return value.isEmpty() ? null : Long.parseLong(value);
    }

    private Integer getCellValueAsInteger(Cell cell, Integer defaultValue) {
        String value = getCellValueAsString(cell);
        if (value.isEmpty()) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    private Workbook getWorkbook(MultipartFile file) throws Exception {
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new RuntimeException("文件名不能为空");
        }
        
        if (fileName.endsWith(".xlsx")) {
            return new XSSFWorkbook(file.getInputStream());
        } else if (fileName.endsWith(".xls")) {
            return new HSSFWorkbook(file.getInputStream());
        } else {
            throw new RuntimeException("不支持的文件格式，请上传 .xls 或 .xlsx 文件");
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                // 数字转字符串，去掉小数点
                double numValue = cell.getNumericCellValue();
                if (numValue == (long) numValue) {
                    return String.valueOf((long) numValue);
                }
                return String.valueOf(numValue).trim();
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    return cell.getStringCellValue();
                }
            default:
                return "";
        }
    }
    
    //导出导入模板
    public void exportTemplate(HttpServletResponse response) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("知识点导入模板");
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            
            // 创建表头行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"科目ID*", "知识点名称*", "难度(1-5)", "考试权重(1-5)", "父级知识点ID"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // 添加说明行
            Row tipRow = sheet.createRow(1);
            tipRow.createCell(0).setCellValue("必填，数字");
            tipRow.createCell(1).setCellValue("必填");
            tipRow.createCell(2).setCellValue("1-5，默认3");
            tipRow.createCell(3).setCellValue("1-5，默认1");
            tipRow.createCell(4).setCellValue("选填，数字");
            
            
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
                    "attachment; filename=knowledge_point_template_" + System.currentTimeMillis() + ".xlsx");
            
            workbook.write(response.getOutputStream());
            response.getOutputStream().flush();
            
        } catch (Exception e) {
            throw new RuntimeException("导出模板失败: " + e.getMessage(), e);
        }
    }

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

    private KnowledgePointDTO toDTO(KnowledgePoint kp) {
        KnowledgePointDTO dto = new KnowledgePointDTO();
        BeanUtils.copyProperties(kp, dto);
        
        if (kp.getSubjectId() != null) {
            subjectRepository.findById(kp.getSubjectId())
                    .ifPresent(s -> dto.setSubjectName(s.getName()));
        }
        if (kp.getParentId() != null) {
            knowledgePointRepository.findById(kp.getParentId())
                    .ifPresent(p -> dto.setParentName(p.getName()));
        }
        return dto;
    }
}