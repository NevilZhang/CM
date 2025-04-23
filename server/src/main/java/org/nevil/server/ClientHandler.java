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

import static org.nevil.response.HttpResponse.generateResponse;
import static org.nevil.response.HttpStatusEnum.*;

@Slf4j
@Data
public class ClientHandler implements Runnable {
    // 路由映射表(url -> method -> ControllerHandler)
    private static final Map<String, Map<String, ControllerHandler>> routeMap = new HashMap<>();

    static {
        // 示例路由配置
        Map<String, ControllerHandler> methodMap = new HashMap<>();
        methodMap.put("GET", requestInfo -> "Hello, World!");
        routeMap.put("/", methodMap);
    }

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
            Map<String, ControllerHandler> methodMap = routeMap.get(requestInfo.getUrl());
            if (methodMap == null) {
                // 如果没有找到对应的方法映射表，返回404
                clientSocket.getOutputStream().write(generateResponse(NOT_FOUND).getBytes());
                return;
            }
            ControllerHandler handler = methodMap.get(requestInfo.getMethod());
            if (handler == null) {
                // 如果没有找到对应的处理程序，返回405
                clientSocket.getOutputStream().write(generateResponse(METHOD_NOT_ALLOWED).getBytes());
                return;
            }

            // 3. 处理请求
            Object result = handler.handle(requestInfo);

            // 4. 生成响应
            clientSocket.getOutputStream().write(generateResponse(OK, result).getBytes());
            clientSocket.close();
        } catch (IOException e) {
            log.error("Error handling client request: {}", e.getMessage());
            try {
                // 处理异常，返回500
                clientSocket.getOutputStream().write(generateResponse(INTERNAL_SERVER_ERROR, "500 Internal Server Error").getBytes());
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


}
