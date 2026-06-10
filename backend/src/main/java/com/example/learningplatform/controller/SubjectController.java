package com.example.learningplatform.controller;

import com.example.learningplatform.dto.*;
import com.example.learningplatform.service.SubjectService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/subjects")
public class SubjectController {

    @Autowired
    private SubjectService subjectService;

    /**
     * 分页查询科目列表
     */
    @GetMapping("/list")
    public Result<PageResult<SubjectDTO>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String name,      // 对应前端 queryParams.name
            @RequestParam(required = false) String category,  // 对应 queryParams.category
            @RequestParam(required = false) String subCategory // 对应 queryParams.subCategory
    ) {
        // 这里的参数名必须和前端 axios 发出的 query 键名一致
        return Result.success(subjectService.page(pageNum, pageSize, name, category, subCategory));
    }

    /**
     * 获取全部科目（用于下拉框）
     */
    @GetMapping("/all")
    public Result<List<SubjectDTO>> all() {
        return Result.success(subjectService.listAll());
    }

    /**
     * 获取科目详情
     */
    @GetMapping("/detail/{id}")
    public Result<SubjectDTO> detail(@PathVariable Long id) {
        SubjectDTO dto = subjectService.getById(id);
        return dto != null ? Result.success(dto) : Result.error("科目不存在");
    }

    /**
     * 新增科目
     */
    @PostMapping("/add")
    public Result<SubjectDTO> add(@Valid @RequestBody SubjectDTO dto) {
        return Result.success("创建成功", subjectService.create(dto));
    }

    /**
     * 修改科目
     */
    @PutMapping("/update/{id}")
    public Result<SubjectDTO> update(@PathVariable Long id, @Valid @RequestBody SubjectDTO dto) {
        return Result.success("更新成功", subjectService.update(id, dto));
    }

    /**
     * 删除科目
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        subjectService.delete(id);
        return Result.success("删除成功", null);
    }

    /**
     * 批量删除科目
     */
    @DeleteMapping("/delete/batch")
    public Result<Void> deleteBatch(@RequestBody List<Long> ids) {
        subjectService.deleteBatch(ids);
        return Result.success("批量删除成功", null);
    }
    
    //导入
    @PostMapping("/import")
    public Result<BatchImportResultDTO> importSubjects(@RequestParam("file") MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename != null && filename.toLowerCase().endsWith(".csv")) {
            return Result.success(subjectService.importFromCsv(file));
        } else {
            return Result.success(subjectService.importFromExcel(file));
        }
    }
    
    /**
     * 下载导入模板（Excel）
     */
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) {
        subjectService.exportTemplate(response);
    }

    //获取所有科目（用于下拉框）
    @GetMapping("/options")
    public Result<List<SubjectOptionDTO>> options() {
        return Result.success(subjectService.listOptions());
    }
}