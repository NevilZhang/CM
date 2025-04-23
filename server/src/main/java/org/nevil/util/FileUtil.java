package org.nevil.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtil {
    /**
     * 读取文件内容并返回字符串
     *
     * @param filePath 文件路径
     * @return 文件内容字符串
     * @throws IOException 如果读取文件时发生错误
     */
    public static String readFileContent(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return Files.readString(path);
    }
}
