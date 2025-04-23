package org.nevil.server;

import lombok.extern.slf4j.Slf4j;
import org.nevil.model.CmConfig;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class Server {
    public static void start() {
        try {
            // 1. 读取配置文件
            CmConfig cmConfig = CmConfig.parseCmConfig();

            // 2. 创建服务器套接字
            ServerSocket serverSocket = new ServerSocket(cmConfig.getPort());
            log.info("Server started on port {}", cmConfig.getPort());

            // 3. 创建线程池
            ExecutorService clientExecutor = Executors.newFixedThreadPool(cmConfig.getMaxThreads());
            log.info("Server create threadPool, maxThreads = {}", cmConfig.getMaxThreads());

            // 4. 等待客户端连接
            log.info("Waiting for clients to connect...");
            while (true) {
                try {
                    // 5. 接受客户端连接
                    Socket clientSocket = serverSocket.accept();
                    log.info("New client connected");

                    // 6. 创建客户端处理程序
                    clientExecutor.execute(new ClientHandler(clientSocket));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}


