package com.rollcall.dto;

import com.rollcall.entity.Student;
import java.util.List;

/**
 * 统计 DTO
 */
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

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getStudentNo() { return studentNo; }
        public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getCallCount() { return callCount; }
        public void setCallCount(int callCount) { this.callCount = callCount; }
        public int getAnswerCount() { return answerCount; }
        public void setAnswerCount(int answerCount) { this.answerCount = answerCount; }
        public double getRate() { return rate; }
        public void setRate(double rate) { this.rate = rate; }
    }

    public long getTotalStudents() { return totalStudents; }
    public void setTotalStudents(long totalStudents) { this.totalStudents = totalStudents; }
    public long getEnabledStudents() { return enabledStudents; }
    public void setEnabledStudents(long enabledStudents) { this.enabledStudents = enabledStudents; }
    public long getTotalCallCount() { return totalCallCount; }
    public void setTotalCallCount(long totalCallCount) { this.totalCallCount = totalCallCount; }
    public long getTotalAnswerCount() { return totalAnswerCount; }
    public void setTotalAnswerCount(long totalAnswerCount) { this.totalAnswerCount = totalAnswerCount; }
    public double getOverallRate() { return overallRate; }
    public void setOverallRate(double overallRate) { this.overallRate = overallRate; }
    public List<StudentStat> getDetails() { return details; }
    public void setDetails(List<StudentStat> details) { this.details = details; }
}
