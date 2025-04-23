package org.nevil.server;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.nevil.handler.ControllerHandler;
import org.nevil.request.RequestInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Data
public class ClientHandler implements Runnable {
    private static final Map<String, ControllerHandler> routeMap = new HashMap<>();

    static {
        // 示例路由配置
        routeMap.put("/helloWorld", requestInfo -> "Hello, World!");
    }

    private static final String RESPONSE_HEADER_200 = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\n";
    private static final String RESPONSE_HEADER_404 = "HTTP/1.1 404 Not Found\r\nContent-Type: text/plain\r\n\r\n";
    private static final String RESPONSE_HEADER_500 = "HTTP/1.1 500 Internal Server Error\r\nContent-Type: text/plain\r\n\r\n";

    private Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            // 1. 解析请求
            InputStream input = clientSocket.getInputStream();
            String request = readRequest(input);
            RequestInfo requestInfo = parseRequest(request);

            // 2. 路由匹配
            ControllerHandler handler = routeMap.get(requestInfo.getUrl());
            if (handler == null) {
                // 如果没有找到对应的处理程序，返回404
                clientSocket.getOutputStream().write(generateResponse(RESPONSE_HEADER_404, "404 Not Found").getBytes());
                return;
            }

            // 4. 处理请求
            Object result = handler.handle(requestInfo);

            // 5. 生成响应
            clientSocket.getOutputStream().write(generateResponse(RESPONSE_HEADER_200, result).getBytes());
            clientSocket.close();
        } catch (IOException e) {
            log.error("Error handling client request: {}", e.getMessage());
            try {
                clientSocket.getOutputStream().write(generateResponse(RESPONSE_HEADER_500, "500 Internal Server Error").getBytes());
            } catch (Exception ex) {
                log.error("Error writing to client socket: {}", ex.getMessage());
            }
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                log.error("Error closing client socket: {}", e.getMessage());
            }
        }
    }

    /**
     * 读取请求
     *
     * @param input InputStream
     * @return String
     */
    private static String readRequest(InputStream input) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        StringBuilder requestBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            requestBuilder.append(line).append("\r\n");
        }
        return requestBuilder.toString();
    }

    /**
     * 解析请求
     *
     * @param request 请求字符串
     * @return RequestInfo
     */
    private static RequestInfo parseRequest(String request) {
        // 简单的请求解析，仅提取URL
        String[] lines = request.split("\r\n");
        String[] firstLine = lines[0].split(" ");
        String method = firstLine[0];
        String url = firstLine[1];
        return new RequestInfo(method, url);
    }

    /**
     * 生成响应
     *
     * @param result 处理结果
     * @return 响应字符串
     */
    private static String generateResponse(String prefix, Object result) {
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
