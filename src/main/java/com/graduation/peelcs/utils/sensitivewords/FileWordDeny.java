package com.graduation.peelcs.utils.sensitivewords;

import com.github.houbb.sensitive.word.api.IWordDeny;
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

@Component
@Slf4j
public class FileWordDeny implements IWordDeny {

    private final List<String> sensitiveWords = new ArrayList<>();
    private final ResourceLoader resourceLoader;

    public FileWordDeny(ResourceLoader resourceLoader,
                        @Value("${sensitive-word.file-path:classpath:static/sensitive_words_lines.txt}") String filePath) {
        this.resourceLoader = resourceLoader;
        this.sensitiveWords.addAll(loadWordsFromResource(filePath));  // 把读取到的词添加到成员变量中
    }

    public List<String> loadWordsFromResource(String filePath) {
        List<String> words = new ArrayList<>(60000);
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
            log.info("成功加载 {} 个敏感词", words.size());  // 添加日志
        } catch (IOException e) {
            log.error("读取敏感词文件失败: {}", e.getMessage());
        }
        return words;
    }

    @Override
    public List<String> deny() {
        return sensitiveWords;
    }
}