package org.nevil.response;

import static org.nevil.response.HttpContent.RESPONSE_TEMPLATE;

public class HttpResponse {
    /**
     * 生成响应
     *
     * @param status 响应状态
     * @return 响应字符串
     */
    public static String generateResponse(HttpStatusEnum status) {
        return generateResponse(status, null);
    }

    /**
     * 生成响应
     *
     * @param result 处理结果
     * @return 响应字符串
     */
    public static String generateResponse(HttpStatusEnum status, Object result) {
        String prefix = String.format(RESPONSE_TEMPLATE, status.getInfo());

        // 检查 result 是否为 null，避免直接拼接 "null"
        if (result == null) {
            return prefix + "null";
        }

        try {
            // 确保 result 的 toString() 方法安全调用
            String resultString = String.valueOf(result);
            return prefix + resultString;
        } catch (Exception e) {
            // 捕获异常并返回错误信息，避免程序崩溃
            return prefix + "Error: Unable to process result due to an internal error.";
        }
    }
}
