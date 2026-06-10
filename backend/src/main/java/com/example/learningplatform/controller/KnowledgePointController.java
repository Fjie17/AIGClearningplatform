package com.example.learningplatform.controller;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.learningplatform.dto.BatchImportResultDTO;
import com.example.learningplatform.dto.KnowledgePointDTO;
import com.example.learningplatform.dto.KnowledgePointSimpleDTO;
import com.example.learningplatform.dto.SubjectKnowledgePointTreeDTO;
import com.example.learningplatform.dto.PageResult;
import com.example.learningplatform.dto.Result;
import com.example.learningplatform.entity.KnowledgePoint;
import com.example.learningplatform.repository.KnowledgePointRepository;
import com.example.learningplatform.service.KnowledgePointService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/knowledge-points")
public class KnowledgePointController {

    private static final Logger logger = Logger.getLogger(KnowledgePointController.class.getName());

    @Autowired
    private KnowledgePointService knowledgePointService;

    @Autowired
    private KnowledgePointRepository knowledgePointRepository;

    /**
     * 分页查询知识点列表
     */
    @GetMapping("/list")
    public Result<PageResult<KnowledgePointDTO>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String subjectName,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) Integer examWeight) {
        return Result.success(knowledgePointService.page(pageNum, pageSize, 
                name, subjectName, difficulty, examWeight));
    }

    /**
     * 获取知识点树形结构
     */
    @GetMapping("/tree")
    public Result<List<KnowledgePointDTO>> tree(@RequestParam Long subjectId) {
        return Result.success(knowledgePointService.tree(subjectId));
    }

    /**
     * 获取知识点简化树形结构（返回所有科目及其知识点，仅返回id和name）
     */
    @GetMapping("/tree/simple")
    public Result<List<SubjectKnowledgePointTreeDTO>> treeSimple() {
        return Result.success(knowledgePointService.treeSimple());
    }

    /**
     * 获取知识点详情
     */
    @GetMapping("/detail/{id}")
    public Result<KnowledgePointDTO> detail(@PathVariable Long id) {
        KnowledgePointDTO dto = knowledgePointService.getById(id);
        return dto != null ? Result.success(dto) : Result.error("知识点不存在");
    }

    /**
     * 新增知识点
     */
    @PostMapping("/add")
    public Result<KnowledgePointDTO> add(@Valid @RequestBody KnowledgePointDTO dto) {
        return Result.success("创建成功", knowledgePointService.create(dto));
    }

    /**
     * 修改知识点
     */
    @PutMapping("/update/{id}")
    public Result<KnowledgePointDTO> update(@PathVariable Long id, 
                                            @Valid @RequestBody KnowledgePointDTO dto) {
        return Result.success("更新成功", knowledgePointService.update(id, dto));
    }

    /**
     * 删除知识点
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        knowledgePointService.delete(id);
        return Result.success("删除成功", null);
    }

    /**
     * 批量删除知识点
     */
    @DeleteMapping("/delete/batch")
    public Result<Void> deleteBatch(@RequestBody List<Long> ids) {
        knowledgePointService.deleteBatch(ids);
        return Result.success("批量删除成功", null);
    }

    /**
     * 批量导入知识点
     */
    @PostMapping("/import")
    public Result<BatchImportResultDTO> importKnowledgePoints(@RequestParam("file") MultipartFile file) {
        return Result.success(knowledgePointService.importFromExcel(file));  // 方法名改变
    }
    
    //下载导入模板（Excel）
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) {
        knowledgePointService.exportTemplate(response);
    }
    
    /**
     * 获取知识点列表（用于AI匹配）
     * 返回格式: [{"id": 79, "name": "高等数学"}, {"id": 81, "name": "考研英语"}...]
     */
    @GetMapping("/simple-list")
    public Result<List<Object>> getSimpleList(@RequestParam Long subjectId) {
        logger.info("调用 /simple-list 接口，subjectId: " + subjectId);
        List<KnowledgePoint> points = knowledgePointRepository.findBySubjectId(subjectId);
        List<Object> result = points.stream()
                .map(kp -> {
                    java.util.Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", kp.getId());
                    map.put("name", kp.getName());
                    return map;
                })
                .collect(Collectors.toList());
        logger.info("/simple-list 接口调用完成，返回知识点数量: " + result.size());
        return Result.success(result);
    }
    
    /**
     * 获取所有知识点列表（用于AI匹配）
     * 返回格式: [{"id": 79, "name": "高等数学"}, {"id": 81, "name": "考研英语"}...]
     */
    @GetMapping("/all-list")
    public Result<List<Object>> getAllList() {
        List<KnowledgePoint> points = knowledgePointRepository.findAll();
        List<Object> result = points.stream()
                .map(kp -> {
                    java.util.Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", kp.getId());
                    map.put("name", kp.getName());
                    map.put("subjectId", kp.getSubjectId());
                    return map;
                })
                .collect(Collectors.toList());
        return Result.success(result);
    }
    
}