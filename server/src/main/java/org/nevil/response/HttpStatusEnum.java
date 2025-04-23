package org.nevil.response;

public enum HttpStatusEnum {
    OK(200, "OK"),
    BAD_REQUEST(400, "Bad Request"),
    NOT_FOUND(404, "Not Found"),
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error");

    private int code;

    private String desc;

    HttpStatusEnum(int code, String desc) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static HttpStatusEnum getHttpStatusEnum(int code) {
        for (HttpStatusEnum httpStatusEnum : HttpStatusEnum.values()) {
            if (httpStatusEnum.getCode() == code) {
                return httpStatusEnum;
            }
        }
        return null;
    }

    public String getInfo() {
        return code + " " + desc;
    }
}
