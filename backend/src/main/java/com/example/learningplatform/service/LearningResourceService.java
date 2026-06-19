package com.example.learningplatform.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

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
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.learningplatform.dto.BatchImportResultDTO;
import com.example.learningplatform.dto.LearningResourceDTO;
import com.example.learningplatform.dto.PageResult;
import com.example.learningplatform.dto.ParseResourceRequest;
import com.example.learningplatform.entity.KnowledgePoint;
import com.example.learningplatform.entity.LearningResource;
import com.example.learningplatform.entity.Subject;
import com.example.learningplatform.repository.KnowledgePointRepository;
import com.example.learningplatform.repository.LearningResourceRepository;
import com.example.learningplatform.repository.SubjectRepository;

import jakarta.servlet.http.HttpServletResponse;


@Service
public class LearningResourceService {

    private static final Logger logger = Logger.getLogger(LearningResourceService.class.getName());

    @Autowired
    private LearningResourceRepository resourceRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private KnowledgePointRepository knowledgePointRepository;

    @Autowired
    private AiMatchService aiMatchService;

    public PageResult<LearningResourceDTO> page(int pageNum, int pageSize, 
                                                Long subjectId, Long knowledgePointId, String platform) {
        Page<LearningResource> page = resourceRepository.searchResources(
                subjectId, knowledgePointId, platform,
                PageRequest.of(pageNum - 1, pageSize, Sort.by("id").descending())
        );
        
        List<LearningResourceDTO> list = page.getContent().stream().map(this::toDTO).toList();
        return new PageResult<>(list, page.getTotalElements(), pageNum, pageSize);
    }

    public LearningResourceDTO getById(Long id) {
        return resourceRepository.findById(id).map(this::toDTO).orElse(null);
    }

    @Transactional
    public LearningResourceDTO create(LearningResourceDTO dto) {
        if (resourceRepository.existsByResourceUrl(dto.getResourceUrl())) {
            throw new RuntimeException("资源链接已存在");
        }
        LearningResource resource = new LearningResource();
        BeanUtils.copyProperties(dto, resource);
        resource.setCreatedAt(LocalDateTime.now());
        
        // 设置新增字段
        resource.setAuthor(dto.getAuthor());
        resource.setViews(dto.getViews());
        resource.setReviewCount(dto.getReviewCount());
        resource.setPublishTime(dto.getPublishTime());
        resource.setTopic(dto.getTopic());
        resource.setClassHours(dto.getClassHours());
        resource.setImageUrl(dto.getImageUrl());
        resource.setAiMatchConfidence(dto.getAiMatchConfidence());
        resource.setAiMatchInfo(dto.getAiMatchInfo());
        resource.setTags(dto.getTags());
        resource.setDescription(dto.getDescription());
        
        resource = resourceRepository.save(resource);
        return toDTO(resource);
    }

    @Transactional
    public LearningResourceDTO update(Long id, LearningResourceDTO dto) {
        LearningResource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("资源不存在"));
        BeanUtils.copyProperties(dto, resource, "id", "createdAt");
        
        // 更新新增字段
        resource.setAuthor(dto.getAuthor());
        resource.setViews(dto.getViews());
        resource.setReviewCount(dto.getReviewCount());
        resource.setPublishTime(dto.getPublishTime());
        resource.setTopic(dto.getTopic());
        resource.setClassHours(dto.getClassHours());
        resource.setImageUrl(dto.getImageUrl());
        resource.setAiMatchConfidence(dto.getAiMatchConfidence());
        resource.setAiMatchInfo(dto.getAiMatchInfo());
        resource.setTags(dto.getTags());
        resource.setDescription(dto.getDescription());
        
        resource = resourceRepository.save(resource);
        return toDTO(resource);
    }

    @Transactional
    public void delete(Long id) {
        resourceRepository.deleteById(id);
    }

    @Transactional
    public void deleteBatch(List<Long> ids) {
        resourceRepository.deleteAllById(ids);
    }

    @Transactional
    public BatchImportResultDTO importFromExcel(MultipartFile file) {
        List<String> errors = new ArrayList<>();
        int success = 0;
        int total = 0;

        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new RuntimeException("文件名不能为空");
        }

        if (filename.toLowerCase().endsWith(".csv")) {
            return importFromCsv(file);
        } else {
            return importFromExcelFile(file);
        }
    }

    private BatchImportResultDTO importFromExcelFile(MultipartFile file) {
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
                    Long subjectId = getCellValueAsLong(row.getCell(0));
                    Long kpId = getCellValueAsLong(row.getCell(1));
                    String platform = getCellValueAsString(row.getCell(2));
                    String title = getCellValueAsString(row.getCell(3));
                    String url = getCellValueAsString(row.getCell(4));
                    String type = getCellValueAsString(row.getCell(5));
                    Integer duration = getCellValueAsInteger(row.getCell(6), null);
                    
                    // 新增字段
                    String author = getCellValueAsString(row.getCell(7));
                    String views = getCellValueAsString(row.getCell(8));
                    Integer reviewCount = getCellValueAsInteger(row.getCell(9), null);
                    String publishTime = getCellValueAsString(row.getCell(10));
                    String topic = getCellValueAsString(row.getCell(11));
                    Integer classHours = getCellValueAsInteger(row.getCell(12), null);
                    String imageUrl = getCellValueAsString(row.getCell(13));
                    String tags = getCellValueAsString(row.getCell(14));
                    String description = getCellValueAsString(row.getCell(15));

                    if (subjectId == null || title.isEmpty() || url.isEmpty()) {
                        errors.add("第" + (i + 1) + "行: 必填字段不能为空 (科目ID/标题/链接)");
                        continue;
                    }

                    LearningResource resource = new LearningResource();
                    resource.setSubjectId(subjectId);
                    resource.setKnowledgePointId(kpId);
                    resource.setPlatform(platform);
                    resource.setTitle(title);
                    resource.setResourceUrl(url);
                    resource.setResourceType(type.isEmpty() ? "video" : type);
                    resource.setDuration(duration);
                    resource.setCreatedAt(LocalDateTime.now());
                    
                    // 设置新增字段
                    resource.setAuthor(author);
                    resource.setViews(views);
                    resource.setReviewCount(reviewCount);
                    resource.setPublishTime(publishTime);
                    resource.setTopic(topic);
                    resource.setClassHours(classHours);
                    resource.setImageUrl(imageUrl);
                    resource.setTags(tags);
                    resource.setDescription(description);

                    if (!resourceRepository.existsByResourceUrl(url)) {
                        resourceRepository.save(resource);
                        success++;
                    } else {
                        errors.add("第" + (i + 1) + "行: 链接已存在 - " + url);
                    }
                } catch (Exception e) {
                    errors.add("第" + (i + 1) + "行: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Excel导入失败: " + e.getMessage(), e);
        }

        return new BatchImportResultDTO(total, success, total - success, errors);
    }

    private BatchImportResultDTO importFromCsv(MultipartFile file) {
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
                    if (parts.length >= 5) {
                        LearningResource resource = new LearningResource();
                        resource.setSubjectId(Long.parseLong(parts[0].trim()));
                        resource.setKnowledgePointId(parts.length > 1 && !parts[1].isEmpty() ? 
                                Long.parseLong(parts[1].trim()) : null);
                        resource.setPlatform(parts[2].trim());
                        resource.setTitle(parts[3].trim());
                        resource.setResourceUrl(parts[4].trim());
                        resource.setResourceType(parts.length > 5 ? parts[5].trim() : "video");
                        resource.setDuration(parts.length > 6 && !parts[6].isEmpty() ?
                                Integer.parseInt(parts[6].trim()) : null);
                        resource.setCreatedAt(LocalDateTime.now());
                        
                        // 新增字段
                        resource.setAuthor(parts.length > 7 ? parts[7].trim() : null);
                        resource.setViews(parts.length > 8 ? parts[8].trim() : null);
                        resource.setReviewCount(parts.length > 9 && !parts[9].isEmpty() ? 
                                Integer.parseInt(parts[9].trim()) : null);
                        resource.setPublishTime(parts.length > 10 ? parts[10].trim() : null);
                        resource.setTopic(parts.length > 11 ? parts[11].trim() : null);
                        resource.setClassHours(parts.length > 12 && !parts[12].isEmpty() ? 
                                Integer.parseInt(parts[12].trim()) : null);
                        resource.setImageUrl(parts.length > 13 ? parts[13].trim() : null);
                        resource.setTags(parts.length > 14 ? parts[14].trim() : null);
                        resource.setDescription(parts.length > 15 ? parts[15].trim() : null);

                        if (!resourceRepository.existsByResourceUrl(resource.getResourceUrl())) {
                            resourceRepository.save(resource);
                            success++;
                        } else {
                            errors.add("第" + total + "行: 链接已存在");
                        }
                    }
                } catch (Exception e) {
                    errors.add("第" + total + "行: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("CSV导入失败: " + e.getMessage(), e);
        }

        return new BatchImportResultDTO(total - 1, success, total - 1 - success, errors);
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
                    try {
                        return cell.getStringCellValue().trim();
                    } catch (Exception ex) {
                        return "";
                    }
                }
            default:
                return "";
        }
    }

    private Long getCellValueAsLong(Cell cell) {
        String value = getCellValueAsString(cell);
        if (value.isEmpty()) return null;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
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
    
    public void exportTemplate(HttpServletResponse response) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("资源导入模板");
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            
            Row headerRow = sheet.createRow(0);
            String[] headers = {"科目ID*", "知识点ID", "平台", "标题*", "资源链接*", "资源类型", "时长(分钟)",
                    "作者", "观看次数", "评论数", "发布时间", "话题", "课时", "封面图片", "标签", "描述"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            Row tipRow = sheet.createRow(1);
            tipRow.createCell(0).setCellValue("必填，数字");
            tipRow.createCell(1).setCellValue("数字，可不填");
            tipRow.createCell(2).setCellValue("B站/小红书/抖音");
            tipRow.createCell(3).setCellValue("必填");
            tipRow.createCell(4).setCellValue("必填，完整URL");
            tipRow.createCell(5).setCellValue("video/article/note");
            tipRow.createCell(6).setCellValue("数字，可不填");
            tipRow.createCell(7).setCellValue("作者名称");
            tipRow.createCell(8).setCellValue("观看次数");
            tipRow.createCell(9).setCellValue("数字");
            tipRow.createCell(10).setCellValue("发布时间");
            tipRow.createCell(11).setCellValue("话题标签");
            tipRow.createCell(12).setCellValue("数字，课时数");
            tipRow.createCell(13).setCellValue("图片URL");
            tipRow.createCell(14).setCellValue("多个用|分隔");
            tipRow.createCell(15).setCellValue("资源描述");

            
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                int columnWidth = sheet.getColumnWidth(i);
                sheet.setColumnWidth(i, columnWidth + 1024);
            }
            
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", 
                    "attachment; filename=resource_template_" + System.currentTimeMillis() + ".xlsx");
            
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

    private LearningResourceDTO toDTO(LearningResource resource) {
        LearningResourceDTO dto = new LearningResourceDTO();
        BeanUtils.copyProperties(resource, dto);
        
        if (resource.getSubjectId() != null) {
            subjectRepository.findById(resource.getSubjectId())
                    .ifPresent(s -> dto.setSubjectName(s.getName()));
        }
        if (resource.getKnowledgePointId() != null) {
            knowledgePointRepository.findById(resource.getKnowledgePointId())
                    .ifPresent(k -> dto.setKnowledgePointName(k.getName()));
        }
        
        // 手动设置新增字段，确保正确复制
        dto.setAuthor(resource.getAuthor());
        dto.setViews(resource.getViews());
        dto.setReviewCount(resource.getReviewCount());
        dto.setPublishTime(resource.getPublishTime());
        dto.setTopic(resource.getTopic());
        dto.setClassHours(resource.getClassHours());
        dto.setImageUrl(resource.getImageUrl());
        dto.setTags(resource.getTags());
        dto.setDescription(resource.getDescription());
        dto.setAiMatchConfidence(resource.getAiMatchConfidence());
        dto.setAiMatchInfo(resource.getAiMatchInfo());
        dto.setAiMatchVersion(resource.getAiMatchVersion());
        
        return dto;
    }

    @Transactional
    public Long parseResource(ParseResourceRequest request) {
        String resourceUrl = request.getResourceUrl();
        String title = request.getTitle();
        String platform = request.getPlatform();
        List<String> tags = request.getTags();
        String description = request.getDescription();
        
        logger.info(String.format("开始解析资源: platform=%s, title=%s, url=%s", platform, title, resourceUrl));

        if (resourceUrl == null || resourceUrl.trim().isEmpty()) {
            logger.warning("资源链接为空");
            throw new IllegalArgumentException("资源链接不能为空");
        }

        Long matchedSubjectId = request.getSubjectId();
        Long matchedKnowledgePointId = null;

        if (matchedSubjectId == null) {
            logger.info("开始AI匹配科目...");
            matchedSubjectId = aiMatchService.matchSubject(title, tags);
            logger.info(String.format("AI匹配科目结果: subjectId=%s", matchedSubjectId));
            
            if (matchedSubjectId == null) {
                logger.warning("AI匹配科目失败，无法确定资源所属科目");
                throw new RuntimeException("AI匹配科目失败，请手动指定科目ID");
            }
        }

        logger.info("开始AI匹配知识点...");
        matchedKnowledgePointId = aiMatchService.matchKnowledgePoint(matchedSubjectId, title, tags);
        logger.info(String.format("AI匹配知识点结果: knowledgePointId=%s", matchedKnowledgePointId));

        // 计算置信度
        int score = 0;
        if (matchedSubjectId != null) score++;
        if (matchedKnowledgePointId != null) score++;
        if (tags != null && !tags.isEmpty()) score++;
        String confidence = score == 3 ? "high" : (score >= 2 ? "medium" : "low");

        Optional<LearningResource> existingResourceOpt = resourceRepository.findByResourceUrl(resourceUrl);
        
        if (existingResourceOpt.isPresent()) {
            LearningResource existingResource = existingResourceOpt.get();
            logger.info(String.format("资源已存在，更新记录: id=%d", existingResource.getId()));
            
            existingResource.setPlatform(platform);
            existingResource.setTitle(title);
            existingResource.setSubjectId(matchedSubjectId);
            existingResource.setKnowledgePointId(matchedKnowledgePointId);
            existingResource.setDescription(description);
            existingResource.setTags(tags != null ? String.join(",", tags) : null);
            existingResource.setAiMatchConfidence(confidence);
            
            LearningResource updatedResource = resourceRepository.save(existingResource);
            
            logger.info(String.format("资源更新成功: id=%d", updatedResource.getId()));
            return updatedResource.getId();
        }

        LearningResource resource = new LearningResource();
        resource.setPlatform(platform);
        resource.setTitle(title);
        resource.setResourceUrl(resourceUrl);
        resource.setSubjectId(matchedSubjectId);
        resource.setKnowledgePointId(matchedKnowledgePointId);
        resource.setDescription(description);
        resource.setTags(tags != null ? String.join(",", tags) : null);
        resource.setAiMatchConfidence(confidence);
        resource.setCreatedAt(LocalDateTime.now());
        
        LearningResource savedResource = resourceRepository.save(resource);
        logger.info(String.format("资源创建成功: id=%d", savedResource.getId()));
        
        return savedResource.getId();
    }
}