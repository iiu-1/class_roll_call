package com.rollcall.dto;

import com.rollcall.entity.Student;
import lombok.Data;

import java.util.List;

/**
 * 统计 DTO
 */
@Data
public class StatisticsDTO {

    /** 学生总数 */
    private long totalStudents;

    /** 启用学生数 */
    private long enabledStudents;

    /** 总点名次数 */
    private long totalCallCount;

    /** 总回答正确次数 */
    private long totalAnswerCount;

    /** 整体正确率 */
    private double overallRate;

    /** 学生明细列表 */
    private List<StudentStat> details;

    @Data
    public static class StudentStat {
        private Long id;
        private String studentNo;
        private String name;
        private int callCount;
        private int answerCount;
        private double rate;

        public static StudentStat from(Student s) {
            StudentStat stat = new StudentStat();
            stat.id = s.getId();
            stat.studentNo = s.getStudentNo();
            stat.name = s.getName();
            stat.callCount = s.getCallCount() != null ? s.getCallCount() : 0;
            stat.answerCount = s.getAnswerCount() != null ? s.getAnswerCount() : 0;
            stat.rate = stat.callCount > 0 ? (double) stat.answerCount / stat.callCount : 0;
            return stat;
        }
    }
}
