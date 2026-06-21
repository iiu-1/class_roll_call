package com.rollcall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rollcall.dto.RollCallResult;
import com.rollcall.entity.Student;
import com.rollcall.mapper.StudentMapper;
import com.rollcall.service.RollCallService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 点名 Service 实现
 */
@Service
public class RollCallServiceImpl implements RollCallService {

    private final StudentMapper studentMapper;

    /** 当前问题已点名的学生ID列表 */
    private final List<Long> calledStudentIds = new ArrayList<>();

    /** 阈值 */
    private int threshold = 3;

    public RollCallServiceImpl(StudentMapper studentMapper) {
        this.studentMapper = studentMapper;
    }

    @Override
    public RollCallResult rollCall(int n) {
        if (n > 0) {
            this.threshold = n;
        }
        return doRollCall();
    }

    @Override
    public RollCallResult markCorrect(Long studentId) {
        if (calledStudentIds.contains(studentId)) {
            Student student = studentMapper.selectById(studentId);
            if (student != null) {
                student.setAnswerCount(student.getAnswerCount() + 1);
                studentMapper.updateById(student);
            }
        }
        resetRound();
        return getStatus();
    }

    @Override
    public RollCallResult markWrong(Long studentId) {
        if (!calledStudentIds.contains(studentId)) {
            calledStudentIds.add(studentId);
            Student student = studentMapper.selectById(studentId);
            if (student != null) {
                student.setCallCount(student.getCallCount() + 1);
                studentMapper.updateById(student);
            }
        }
        return getStatus();
    }

    @Override
    public RollCallResult getStatus() {
        RollCallResult result = new RollCallResult();
        result.setCurrentRoundCount(calledStudentIds.size());
        result.setThreshold(threshold);
        result.setCalledStudentIds(new ArrayList<>(calledStudentIds));

        // 统计未答对人数（当前轮次所有被点名的都算未答对直到有人答对）
        long wrongCount = countWrongInRound();
        result.setCurrentWrongCount((int) wrongCount);
        result.setHighScoreMode(wrongCount >= threshold);
        return result;
    }

    @Override
    public void resetRound() {
        calledStudentIds.clear();
    }

    /**
     * 执行点名核心逻辑
     */
    private RollCallResult doRollCall() {
        RollCallResult result = new RollCallResult();

        // 获取所有启用的学生
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getEnabled, 1);
        List<Student> allEnabled = studentMapper.selectList(wrapper);

        if (allEnabled.isEmpty()) {
            throw new IllegalStateException("没有可点名的学生，请先导入学生信息");
        }

        int wrongInRound = (int) countWrongInRound();
        boolean highScoreMode = wrongInRound >= threshold;

        // 排除本轮已点名的学生（避免重复点同一个人）
        Set<Long> calledIds = new HashSet<>(calledStudentIds);
        List<Student> available = allEnabled.stream()
                .filter(s -> !calledIds.contains(s.getId()))
                .collect(Collectors.toList());

        if (available.isEmpty()) {
            // 所有学生本轮都被点过了，从所有启用学生中重新选择
            available = new ArrayList<>(allEnabled);
        }

        Student picked;
        if (highScoreMode) {
            // 高分模式：从 answer_count 最高的学生中随机选
            picked = pickFromHighScore(available);
        } else {
            // 正常模式：选 call_count 最小的
            picked = pickFromLowCallCount(available);
        }

        // 更新学生点名次数
        picked.setCallCount(picked.getCallCount() + 1);
        studentMapper.updateById(picked);

        // 记录本轮点名
        calledStudentIds.add(picked.getId());

        result.setStudent(picked);
        result.setCurrentRoundCount(calledStudentIds.size());
        result.setCurrentWrongCount(wrongInRound);
        result.setThreshold(threshold);
        result.setCalledStudentIds(new ArrayList<>(calledStudentIds));
        result.setHighScoreMode(highScoreMode);
        return result;
    }

    /**
     * 从 call_count 最小的学生中随机选一个
     */
    private Student pickFromLowCallCount(List<Student> available) {
        int minCount = available.stream()
                .mapToInt(Student::getCallCount)
                .min()
                .orElse(0);
        List<Student> candidates = available.stream()
                .filter(s -> s.getCallCount() == minCount)
                .collect(Collectors.toList());
        Collections.shuffle(candidates);
        return candidates.get(0);
    }

    /**
     * 从 answer_count 最高的学生中随机选一个
     */
    private Student pickFromHighScore(List<Student> available) {
        int maxAnswers = available.stream()
                .mapToInt(Student::getAnswerCount)
                .max()
                .orElse(0);
        List<Student> candidates = available.stream()
                .filter(s -> s.getAnswerCount() == maxAnswers)
                .collect(Collectors.toList());
        Collections.shuffle(candidates);
        return candidates.get(0);
    }

    /**
     * 统计当前轮次中未答对的人数
     */
    private long countWrongInRound() {
        // 本轮所有被点名的学生中，排除已标记为正确的
        // 目前简化：所有 calledStudentIds 都是未答对的（答对会 reset）
        // 实际逻辑：calledStudentIds 中的学生如果被标记答错会保持在列表中
        return calledStudentIds.size();
    }
}
