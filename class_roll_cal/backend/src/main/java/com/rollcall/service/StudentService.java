package com.rollcall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rollcall.entity.Student;

import java.util.List;

/**
 * 学生 Service 接口
 */
public interface StudentService {

    /** 分页查询 */
    Page<Student> page(int current, int size, String keyword);

    /** 根据ID查询 */
    Student getById(Long id);

    /** 新增学生 */
    Student add(Student student);

    /** 批量新增学生 */
    List<Student> addBatch(List<Student> students);

    /** 更新学生 */
    Student update(Student student);

    /** 删除学生 */
    void delete(Long id);

    /** 切换启用/禁用状态 */
    Student toggleEnabled(Long id);
}
