package com.rollcall.dto;

import com.rollcall.entity.Student;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 点名结果 DTO
 */
@Data
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

    /** 本轮已点名全部学生，自动重选 */
    private boolean fullRoundExhausted;
}
