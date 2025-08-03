package priv.wzw.onw.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseBody<T> {
    public static final int SUCCESS_CODE = 0;
    public static final int FAIL_CODE = 1;

    private int code;
    private String message;
    private T data;

    public static <T> ResponseBody<T> success() {
        return success(null);
    }

    public static <T> ResponseBody<T> success(T data) {
        return ResponseBody.<T>builder().code(SUCCESS_CODE).message("success").data(data).build();
    }

    public static <T> ResponseBody<T> fail(String message) {
        return fail(FAIL_CODE, message);
    }

    public static <T> ResponseBody<T> fail(int code, String message) {
        return ResponseBody.<T>builder().code(code).message(message).build();
    }
}
