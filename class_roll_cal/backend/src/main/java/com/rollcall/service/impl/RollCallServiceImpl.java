package com.rollcall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rollcall.dto.RollCallResult;
import com.rollcall.entity.Student;
import com.rollcall.mapper.StudentMapper;
import com.rollcall.service.RollCallService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

/**
 * 点名 Service 实现
 * <p>
 * 注意：当前设计为单用户单教室场景，实例状态存储在内存中。
 * 如需多用户支持，应将轮次状态迁移到 Redis 或数据库。
 */
@Service
public class RollCallServiceImpl implements RollCallService {

    private final StudentMapper studentMapper;

    /** 本轮已点名的学生ID（用于避免同一轮重复点名） */
    private final ConcurrentLinkedDeque<Long> calledStudentIds = new ConcurrentLinkedDeque<>();

    /** 本轮实际答错人数 */
    private volatile int wrongCount = 0;

    /** 阈值（volatile 保证多线程可见性） */
    private volatile int threshold = 3;

    public RollCallServiceImpl(StudentMapper studentMapper) {
        this.studentMapper = studentMapper;
    }

    @Override
    public synchronized RollCallResult rollCall(int n) {
        if (n > 0) {
            this.threshold = n;
        }
        return doRollCall();
    }

    @Override
    public synchronized RollCallResult markCorrect(Long studentId) {
        if (!calledStudentIds.contains(studentId)) {
            throw new IllegalArgumentException("该学生不在当前轮次点名列表中");
        }
        Student student = studentMapper.selectById(studentId);
        if (student != null) {
            int answerCount = student.getAnswerCount() != null ? student.getAnswerCount() : 0;
            student.setAnswerCount(answerCount + 1);
            studentMapper.updateById(student);
        }
        resetRound();
        return getStatus();
    }

    @Override
    public synchronized RollCallResult markWrong(Long studentId) {
        if (!calledStudentIds.contains(studentId)) {
            throw new IllegalArgumentException("该学生不在当前轮次点名列表中");
        }
        wrongCount++;
        return getStatus();
    }

    @Override
    public RollCallResult getStatus() {
        RollCallResult result = new RollCallResult();
        result.setCurrentRoundCount(calledStudentIds.size());
        result.setThreshold(threshold);
        result.setCalledStudentIds(new ArrayList<>(calledStudentIds));
        result.setCurrentWrongCount(wrongCount);
        result.setHighScoreMode(wrongCount >= threshold);
        return result;
    }

    @Override
    public void resetRound() {
        calledStudentIds.clear();
        wrongCount = 0;
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

        boolean highScoreMode = wrongCount >= threshold;

        // 排除本轮已点名的学生（避免重复点同一个人）
        Set<Long> calledIds = new HashSet<>(calledStudentIds);
        List<Student> available = allEnabled.stream()
                .filter(s -> !calledIds.contains(s.getId()))
                .collect(Collectors.toList());

        boolean fullRoundExhausted = false;
        if (available.isEmpty()) {
            // 所有学生本轮都被点过了，重新开始一轮
            fullRoundExhausted = true;
            available = new ArrayList<>(allEnabled);
        }

        Student picked;
        if (highScoreMode) {
            picked = pickFromHighScore(available);
        } else {
            picked = pickFromLowCallCount(available);
        }

        // 更新学生点名次数（空值保护）
        int currentCallCount = picked.getCallCount() != null ? picked.getCallCount() : 0;
        picked.setCallCount(currentCallCount + 1);
        studentMapper.updateById(picked);

        // 记录本轮点名
        calledStudentIds.add(picked.getId());

        result.setStudent(picked);
        result.setCurrentRoundCount(calledStudentIds.size());
        result.setCurrentWrongCount(wrongCount);
        result.setThreshold(threshold);
        result.setCalledStudentIds(new ArrayList<>(calledStudentIds));
        result.setHighScoreMode(highScoreMode);
        result.setFullRoundExhausted(fullRoundExhausted);
        return result;
    }

    /**
     * 从 call_count 最小的学生中随机选一个
     */
    private Student pickFromLowCallCount(List<Student> available) {
        int minCount = available.stream()
                .mapToInt(s -> s.getCallCount() != null ? s.getCallCount() : 0)
                .min()
                .orElse(0);
        List<Student> candidates = available.stream()
                .filter(s -> (s.getCallCount() != null ? s.getCallCount() : 0) == minCount)
                .collect(Collectors.toList());
        Collections.shuffle(candidates);
        return candidates.get(0);
    }

    /**
     * 从 answer_count 最高的学生中随机选一个
     */
    private Student pickFromHighScore(List<Student> available) {
        int maxAnswers = available.stream()
                .mapToInt(s -> s.getAnswerCount() != null ? s.getAnswerCount() : 0)
                .max()
                .orElse(0);
        List<Student> candidates = available.stream()
                .filter(s -> (s.getAnswerCount() != null ? s.getAnswerCount() : 0) == maxAnswers)
                .collect(Collectors.toList());
        Collections.shuffle(candidates);
        return candidates.get(0);
    }
}
