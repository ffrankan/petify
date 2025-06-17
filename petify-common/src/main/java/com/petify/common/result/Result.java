package com.petify.common.result;

/**
 * 统一响应结果类 - 使用Java 17 Record特性
 * 自动生成constructor、getter、equals、hashCode、toString方法
 */
public record Result<T>(Integer code, String message, T data) {

    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    /**
     * 错误响应（默认500错误码）
     */
    public static <T> Result<T> error(String message) {
        return error(500, message);
    }

    /**
     * 错误响应（自定义错误码）
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return code != null && code == 200;
    }
}