package com.rollcall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rollcall.common.Result;
import com.rollcall.entity.Student;
import com.rollcall.service.StudentService;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 学生管理 Controller
 */
@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    /**
     * 分页查询学生列表
     */
    @GetMapping
    public Result<Page<Student>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.ok(studentService.page(page, size, keyword));
    }

    /**
     * 根据ID查询学生
     */
    @GetMapping("/{id}")
    public Result<Student> get(@PathVariable Long id) {
        Student student = studentService.getById(id);
        if (student == null) {
            return Result.error(404, "学生不存在");
        }
        return Result.ok(student);
    }

    /**
     * 新增学生
     */
    @PostMapping
    public Result<Student> add(@Valid @RequestBody Student student) {
        return Result.ok(studentService.add(student));
    }

    /**
     * 批量新增学生
     */
    @PostMapping("/batch")
    public Result<List<Student>> addBatch(@Valid @RequestBody List<Student> students) {
        return Result.ok(studentService.addBatch(students));
    }

    /**
     * 更新学生信息
     */
    @PutMapping("/{id}")
    public Result<Student> update(@PathVariable Long id, @Valid @RequestBody Student student) {
        student.setId(id);
        return Result.ok(studentService.update(student));
    }

    /**
     * 删除学生
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        studentService.delete(id);
        return Result.ok();
    }

    /**
     * 切换启用/禁用状态
     */
    @PutMapping("/{id}/toggle")
    public Result<Student> toggleEnabled(@PathVariable Long id) {
        return Result.ok(studentService.toggleEnabled(id));
    }
}
