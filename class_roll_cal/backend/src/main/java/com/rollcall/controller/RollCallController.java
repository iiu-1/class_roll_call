package com.rollcall.controller;

import com.rollcall.common.Result;
import com.rollcall.dto.RollCallResult;
import com.rollcall.service.RollCallService;
import org.springframework.web.bind.annotation.*;

/**
 * 点名 Controller
 */
@RestController
@RequestMapping("/api/roll-call")
public class RollCallController {

    private final RollCallService rollCallService;

    public RollCallController(RollCallService rollCallService) {
        this.rollCallService = rollCallService;
    }

    /**
     * 执行点名
     * @param n 阈值（默认3），超过 n 个同学未答对时切换高分模式
     */
    @PostMapping
    public Result<RollCallResult> rollCall(@RequestParam(defaultValue = "3") int n) {
        try {
            RollCallResult result = rollCallService.rollCall(n);
            return Result.ok(result);
        } catch (IllegalStateException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 标记学生回答正确
     */
    @PostMapping("/{studentId}/answer")
    public Result<RollCallResult> markAnswer(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "false") boolean correct) {
        if (correct) {
            return Result.ok(rollCallService.markCorrect(studentId));
        } else {
            return Result.ok(rollCallService.markWrong(studentId));
        }
    }

    /**
     * 获取当前轮次点名状态
     */
    @GetMapping("/status")
    public Result<RollCallResult> getStatus() {
        return Result.ok(rollCallService.getStatus());
    }

    /**
     * 重置当前轮次（换下一题）
     */
    @PostMapping("/reset")
    public Result<Void> resetRound() {
        rollCallService.resetRound();
        return Result.ok();
    }
}
