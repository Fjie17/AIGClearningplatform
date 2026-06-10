package com.example.learningplatform.dto;

public class ApiResponse<T> {

    private int code;
    private String message;
    private T data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.code = 200;
        r.message = "success";
        r.data = data;
        return r;
    }

    public static ApiResponse<Void> success() {
        ApiResponse<Void> r = new ApiResponse<>();
        r.code = 200;
        r.message = "success";
        return r;
    }

    public static ApiResponse<Void> error(String msg) {
        ApiResponse<Void> r = new ApiResponse<>();
        r.code = 500;
        r.message = msg;
        return r;
    }

    public static <T> ApiResponse<T> error(String msg, Class<T> clazz) {
        ApiResponse<T> r = new ApiResponse<>();
        r.code = 500;
        r.message = msg;
        return r;
    }
}
