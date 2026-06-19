package com.example.learningplatform.service;

import com.example.learningplatform.dto.*;
import com.example.learningplatform.entity.LearningResource;
import com.example.learningplatform.repository.LearningResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.example.learningplatform.repository.LearningResourceRepository;

@Service
public class ParseResourceService {

    private static final String CACHE_PREFIX = "preview_data:";
    private static final long CACHE_EXPIRE_MINUTES = 30;

    @Autowired
    private LearningResourceRepository resourceRepository;

    @Autowired
    private AiMatchService aiMatchService;

    @Autowired
    private AiMatchVersionManager versionManager;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 清洗数据接口
     * 接收Excel/CSV文件，清洗数据后返回预览结果（不保存到数据库）
     */
    public ParsePreviewResponse cleanData(MultipartFile file, Long subjectId, String platform, String resourceType) {
        ParsePreviewResponse response = new ParsePreviewResponse();
        List<PreviewResourceDTO> previewData = new ArrayList<>();
        List<String> invalidReasons = new ArrayList<>();
        int totalRows = 0;
        int validRows = 0;

        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new RuntimeException("文件名不能为空");
        }

        try {
            if (filename.toLowerCase().endsWith(".csv")) {
                // 处理CSV文件
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
                    String line;
                    boolean isFirstLine = true;

                    while ((line = reader.readLine()) != null) {
                        totalRows++;
                        if (isFirstLine) {
                            isFirstLine = false;
                            continue;
                        }

                        try {
                            String[] parts = line.split(",");
                            if (parts.length >= 12) {
                                PreviewResourceDTO dto = parseCrawlData(parts, totalRows, subjectId, platform, resourceType);
                                if (dto != null) {
                                    previewData.add(dto);
                                    validRows++;
                                }
                            } else {
                                invalidReasons.add("第" + totalRows + "行: 字段数量不足");
                            }
                        } catch (Exception e) {
                            invalidReasons.add("第" + totalRows + "行: " + e.getMessage());
                        }
                    }
                }
            } else {
                // 处理Excel文件（xlsx格式）
                try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
                    Sheet sheet = workbook.getSheetAt(0);
                    
                    boolean isFirstRow = true;
                    for (Row row : sheet) {
                        if (isFirstRow) {
                            isFirstRow = false;
                            continue;
                        }
                        
                        totalRows++;
                        
                        try {
                            if (row.getLastCellNum() >= 12) {
                                String[] parts = new String[12];
                                for (int i = 0; i < 12; i++) {
                                    Cell cell = row.getCell(i);
                                    parts[i] = cell != null ? getCellValueAsString(cell).trim() : "";
                                }
                                
                                PreviewResourceDTO dto = parseCrawlData(parts, totalRows, subjectId, platform, resourceType);
                                if (dto != null) {
                                    previewData.add(dto);
                                    validRows++;
                                }
                            } else {
                                invalidReasons.add("第" + totalRows + "行: 字段数量不足");
                            }
                        } catch (Exception e) {
                            invalidReasons.add("第" + totalRows + "行: " + e.getMessage());
                        }
                    }
                }
            }

            // 生成缓存Key
            String cacheKey = UUID.randomUUID().toString();
            String redisKey = CACHE_PREFIX + cacheKey;
            redisTemplate.opsForValue().set(redisKey, previewData, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);

            response.setTotalRows(totalRows - 1); // 减去表头
            response.setValidRows(validRows);
            response.setInvalidRows((totalRows - 1) - validRows);
            response.setInvalidReasons(invalidReasons);
            response.setPreviewData(previewData);

            response.setCacheKey(cacheKey);

        } catch (Exception e) {
            throw new RuntimeException("文件解析失败: " + e.getMessage(), e);
        }

        return response;
    }

    /**
     * 解析爬虫数据行
     */
    private PreviewResourceDTO parseCrawlData(String[] parts, int rowNum, Long subjectId, String platform, String resourceType) {
        PreviewResourceDTO dto = new PreviewResourceDTO();
        
        // 爬虫字段映射
        // parts[0] = web_scraper_order (忽略)
        // parts[1] = web_scraper_start_url (忽略)
        String title = parts.length > 2 ? parts[2].trim() : "";
        String author = parts.length > 3 ? parts[3].trim() : "";
        String views = parts.length > 4 ? cleanViews(parts[4].trim()) : "";
        String publishTime = parts.length > 5 ? cleanPublishTime(parts[5].trim()) : "";
        String reviews = parts.length > 6 ? cleanReviews(parts[6].trim()) : "";
        String duration = parts.length > 7 ? cleanDuration(parts[7].trim()) : "";
        String link = parts.length > 8 ? parts[8].trim() : "";
        String topic = parts.length > 9 ? parts[9].trim() : "";
        String classhours = parts.length > 10 ? cleanClassHours(parts[10].trim()) : "";
        String image = parts.length > 11 ? parts[11].trim() : "";

        // 必填字段校验
        if (title.isEmpty() || link.isEmpty()) {
            return null; // 返回null表示无效行
        }

        dto.setRowIndex(rowNum);
        dto.setPlatform(platform);
        dto.setTitle(title);
        dto.setResourceUrl(link);
        dto.setResourceType(resourceType);
        dto.setAuthor(author);
        dto.setViews(views);
        dto.setReviewCount(reviews.isEmpty() ? null : Integer.parseInt(reviews));
        dto.setPublishTime(publishTime);
        dto.setDuration(duration.isEmpty() ? null : Integer.parseInt(duration));
        dto.setTopic(topic);
        dto.setClassHours(classhours.isEmpty() ? null : Integer.parseInt(classhours));
        dto.setImageUrl(image);
        dto.setSubjectId(subjectId);
        dto.setCanModify(true);

        return dto;
    }

    /**
     * 清洗观看次数："120万" → "1200000"
     */
    private String cleanViews(String views) {
        if (views == null || views.isEmpty()) return "";
        views = views.replaceAll("[,，\\s]", "");
        if (views.contains("万")) {
            return views.replace("万", "0000");
        }
        if (views.contains("亿")) {
            return views.replace("亿", "00000000");
        }
        return views;
    }

    /**
     * 清洗评论数：提取数字部分
     */
    private String cleanReviews(String reviews) {
        if (reviews == null || reviews.isEmpty()) return "";
        reviews = reviews.replaceAll("[,，\\s]", "");
        if (reviews.contains("万")) {
            return String.valueOf((int)(Double.parseDouble(reviews.replace("万", "")) * 10000));
        }
        return reviews.replaceAll("[^0-9]", "");
    }

    /**
     * 清洗时长："45:30" → 45（分钟）
     */
    private String cleanDuration(String duration) {
        if (duration == null || duration.isEmpty()) return "";
        String[] parts = duration.split(":");
        if (parts.length == 2) {
            return parts[0];
        } else if (parts.length == 3) {
            // "01:23:45" → 83分钟
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            return String.valueOf(hours * 60 + minutes);
        }
        return duration.replaceAll("[^0-9]", "");
    }

    /**
     * 清洗课时：提取数字部分
     */
    private String cleanClassHours(String classhours) {
        if (classhours == null || classhours.isEmpty()) return "";
        return classhours.replaceAll("[^0-9]", "");
    }
    
    /**
     * 清洗发布时间：去除点、空格等前缀，提取时间部分
     * 示例："· 03-24" → "03-24"
     */
    private String cleanPublishTime(String publishTime) {
        if (publishTime == null || publishTime.isEmpty()) return "";
        publishTime = publishTime.trim();
        publishTime = publishTime.replaceAll("^[·.\\s]+", "");
        return publishTime.trim();
    }

    /**
     * AI处理接口
     * 调用AI生成知识点匹配、标签、置信度、描述等信息，然后保存到数据库
     */
    public AiProcessResponse processWithAi(String cacheKey, Long subjectId) {
        AiProcessResponse response = new AiProcessResponse();
        int successCount = 0;
        int failedCount = 0;
        List<String> errors = new ArrayList<>();

        // 从Redis缓存获取数据
        String redisKey = CACHE_PREFIX + cacheKey;
        List<PreviewResourceDTO> previewData = (List<PreviewResourceDTO>) redisTemplate.opsForValue().get(redisKey);
        if (previewData == null || previewData.isEmpty()) {
            throw new RuntimeException("缓存数据不存在或已过期");
        }

        for (PreviewResourceDTO dto : previewData) {
            try {
                // 调用AI生成匹配结果
                AiMatchResult aiResult = aiMatchService.processResource(
                    dto.getTitle(),
                    dto.getTopic(),
                    dto.getResourceUrl(),
                    dto.getPlatform(),
                    subjectId,
                    dto.getResourceType(),
                    dto.getDuration(),
                    dto.getClassHours()
                );

                // 创建资源实体
                LearningResource resource = new LearningResource();
                resource.setSubjectId(subjectId);
                resource.setKnowledgePointId(aiResult.getKnowledgePointId());
                resource.setPlatform(dto.getPlatform());
                resource.setTitle(dto.getTitle());
                resource.setAuthor(dto.getAuthor());
                resource.setViews(dto.getViews());
                resource.setReviewCount(dto.getReviewCount());
                resource.setPublishTime(dto.getPublishTime());
                resource.setTopic(dto.getTopic());
                resource.setClassHours(dto.getClassHours());
                resource.setImageUrl(dto.getImageUrl());
                resource.setAiMatchConfidence(aiResult.getConfidence());
                resource.setAiMatchInfo(aiResult.getMatchInfo());
                resource.setTags(aiResult.getTags());
                resource.setDescription(aiResult.getDescription());
                resource.setAiMatchVersion(aiResult.getAiMatchVersion() != null
                        ? aiResult.getAiMatchVersion() : versionManager.getCurrentVersion());
                resource.setResourceUrl(dto.getResourceUrl());
                resource.setResourceType(dto.getResourceType());
                resource.setDuration(dto.getDuration());
                resource.setCreatedAt(LocalDateTime.now());

                // 检查重复
                if (!resourceRepository.existsByResourceUrl(dto.getResourceUrl())) {
                    resourceRepository.save(resource);
                    successCount++;
                } else {
                    errors.add("资源链接已存在: " + dto.getResourceUrl());
                    failedCount++;
                }

            } catch (Exception e) {
                failedCount++;
                errors.add("处理失败 [" + dto.getTitle() + "]: " + e.getMessage());
            }
        }

        // 清理Redis缓存
        redisTemplate.delete(redisKey);

        response.setSuccessCount(successCount);
        response.setFailedCount(failedCount);
        response.setErrors(errors);

        return response;
    }
    
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double value = cell.getNumericCellValue();
                    if (value == Math.floor(value)) {
                        return String.valueOf((long) value);
                    } else {
                        return String.valueOf(value);
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
            case BLANK:
                return "";
            default:
                return "";
        }
    }
}