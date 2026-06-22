package com.rollcall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rollcall.common.Result;
import com.rollcall.dto.StatisticsDTO;
import com.rollcall.entity.Student;
import com.rollcall.mapper.StudentMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 统计 Controller
 */
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StudentMapper studentMapper;

    public StatisticsController(StudentMapper studentMapper) {
        this.studentMapper = studentMapper;
    }

    /**
     * 获取统计数据
     */
    @GetMapping
    public Result<StatisticsDTO> getStatistics() {
        List<Student> allStudents = studentMapper.selectList(null);

        StatisticsDTO dto = new StatisticsDTO();
        dto.setTotalStudents(allStudents.size());
        dto.setEnabledStudents(allStudents.stream().filter(s -> s.getEnabled() != null && s.getEnabled() == 1).count());

        long totalCalls = allStudents.stream().mapToLong(s -> s.getCallCount() != null ? s.getCallCount() : 0).sum();
        long totalAnswers = allStudents.stream().mapToLong(s -> s.getAnswerCount() != null ? s.getAnswerCount() : 0).sum();
        dto.setTotalCallCount(totalCalls);
        dto.setTotalAnswerCount(totalAnswers);
        dto.setOverallRate(totalCalls > 0 ? (double) totalAnswers / totalCalls : 0);

        List<StatisticsDTO.StudentStat> details = allStudents.stream()
                .map(StatisticsDTO.StudentStat::from)
                .sorted((a, b) -> Double.compare(b.getRate(), a.getRate()))
                .collect(Collectors.toList());
        dto.setDetails(details);

        return Result.ok(dto);
    }
}
