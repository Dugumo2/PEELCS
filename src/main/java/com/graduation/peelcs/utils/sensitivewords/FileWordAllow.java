package com.graduation.peelcs.utils.sensitivewords;

import com.github.houbb.sensitive.word.api.IWordAllow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class FileWordAllow implements IWordAllow {


    private final List<String> allowWords = new ArrayList<>();
    private final ResourceLoader resourceLoader;

    public FileWordAllow(ResourceLoader resourceLoader,
                        @Value("${allow-word.file-path:classpath:static/sensitive_words_lines.txt}") String filePath) {
        this.resourceLoader = resourceLoader;
        this.allowWords.addAll(loadWordsFromResource(filePath));  // 把读取到的词添加到成员变量中
    }

    public List<String> loadWordsFromResource(String filePath) {
        List<String> words = new ArrayList<>(10000);
        try {
            Resource resource = resourceLoader.getResource(filePath);
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#")) {
                        words.add(line);
                    }
                }
            }
            log.info("成功加载 {} 个白名单词", words.size());  // 添加日志
        } catch (IOException e) {
            log.error("读取白名单文件失败: {}", e.getMessage());
        }
        return words;
    }

    @Override
    public List<String> allow() {
        return allowWords;
    }
}