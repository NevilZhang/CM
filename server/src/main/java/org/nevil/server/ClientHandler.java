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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        methodMap.put("GET", new ControllerHandler() {
            @Override
            public Object handle(RequestInfo requestInfo) {
                return "Hello, World!";
            }
        });
        methodMap.put("POST", new ControllerHandler() {
            @Override
            public Object handle(RequestInfo requestInfo) {
                return "Received POST request with body: " + requestInfo.getBody();
            }
        });
        routeMap.put("/", methodMap);
    }

    // 客户端套接字
    private Socket clientSocket;

    // 构造函数
    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            // 1. 解析请求
            InputStream inputStream = clientSocket.getInputStream();

            // 解析Header
            String headerStr = readHeader(inputStream);
            RequestInfo requestInfo = parseRequest(headerStr);

            // 检查 Expect: 100-continue
            if (requestInfo.getHeaders().containsKey("Expect") && requestInfo.getHeaders().get("Expect").equalsIgnoreCase("100-continue")) {
                clientSocket.getOutputStream().write(generateResponse(CONTINUE).getBytes());
            }

            int contentLength = readContentLength(headerStr);

            // 读取POST请求体
            if ("POST".equalsIgnoreCase(requestInfo.getMethod()) && contentLength > 0) {
                byte[] bodyBytes = new byte[contentLength];
                inputStream.read(bodyBytes, 0, contentLength);
                requestInfo.setBody(new String(bodyBytes));
            }

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

    private int readContentLength(String header) {
        // 2. 解析Content-Length
        int contentLength = 0;
        Pattern pattern = Pattern.compile("Content-Length: (\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(header);
        if (matcher.find()) {
            contentLength = Integer.parseInt(matcher.group(1));
        }
        return contentLength;
    }

    private String readHeader(InputStream input) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));

        // 1. 读取Header
        StringBuilder headerBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            headerBuilder.append(line).append("\r\n");
        }
        String header = headerBuilder.toString();
        return header;
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
        RequestInfo requestInfo = new RequestInfo();
        String[] lines = request.split("\r\n");

        // 解析请求行
        String[] firstLine = lines[0].split(" ");
        requestInfo.setMethod(firstLine[0]);
        requestInfo.setUrl(firstLine[1]);

        // 解析请求头
        int i = 1;
        for (; i < lines.length; i++) {
            String line = lines[i];
            if (line.isEmpty()) {
                break;
            }
            String[] headerParts = line.split(": ", 2);
            if (headerParts.length == 2) {
                requestInfo.getHeaders().put(headerParts[0], headerParts[1]);
            }
        }

        // 解析请求体（仅适用于POST请求）
        if (i < lines.length) {
            StringBuilder bodyBuilder = new StringBuilder();
            for (int j = i + 1; j < lines.length; j++) {
                bodyBuilder.append(lines[j]);
            }
            requestInfo.setBody(bodyBuilder.toString());
        }

        return requestInfo;
    }

    // todo routeMap 注册方法
}