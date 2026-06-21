package com.rollcall.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 导入结果 DTO
 */
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

    // ====== getters & setters ======

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public int getSuccess() { return success; }
    public void setSuccess(int success) { this.success = success; }

    public int getFail() { return fail; }
    public void setFail(int fail) { this.fail = fail; }

    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }
}
