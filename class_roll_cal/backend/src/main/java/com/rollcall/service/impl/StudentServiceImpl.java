package com.rollcall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rollcall.entity.Student;
import com.rollcall.mapper.StudentMapper;
import com.rollcall.service.StudentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 学生 Service 实现
 */
@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {

    @Override
    public Page<Student> page(int current, int size, String keyword) {
        Page<Student> page = new Page<>(current, size);
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Student::getStudentNo, keyword)
                              .or()
                              .like(Student::getName, keyword));
        }
        wrapper.orderByAsc(Student::getStudentNo);
        return this.page(page, wrapper);
    }

    @Override
    public Student getById(Long id) {
        return this.baseMapper.selectById(id);
    }

    @Override
    public Student add(Student student) {
        // 默认值
        if (student.getCallCount() == null) {
            student.setCallCount(0);
        }
        if (student.getAnswerCount() == null) {
            student.setAnswerCount(0);
        }
        if (student.getEnabled() == null) {
            student.setEnabled(1);
        }
        this.save(student);
        return student;
    }

    @Override
    @Transactional
    public List<Student> addBatch(List<Student> students) {
        students.forEach(s -> {
            if (!StringUtils.hasText(s.getStudentNo())) {
                throw new IllegalArgumentException("学号不能为空");
            }
            if (!StringUtils.hasText(s.getName())) {
                throw new IllegalArgumentException("姓名不能为空");
            }
            if (s.getCallCount() == null) s.setCallCount(0);
            if (s.getAnswerCount() == null) s.setAnswerCount(0);
            if (s.getEnabled() == null) s.setEnabled(1);
        });
        this.saveBatch(students);
        return students;
    }

    @Override
    public Student update(Student student) {
        // 只允许修改基本信息，不修改统计字段
        LambdaUpdateWrapper<Student> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Student::getId, student.getId())
               .set(Student::getStudentNo, student.getStudentNo())
               .set(Student::getName, student.getName())
               .set(Student::getEnabled, student.getEnabled());
        this.update(wrapper);
        return this.getById(student.getId());
    }

    @Override
    public void delete(Long id) {
        this.removeById(id);
    }

    @Override
    public Student toggleEnabled(Long id) {
        Student student = this.getById(id);
        if (student != null) {
            student.setEnabled(student.getEnabled() == 1 ? 0 : 1);
            this.updateById(student);
        }
        return student;
    }
}
