package com.rollcall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rollcall.dto.ImportResult;
import com.rollcall.entity.Student;
import com.rollcall.mapper.StudentMapper;
import com.rollcall.service.StudentService;
import org.apache.poi.ss.usermodel.*;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

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
    public ImportResult importFromFile(MultipartFile file) {
        ImportResult result = new ImportResult();
        String filename = file.getOriginalFilename();
        if (filename == null) {
            result.addError("文件名为空");
            return result;
        }

        List<Student> students;
        String lowerName = filename.toLowerCase();
        if (lowerName.endsWith(".csv")) {
            students = parseCsv(file, result);
        } else if (lowerName.endsWith(".xlsx") || lowerName.endsWith(".xls")) {
            students = parseExcel(file, result);
        } else {
            result.addError("不支持的文件格式，请上传 .xlsx、.xls 或 .csv 文件");
            return result;
        }

        // 预过滤：去除数据库中已存在及文件内重复的学号
        List<Student> newStudents = students;
        Set<String> seenInFile = new HashSet<>();
        if (!students.isEmpty()) {
            Set<String> existingNos = this.list().stream()
                    .map(Student::getStudentNo)
                    .collect(Collectors.toSet());
            newStudents = new ArrayList<>();
            for (Student s : students) {
                if (existingNos.contains(s.getStudentNo())) {
                    result.addError("学号 " + s.getStudentNo() + " 已存在数据库中，已跳过");
                } else if (!seenInFile.add(s.getStudentNo())) {
                    result.addError("学号 " + s.getStudentNo() + " 在文件中重复，已跳过");
                } else {
                    newStudents.add(s);
                }
            }
        }

        result.setTotal(newStudents.size() + result.getFail());
        if (!newStudents.isEmpty()) {
            try {
                this.saveBatch(newStudents);
                result.setSuccess(newStudents.size());
            } catch (DuplicateKeyException e) {
                // 并发场景下预过滤未能完全消除重复，逐条插入以精确定位
                int successCount = 0;
                for (Student s : newStudents) {
                    try {
                        this.save(s);
                        successCount++;
                    } catch (DuplicateKeyException ignored) {
                        result.addError("学号 " + s.getStudentNo() + " 已被其他请求导入，已跳过");
                    }
                }
                result.setSuccess(successCount);
            }
        }
        return result;
    }

    /**
     * 解析 CSV 文件
     */
    private List<Student> parseCsv(MultipartFile file, ImportResult result) {
        List<Student> students = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                result.addError("CSV 文件为空");
                return students;
            }
            String[] headers = splitCsvLine(headerLine);
            int colSno = findColumn(headers, "学号");
            int colName = findColumn(headers, "姓名");
            if (colSno < 0 || colName < 0) {
                result.addError("CSV 表头需包含「学号」和「姓名」列");
                return students;
            }

            String line;
            int rowNum = 1;
            while ((line = reader.readLine()) != null) {
                rowNum++;
                String[] cells = splitCsvLine(line);
                try {
                    String sno = cells[colSno].trim();
                    String name = cells[colName].trim();
                    if (!StringUtils.hasText(sno) || !StringUtils.hasText(name)) {
                        result.addError("第" + rowNum + "行：学号或姓名为空");
                        continue;
                    }
                    Student s = new Student();
                    s.setStudentNo(sno);
                    s.setName(name);
                    s.setCallCount(0);
                    s.setAnswerCount(0);
                    s.setEnabled(1);
                    students.add(s);
                } catch (Exception e) {
                    result.addError("第" + rowNum + "行：解析失败 - " + e.getMessage());
                }
            }
        } catch (Exception e) {
            result.addError("CSV 文件读取失败：" + e.getMessage());
        }
        return students;
    }

    /**
     * 解析 Excel 文件
     */
    private List<Student> parseExcel(MultipartFile file, ImportResult result) {
        List<Student> students = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet.getLastRowNum() < 1) {
                result.addError("Excel 文件无数据");
                return students;
            }

            // 读取表头
            Row headerRow = sheet.getRow(0);
            String[] headers = new String[headerRow.getLastCellNum()];
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                headers[i] = cell == null ? "" : cell.toString().trim();
            }
            int colSno = findColumn(headers, "学号");
            int colName = findColumn(headers, "姓名");
            if (colSno < 0 || colName < 0) {
                result.addError("Excel 表头需包含「学号」和「姓名」列");
                return students;
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                try {
                    String sno = getCellValue(row, colSno);
                    String name = getCellValue(row, colName);
                    if (!StringUtils.hasText(sno) || !StringUtils.hasText(name)) {
                        result.addError("第" + (i + 1) + "行：学号或姓名为空");
                        continue;
                    }
                    Student s = new Student();
                    s.setStudentNo(sno);
                    s.setName(name);
                    s.setCallCount(0);
                    s.setAnswerCount(0);
                    s.setEnabled(1);
                    students.add(s);
                } catch (Exception e) {
                    result.addError("第" + (i + 1) + "行：解析失败 - " + e.getMessage());
                }
            }
        } catch (Exception e) {
            result.addError("Excel 文件读取失败：" + e.getMessage());
        }
        return students;
    }

    /** 在表头中查找列索引 */
    private int findColumn(String[] headers, String name) {
        for (int i = 0; i < headers.length; i++) {
            if (headers[i] != null && headers[i].contains(name)) {
                return i;
            }
        }
        return -1;
    }

    /** 获取单元格值（使用 DataFormatter 避免数字学号变成 "2021001001.0"） */
    private String getCellValue(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return "";
        // DataFormatter 按单元格原有格式输出，纯数字不会追加 .0
        return new DataFormatter().formatCellValue(cell).trim();
    }

    /**
     * 解析 CSV 行，处理引号包裹的字段
     */
    private String[] splitCsvLine(String line) {
        return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    }

    @Override
    public Student update(Student student) {
        // 只允许修改基本信息，不修改统计字段
        LambdaUpdateWrapper<Student> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Student::getId, student.getId())
               .set(Student::getStudentNo, student.getStudentNo())
               .set(Student::getName, student.getName());
        // enabled 可能未传，仅在有值时更新
        if (student.getEnabled() != null) {
            wrapper.set(Student::getEnabled, student.getEnabled());
        }
        this.update(wrapper);
        return this.getById(student.getId());
    }

    @Override
    public void delete(Long id) {
        this.removeById(id);
    }

    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            this.removeByIds(ids);
        }
    }

    @Override
    public Student toggleEnabled(Long id) {
        Student student = this.getById(id);
        if (student != null) {
            // enabled 为 null 时视为禁用，切换为启用
            int current = student.getEnabled() != null ? student.getEnabled() : 0;
            student.setEnabled(current == 1 ? 0 : 1);
            this.updateById(student);
        }
        return student;
    }
}
