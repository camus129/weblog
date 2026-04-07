package com.fy.weblog.dto;

public class Result<T> {
    private Boolean success;   // true/false
    private String message;    // 错误信息或提示
    private T data;            // 业务数据
    private Long total;        // 分页总数（可选）

    public Result(Boolean success, String message, T data, Long total) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.total = total;
    }

    // 成功静态方法
    public static <T> Result<T> ok(T data) {
        return new Result<>(true, "操作成功", data, null);
    }

    public static <T> Result<T> ok(T data, Long total) {
        return new Result<>(true, "操作成功", data, total);
    }

    public static <T> Result<T> ok(String message, T data) {
        return new Result<>(true, message, data, null);
    }

    // 成功静态方法（只返回消息，不返回数据）
    public static <T> Result<T> ok(String message) {
        return new Result<>(true, message, null, null);
    }

    // 失败静态方法（业务错误）
    public static <T> Result<T> fail(String message) {
        return new Result<>(false, message, null, null);
    }

    // 失败 + 指定错误码（可选，用于前端识别）
    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(false, message, null, null);
    }

    // Getter / Setter
    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public Long getTotal() { return total; }
    public void setTotal(Long total) { this.total = total; }
}
