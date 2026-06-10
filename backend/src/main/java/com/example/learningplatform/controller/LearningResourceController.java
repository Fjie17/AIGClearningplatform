package com.example.learningplatform.controller;

import com.example.learningplatform.dto.*;
import com.example.learningplatform.service.LearningResourceService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/resources")
public class LearningResourceController {

    @Autowired
    private LearningResourceService resourceService;

    /**
     * 分页查询资源列表（支持筛选）
     */
    @GetMapping("/list")
    public Result<PageResult<LearningResourceDTO>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "12") int pageSize,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long knowledgePointId,
            @RequestParam(required = false) String platform) {
        return Result.success(resourceService.page(pageNum, pageSize, 
                subjectId, knowledgePointId, platform));
    }

    /**
     * 获取资源详情
     */
    @GetMapping("/detail/{id}")
    public Result<LearningResourceDTO> detail(@PathVariable Long id) {
        LearningResourceDTO dto = resourceService.getById(id);
        return dto != null ? Result.success(dto) : Result.error("资源不存在");
    }

    /**
     * 新增资源
     */
    @PostMapping("/add")
    public Result<LearningResourceDTO> add(@Valid @RequestBody LearningResourceDTO dto) {
        return Result.success("创建成功", resourceService.create(dto));
    }

    /**
     * 修改资源
     */
    @PutMapping("/update/{id}")
    public Result<LearningResourceDTO> update(@PathVariable Long id, 
                                              @Valid @RequestBody LearningResourceDTO dto) {
        return Result.success("更新成功", resourceService.update(id, dto));
    }

    /**
     * 删除资源
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        resourceService.delete(id);
        return Result.success("删除成功", null);
    }

    /**
     * 批量删除资源
     */
    @DeleteMapping("/delete/batch")
    public Result<Void> deleteBatch(@RequestBody List<Long> ids) {
        resourceService.deleteBatch(ids);
        return Result.success("批量删除成功", null);
    }

    /**
     * 批量导入资源
     */
    @PostMapping("/import")
    public Result<BatchImportResultDTO> importResources(@RequestParam("file") MultipartFile file) {
        return Result.success(resourceService.importFromExcel(file));
    }
    
    /**
     * 下载导入模板（Excel）
     */
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) {
        resourceService.exportTemplate(response);
    }
}