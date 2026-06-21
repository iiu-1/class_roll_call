package com.rollcall.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 导入结果 DTO
 */
@Data
public class ImportResult {

    /** 总行数 */
    private int total;

    /** 成功导入数 */
    private int success;

    /** 失败数 */
    private int fail;

    /** 错误详情列表 */
    private List<String> errors = new ArrayList<>();

    public void addError(String error) {
        this.errors.add(error);
        this.fail++;
    }
}
