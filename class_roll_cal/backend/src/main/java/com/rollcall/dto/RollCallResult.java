package com.rollcall.dto;

import com.rollcall.entity.Student;
import java.util.ArrayList;
import java.util.List;

/**
 * 点名结果 DTO
 */
public class RollCallResult {

    /** 本轮被点名学生 */
    private Student student;

    /** 本轮问题已点名次数 */
    private int currentRoundCount;

    /** 本轮问题已答错人数 */
    private int currentWrongCount;

    /** 阈值 n */
    private int threshold;

    /** 本轮已点名学生列表 */
    private List<Long> calledStudentIds = new ArrayList<>();

    /** 是否已触发高分补抽模式 */
    private boolean highScoreMode;

    // ====== getters & setters ======

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public int getCurrentRoundCount() { return currentRoundCount; }
    public void setCurrentRoundCount(int currentRoundCount) { this.currentRoundCount = currentRoundCount; }

    public int getCurrentWrongCount() { return currentWrongCount; }
    public void setCurrentWrongCount(int currentWrongCount) { this.currentWrongCount = currentWrongCount; }

    public int getThreshold() { return threshold; }
    public void setThreshold(int threshold) { this.threshold = threshold; }

    public List<Long> getCalledStudentIds() { return calledStudentIds; }
    public void setCalledStudentIds(List<Long> calledStudentIds) { this.calledStudentIds = calledStudentIds; }

    public boolean isHighScoreMode() { return highScoreMode; }
    public void setHighScoreMode(boolean highScoreMode) { this.highScoreMode = highScoreMode; }
}
