package org.nevil.model;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nevil.util.FileUtil;

import java.io.IOException;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CmConfig {
    private static final String CM_CONFIG = "config/cmConfig.json";
    private String cmName;
    private Integer port = 8080;
    private Integer maxThreads = 200;

    /**
     * 解析配置文件
     *
     * @return CmConfig
     */
    public static CmConfig parseCmConfig() {
        try {
            return JSON.parseObject(FileUtil.readFileContent(CM_CONFIG), CmConfig.class);
        } catch (IOException e) {
            CmConfig cmConfig = new CmConfig();
            log.info("解析配置文件 CmConfig = {} 不存在, 使用默认配置 cmConfig = {}", CM_CONFIG, JSON.toJSONString(cmConfig));
            return cmConfig;
        }
    }
}
