package com.rollcall.service;

import com.rollcall.dto.RollCallResult;

/**
 * 点名 Service 接口
 */
public interface RollCallService {

    /**
     * 执行点名
     * @param n 阈值：超过 n 个同学未答对时切换高分模式
     */
    RollCallResult rollCall(int n);

    /**
     * 标记点名结果为正确
     */
    RollCallResult markCorrect(Long studentId);

    /**
     * 标记点名结果为错误
     */
    RollCallResult markWrong(Long studentId);

    /**
     * 获取当前轮次状态
     */
    RollCallResult getStatus();

    /**
     * 重置当前轮次（换新问题时调用）
     */
    void resetRound();
}
