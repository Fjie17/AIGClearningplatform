package com.example.learningplatform.controller;

import com.example.learningplatform.dto.PageResult;
import com.example.learningplatform.dto.Result;
import com.example.learningplatform.dto.SubjectAssessmentQuestionDTO;
import com.example.learningplatform.service.SubjectAssessmentQuestionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/assessment-questions")
public class SubjectAssessmentQuestionController {

    @Autowired
    private SubjectAssessmentQuestionService questionService;

    /**
     * 分页查询题目列表
     */
    @GetMapping("/list")
    public Result<PageResult<SubjectAssessmentQuestionDTO>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) String questionCode) {
        return Result.success(questionService.page(pageNum, pageSize, subjectId, questionCode));
    }

    /**
     * 获取所有题目
     */
    @GetMapping("/all")
    public Result<List<SubjectAssessmentQuestionDTO>> all(
            @RequestParam(required = false) Long subjectId) {
        return Result.success(questionService.listAll(subjectId));
    }

    /**
     * 获取题目详情
     */
    @GetMapping("/detail/{id}")
    public Result<SubjectAssessmentQuestionDTO> detail(@PathVariable Long id) {
        SubjectAssessmentQuestionDTO dto = questionService.getById(id);
        return dto != null ? Result.success(dto) : Result.error("题目不存在");
    }

    /**
     * 新增题目
     */
    @PostMapping("/add")
    public Result<SubjectAssessmentQuestionDTO> add(@Valid @RequestBody SubjectAssessmentQuestionDTO dto) {
        return Result.success("创建成功", questionService.create(dto));
    }

    /**
     * 修改题目
     */
    @PutMapping("/update/{id}")
    public Result<SubjectAssessmentQuestionDTO> update(@PathVariable Long id, @Valid @RequestBody SubjectAssessmentQuestionDTO dto) {
        return Result.success("更新成功", questionService.update(id, dto));
    }

    /**
     * 删除题目
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        questionService.delete(id);
        return Result.success("删除成功", null);
    }

    /**
     * 批量删除题目
     */
    @DeleteMapping("/delete/batch")
    public Result<Void> deleteBatch(@RequestBody List<Long> ids) {
        questionService.deleteBatch(ids);
        return Result.success("批量删除成功", null);
    }
}
